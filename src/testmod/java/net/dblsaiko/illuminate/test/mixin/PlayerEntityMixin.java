package net.dblsaiko.illuminate.test.mixin;

import net.dblsaiko.illuminate.client.IlluminateClient;
import net.dblsaiko.illuminate.test.FlashlightLight;
import net.dblsaiko.illuminate.test.IlluminateTest;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin
{
    @Unique
    private final PlayerEntity self = (PlayerEntity) (Object) this;

    @Unique
    private boolean spawnedLight = false;

    @Inject(method = "tick()V", at = @At("RETURN"))
    private void update(CallbackInfo ci) {
        var isHoldingFlashlight = self.getMainHandStack().getItem() == IlluminateTest.instance().items.flashlight;
        this.spawnedLight = this.spawnedLight && isHoldingFlashlight;

        if (!this.spawnedLight) {
            if (this.self.world.isClient && isHoldingFlashlight) {
                IlluminateClient.instance().addLight(new FlashlightLight(this.self));
                this.spawnedLight = true;
            }
        }
    }
}
