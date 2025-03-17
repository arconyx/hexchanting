package gay.thehivemind.hexchanting.mixin;

import gay.thehivemind.hexchanting.items.armour.HexArmour;
import gay.thehivemind.hexchanting.items.armour.HexShield;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract Iterable<ItemStack> getArmorItems();

    @Shadow
    protected ItemStack activeItemStack;

    @Inject(method = "applyDamage", at = @At(value = "TAIL"))
    public void triggerArmor(DamageSource source, float amount, CallbackInfo ci) {
        // TODO: Don't trigger on /kill by validating damage source
        if (this.isInvulnerableTo(source) || this.getWorld().isClient()) return;
        this.getArmorItems().forEach((ItemStack stack) -> {
            if (stack.getItem() instanceof HexArmour armour) {
                armour.cast(stack, source, amount, this);
            }
        });
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damageShield(F)V"))
    public void triggerShield(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        var activeStack = this.activeItemStack;
        if (activeStack.getItem() instanceof HexShield shield) {
            shield.cast(activeStack, source, amount, this);
        }
    }
}
