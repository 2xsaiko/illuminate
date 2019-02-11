package therealfarfetchd.illuminate.mixin.test;

import net.minecraft.entity.mob.CreeperEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import therealfarfetchd.illuminate.client.api.Lights;
import therealfarfetchd.illuminate.client.test.CreeperLight;

@Mixin(CreeperEntity.class)
public abstract class MixinCreeperEntity {

    private CreeperEntity self = (CreeperEntity) (Object) this;

    private boolean spawnedLight = false;

    @Inject(method = "update()V", at = @At("RETURN"))
    private void update(CallbackInfo ci) {
        if (!spawnedLight) {
            if (self.world.isClient)
                Lights.getInstance().add(new CreeperLight(self));
            spawnedLight = true;
        }
    }

}
