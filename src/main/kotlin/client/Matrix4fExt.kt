package therealfarfetchd.illuminate.client

import net.minecraft.client.util.math.Matrix4f
import therealfarfetchd.qcommon.croco.Mat4

interface Matrix4fExt {

  fun set(mat: Mat4)

}

fun Matrix4f.set(mat: Mat4) = (this as Matrix4fExt).set(mat)