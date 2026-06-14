package com.valaphee.edgejump.mixin;

import com.valaphee.edgejump.EdgeJump;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class ClientPlayerEntityMixin {
    @Shadow
    protected abstract boolean clipAtLedge();

    @Inject(method = "adjustMovementForSneaking", at = @At("HEAD"), cancellable = true)
    private void onAdjustMovementForCollisions(Vec3d movement, MovementType type, CallbackInfoReturnable<Vec3d> cir) {
        if (!(((Object) this) instanceof ClientPlayerEntity player)) return;

        float stepHeight = player.getStepHeight();
        PlayerAbilities abilities = player.getAbilities();

        if (!abilities.flying && !(movement.y > 0.0) && player.isOnGround()) {
            double moveX = movement.x;
            double moveZ = movement.z;
            double threshold = 0.05;
            double stepX = Math.signum(moveX) * threshold;
            double stepZ = Math.signum(moveZ) * threshold;

            while (moveX != 0.0 && isSpaceAroundPlayerEmpty(player, moveX, 0.0, stepHeight)) {
                if (Math.abs(moveX) <= threshold) {
                    moveX = 0.0;
                    break;
                }
                moveX -= stepX;
            }

            while (moveZ != 0.0 && isSpaceAroundPlayerEmpty(player, 0.0, moveZ, stepHeight)) {
                if (Math.abs(moveZ) <= threshold) {
                    moveZ = 0.0;
                    break;
                }
                moveZ -= stepZ;
            }

            while (moveX != 0.0 && moveZ != 0.0 && isSpaceAroundPlayerEmpty(player, moveX, moveZ, stepHeight)) {
                if (Math.abs(moveX) <= threshold) {
                    moveX = 0.0;
                } else {
                    moveX -= stepX;
                }
                if (Math.abs(moveZ) <= threshold) {
                    moveZ = 0.0;
                } else {
                    moveZ -= stepZ;
                }
            }

            boolean edgeDetected = (moveX != movement.x || moveZ != movement.z);

            if (edgeDetected && type != MovementType.PISTON && type != MovementType.SHULKER_BOX) {
                if (clipAtLedge()) {
                    cir.setReturnValue(new Vec3d(moveX, movement.y, moveZ));
                } else if (EdgeJump.edgeJumpEnabled) {
                    ((ClientPlayerEntityAccessor) player).setTicksToNextAutojump(1);
                }
            }
        }
    }

    @Unique
    private static boolean isSpaceAroundPlayerEmpty(ClientPlayerEntity player, double offsetX, double offsetZ, float stepHeight) {
        Box box = player.getBoundingBox();
        return player.getWorld().isSpaceEmpty(player, new Box(
                box.minX + offsetX,
                box.minY - (double) stepHeight - 1.0E-5,
                box.minZ + offsetZ,
                box.maxX + offsetX,
                box.minY,
                box.maxZ + offsetZ
        ));
    }
}
