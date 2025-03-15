package gay.thehivemind.hexchanting.mixin;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gay.thehivemind.hexchanting.casting.PackagedToolCastEnv;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "at.petrak.hexcasting.common.casting.actions.spells.OpBreakBlock$Spell")
public class OpBreakBlockSpellMixin {
    @WrapOperation(method = "cast(Lat/petrak/hexcasting/api/casting/eval/CastingEnvironment;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;breakBlock(Lnet/minecraft/util/math/BlockPos;ZLnet/minecraft/entity/Entity;)Z"))
    public boolean wrapDestroyBlock(ServerWorld world, BlockPos blockPos, boolean drop, Entity breakingEntity, Operation<Boolean> original, CastingEnvironment env) {
        BlockState blockState = world.getBlockState(blockPos);
        if (drop && env instanceof PackagedToolCastEnv && !blockState.isAir()) {
            // We're running the drop code a little earlier than it'll normally go, but it'll probably be fine
            // Cleanly mixing into breakBlock to use a specific tool is surprisingly awkward.
            BlockEntity blockEntity = blockState.hasBlockEntity() ? world.getBlockEntity(blockPos) : null;
            Block.dropStacks(blockState, world, blockPos, blockEntity, breakingEntity, ((PackagedToolCastEnv) env).getTool());
            return original.call(world, blockPos, false, breakingEntity);
        }
        return original.call(world, blockPos, drop, breakingEntity);
    }
}
