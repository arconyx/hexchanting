package gay.thehivemind.hexchanting.items

import net.minecraft.block.BlockState
import net.minecraft.entity.LivingEntity
import net.minecraft.item.AxeItem
import net.minecraft.item.ItemStack
import net.minecraft.item.ToolMaterial
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class HexAxe(material: ToolMaterial?, attackDamage: Float, attackSpeed: Float, settings: Settings?) : AxeItem(
    material, attackDamage,
    attackSpeed,
    settings
), HexMiningTool {
    override fun postMine(
        stack: ItemStack?,
        world: World?,
        state: BlockState?,
        pos: BlockPos?,
        miner: LivingEntity?
    ): Boolean {
        castPostMine(stack, world, state, pos, miner)
        return super.postMine(stack, world, state, pos, miner)
    }

    override fun postHit(stack: ItemStack?, target: LivingEntity?, attacker: LivingEntity?): Boolean {
        castPostHit(stack, target, attacker)
        return super.postHit(stack, target, attacker)
    }
}