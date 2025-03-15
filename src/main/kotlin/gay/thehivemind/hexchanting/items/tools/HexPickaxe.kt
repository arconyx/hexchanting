package gay.thehivemind.hexchanting.items.tools

import net.minecraft.block.BlockState
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.PickaxeItem
import net.minecraft.item.ToolMaterial
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class HexPickaxe(toolMaterial: ToolMaterial, attackDamage: Int, attackSpeed: Float, settings: Settings?) : PickaxeItem(
    toolMaterial, attackDamage, attackSpeed,
    settings,
), HexTool {
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
}