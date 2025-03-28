package gay.thehivemind.hexchanting.mixin;

import gay.thehivemind.hexchanting.items.armour.HexArmorItem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Targeter;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity implements Targeter {
    @Shadow
    private LivingEntity target;

    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "setTarget", at = @At(value = "HEAD"))
    public void triggerHelmetOnAggro(LivingEntity target, CallbackInfo ci) {
        if (target != this.target && target instanceof ServerPlayerEntity player) {
            player.getArmorItems().forEach((ItemStack stack) -> {
                if (stack.getItem() instanceof HexArmorItem armour && armour.getType() == ArmorItem.Type.HELMET) {
                    armour.castOnAggro(stack, this, player);
                }
            });
        }
    }
}
