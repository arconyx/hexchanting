package gay.thehivemind.hexchanting.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import gay.thehivemind.hexchanting.items.armour.HexArmorItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;
import java.util.stream.StreamSupport;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    @Unique
    private boolean triggeredCastForCurrentFall = false;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @WrapOperation(method = "handleFall", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;fall(DZLnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)V"))
    public void triggerBootsOnFall(ServerPlayerEntity instance, double heightDifference, boolean onGround, BlockState state, BlockPos landedPos, Operation<Void> original) {
        original.call(instance, heightDifference, onGround, state, landedPos);
        // Trigger if falling, checking fall distance to ensure we don't fire for going down some stairs
        // Use a flag so it only fires once. We aren't serializing it so it won't be maintained across reload, but
        // that's probably fine.
        // We also check they aren't using elytra so it can't trigger in flight.
        if (!onGround && !triggeredCastForCurrentFall && heightDifference < 0.0f && instance.fallDistance > 1.5f && !instance.isFallFlying()) {
            instance.getArmorItems().forEach((ItemStack stack) -> {
                if (stack.getItem() instanceof HexArmorItem armour && armour.getType() == ArmorItem.Type.BOOTS) {
                    armour.castOnFall(stack, instance.fallDistance, instance);
                    triggeredCastForCurrentFall = true;
                }
            });
            // Permit reset if the player starts going up.
            // This leads to some silly behaviour with Altiora impulsing you up and chaining
            // Which may be a problem but the spell is more flexible this way. Also, it's funny.
            // It would be nice if we could just pass the fall distance to the player each tick and let them decide
            // when to activate, but that allows for casting on every tick, which we want to avoid.
        } else if (onGround || (heightDifference > 0.0f && instance.fallDistance == 0.0f)) {
            triggeredCastForCurrentFall = false;
        }
    }

    @WrapMethod(method = "onDeath")
    public void triggerLeggingsOnDeath(DamageSource source, Operation<Void> original) {
        // We want to trigger the spell after items have been splattered, but we need to check the player's
        // inventory before that else the armour won't be present
        Optional<ItemStack> hexPantsStack = StreamSupport.stream(this.getArmorItems().spliterator(), false)
                .filter((ItemStack stack) -> stack.getItem() instanceof HexArmorItem armour && armour.getType() == ArmorItem.Type.LEGGINGS)
                .findFirst();

        // Always need to call the original function
        original.call(source);

        // If the armour is present then cast
        // FIX: There'll be no media available so this will draw from the item
        hexPantsStack.ifPresent((ItemStack stack) -> {
            ((HexArmorItem) stack.getItem()).castOnDeath(stack, source, this);
        });
    }
}
