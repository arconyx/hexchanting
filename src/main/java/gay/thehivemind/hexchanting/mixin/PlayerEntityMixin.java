package gay.thehivemind.hexchanting.mixin;

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

    @Inject(method = "applyDamage", at = @At(value = "TAIL"))
    public void triggerArmor(DamageSource source, float amount, CallbackInfo ci) {
        if (this.getWorld().isClient() || this.isInvulnerableTo(source) || source.isIn(HexchantingTags.INSTANCE.getBYPASS_ARMOUR()))
            return;
        this.getArmorItems().forEach((ItemStack stack) -> {
            if (stack.getItem() instanceof HexArmorItem armour && armour.getType() == ArmorItem.Type.CHESTPLATE) {
                armour.castOnHit(stack, source, amount, this);
            }
        });
    }
}
