package therealfarfetchd.illuminate.mixin;

import net.minecraft.client.util.math.Matrix4f;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import therealfarfetchd.illuminate.client.Matrix4fExt;
import therealfarfetchd.qcommon.croco.Mat4;

@Mixin(Matrix4f.class)
public class MixinMatrix4f implements Matrix4fExt {

    @Shadow protected float a00;
    @Shadow protected float a01;
    @Shadow protected float a02;
    @Shadow protected float a03;
    @Shadow protected float a10;
    @Shadow protected float a11;
    @Shadow protected float a12;
    @Shadow protected float a13;
    @Shadow protected float a20;
    @Shadow protected float a21;
    @Shadow protected float a22;
    @Shadow protected float a23;
    @Shadow protected float a30;
    @Shadow protected float a31;
    @Shadow protected float a32;
    @Shadow protected float a33;

    @Override
    public void set(Mat4 mat) {
        this.a00 = mat.c00;
        this.a01 = mat.c01;
        this.a02 = mat.c02;
        this.a03 = mat.c03;
        this.a10 = mat.c10;
        this.a11 = mat.c11;
        this.a12 = mat.c12;
        this.a13 = mat.c13;
        this.a20 = mat.c20;
        this.a21 = mat.c21;
        this.a22 = mat.c22;
        this.a23 = mat.c23;
        this.a30 = mat.c30;
        this.a31 = mat.c31;
        this.a32 = mat.c32;
        this.a33 = mat.c33;
    }

}
