package therealfarfetchd.illuminate.mixin.test;

import net.dblsaiko.illuminate.client.IlluminateClient;
import net.minecraft.entity.mob.CreeperEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import therealfarfetchd.illuminate.client.test.CreeperLight;

@Mixin(CreeperEntity.class)
public abstract class MixinCreeperEntity {

    @Unique
    private final CreeperEntity self = (CreeperEntity) (Object) this;

    @Unique
    private boolean spawnedLight = false;

    @Inject(method = "tick()V", at = @At("RETURN"))
    private void update(CallbackInfo ci) {
        if (!this.spawnedLight) {
            if (this.self.world.isClient)
                IlluminateClient.instance().addLight(new CreeperLight(this.self));
            this.spawnedLight = true;
        }
    }

}
