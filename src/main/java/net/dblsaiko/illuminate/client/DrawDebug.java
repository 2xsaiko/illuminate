package net.dblsaiko.illuminate.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import therealfarfetchd.illuminate.client.GameRendererExtKt;

public final class DrawDebug {
    private DrawDebug() {
    }

    public static void drawDebug(MinecraftClient mc, MatrixStack mv) {
        var width = mc.getWindow().getScaledWidth();
        var height = mc.getWindow().getScaledHeight();

        var pp = GameRendererExtKt.getPostProcess(mc.gameRenderer);

        var size = 64.0f;
        var dist = 8.0f;
        var y = height - dist - size;

        var buf = Tessellator.getInstance().getBuffer();
        var curMv = mv.peek().getPositionMatrix();

        drawBuf(curMv, buf, width - dist - size, y, size, pp.playerCamDepthTex());

        int i = 1;

        for (LightContainer l : pp.activeLights()) {
            var depth = l.depthTex();
            var x0 = width - (i + 1) * dist - (i + 1) * size;
            drawBuf(curMv, buf, x0, y, size, depth);

            i += 1;
        }
    }

    private static void drawBuf(Matrix4f mv, BufferBuilder buf, float x0, float y0, float size, int tex) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, tex);

        buf.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        buf.vertex(mv, x0, y0, 0.0f).texture(0f, 1f).next();
        buf.vertex(mv, x0, y0 + size, 0.0f).texture(0f, 0f).next();
        buf.vertex(mv, x0 + size, y0 + size, 0.0f).texture(1f, 0f).next();
        buf.vertex(mv, x0 + size, y0, 0.0f).texture(1f, 1f).next();

        BufferRenderer.drawWithGlobalProgram(buf.end());
    }
}
