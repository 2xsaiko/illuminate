package therealfarfetchd.illuminate.client.init

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceReloader
import net.minecraft.resource.ResourceType.CLIENT_RESOURCES
import net.minecraft.util.Identifier
import net.minecraft.util.profiler.Profiler
import org.lwjgl.opengl.GL11.GL_FALSE
import org.lwjgl.opengl.GL20.GL_COMPILE_STATUS
import org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER
import org.lwjgl.opengl.GL20.GL_LINK_STATUS
import org.lwjgl.opengl.GL20.GL_VERTEX_SHADER
import org.lwjgl.opengl.GL20.glAttachShader
import org.lwjgl.opengl.GL20.glCompileShader
import org.lwjgl.opengl.GL20.glCreateProgram
import org.lwjgl.opengl.GL20.glCreateShader
import org.lwjgl.opengl.GL20.glDeleteProgram
import org.lwjgl.opengl.GL20.glDeleteShader
import org.lwjgl.opengl.GL20.glGetProgramInfoLog
import org.lwjgl.opengl.GL20.glGetProgrami
import org.lwjgl.opengl.GL20.glGetShaderInfoLog
import org.lwjgl.opengl.GL20.glGetShaderi
import org.lwjgl.opengl.GL20.glLinkProgram
import org.lwjgl.opengl.GL20.glShaderSource
import therealfarfetchd.illuminate.ModID
import therealfarfetchd.illuminate.client.postProcess
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

object Shaders {

  private var lighting = 0

  fun lighting() = lighting

  init {
    ResourceManagerHelper.get(CLIENT_RESOURCES).registerReloadListener(object : IdentifiableResourceReloadListener {

      override fun reload(s: ResourceReloader.Synchronizer, rm: ResourceManager, profiler: Profiler, profiler1: Profiler, executor: Executor, executor1: Executor): CompletableFuture<Void> {
        return CompletableFuture.runAsync(Runnable {
          if (lighting != 0) glDeleteProgram(lighting)

          lighting = loadShader(rm, "lighting")

          MinecraftClient.getInstance().gameRenderer.postProcess.onShaderReload()
        }, executor1).thenCompose<Void> { s.whenPrepared(null) }
      }

      override fun getFabricId(): Identifier = Identifier(ModID, "shaders")

    })
  }

  private fun loadShader(rm: ResourceManager, id: String): Int {
    val vshsR = rm.getResource(Identifier(ModID, "shaders/$id.vert")).get()
    val fshsR = rm.getResource(Identifier(ModID, "shaders/$id.frag")).get()

    val vshs = vshsR.inputStream.bufferedReader().readText()
    val fshs = fshsR.inputStream.bufferedReader().readText()

    vshsR.inputStream.close()
    fshsR.inputStream.close()

    val vsh = glCreateShader(GL_VERTEX_SHADER)
    val fsh = glCreateShader(GL_FRAGMENT_SHADER)
    val prog = glCreateProgram()

    // No goto? I'll make my own.
    run {
      glShaderSource(vsh, vshs)
      glShaderSource(fsh, fshs)

      glCompileShader(vsh)
      if (glGetShaderi(vsh, GL_COMPILE_STATUS) == GL_FALSE) {
        // TODO use logger
        val log = glGetShaderInfoLog(vsh, 32768)
        println("Failed to compile vertex shader '$id'")
        for (line in log.lineSequence()) println(line)
        return@run
      }

      glCompileShader(fsh)
      if (glGetShaderi(fsh, GL_COMPILE_STATUS) == GL_FALSE) {
        // TODO use logger
        val log = glGetShaderInfoLog(fsh, 32768)
        println("Failed to compile fragment shader '$id'")
        for (line in log.lineSequence()) println(line)
        return@run
      }

      glAttachShader(prog, vsh)
      glAttachShader(prog, fsh)
      glLinkProgram(prog)

      if (glGetProgrami(prog, GL_LINK_STATUS) == GL_FALSE) {
        // TODO use logger
        val log = glGetProgramInfoLog(prog, 32768)
        println("Failed to link program '$id'")
        for (line in log.lineSequence()) println(line)
        return@run
      }

      glDeleteShader(vsh)
      glDeleteShader(fsh)
      return prog
    }

    glDeleteShader(vsh)
    glDeleteShader(fsh)
    glDeleteProgram(prog)
    return 0
  }

}
