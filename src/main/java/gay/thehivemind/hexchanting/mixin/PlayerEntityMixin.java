package gay.thehivemind.hexchanting.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gay.thehivemind.hexchanting.Hexchanting;
import gay.thehivemind.hexchanting.HexchantingTags;
import gay.thehivemind.hexchanting.items.armour.HexArmorItem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    public PlayerEntityMixin(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract Iterable<ItemStack> getArmorItems();

    /**
     * Invoke the chestplate hex at the start of damage processing, before armour or other modifiers are applied.
     * Exactly one of this and {@link #triggerChestplateAfterDamage(PlayerEntity, float, Operation, DamageSource)} should trigger
     * the chestplate for a given damage instance.
     *
     * @param source Damage source
     * @param amount Damage taken, before processing
     * @param ci     Mixin callback info
     */
    @Inject(method = "applyDamage", at = @At(value = "HEAD"))
    public void triggerChestplateBeforeDamage(DamageSource source, float amount, CallbackInfo ci) {
        if (!Hexchanting.CONFIG.getApplyDamageBeforeChestplateTrigger()) {
            triggerChestplate(source, amount);
        }
    }

    /**
     * Invoke the chestplate hex after damage has been applied to the player.
     * Exactly one of this and {@link #triggerChestplateBeforeDamage(DamageSource, float, CallbackInfo)} should trigger
     * the chestplate for a given damage instance.
     *
     * @param source Damage source
     * @param newHealth New health when damage is applied
     * @param original Wrapped operation
     */
    @WrapOperation(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setHealth(F)V",
    shift = At.Shift.BY)
    )
    public void triggerChestplateAfterDamage(PlayerEntity instance, float newHealth, Operation<Void> original, DamageSource source) {
        // The argument to setHealth is this.getHealth() - var7 where var7 is the damage taken
        // So v = this.getHealth() - var7
        // We rearrange the operation to extract the original damage, with armour, absorption and other such effect applied.
        if (Hexchanting.CONFIG.getApplyDamageBeforeChestplateTrigger()) {
            // the order of operations is very important here
            float damage = this.getHealth() - newHealth;
            original.call(instance, newHealth);
            triggerChestplate(source, damage);
        } else {
            original.call(instance, newHealth);
        }
    }

    /**
     * Conditionally trigger the chestplate hex.
     * The hex will not be triggered if the damage type is in the bypass tag or no hexchanting chestplate is found
     *
     * @param source The source of the damage
     * @param amount The amount of damage taken
     */
    @Unique
    private void triggerChestplate(DamageSource source, float amount) {
        if (this.getWorld().isClient() || this.isInvulnerableTo(source) || source.isIn(HexchantingTags.INSTANCE.getBYPASS_ARMOUR()))
            return;
        this.getArmorItems().forEach((ItemStack stack) -> {
            if (stack.getItem() instanceof HexArmorItem armour && armour.getType() == ArmorItem.Type.CHESTPLATE) {
                armour.castOnHit(stack, source, amount, this);
            }
        });
    }

}
