package gay.thehivemind.hexchanting.items.tools

import net.minecraft.block.BlockState
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ShovelItem
import net.minecraft.item.ToolMaterial
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class HexShovel(toolMaterial: ToolMaterial, attackDamage: Float, attackSpeed: Float, settings: Settings?) : ShovelItem(
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

    override fun canRepair(stack: ItemStack?, ingredient: ItemStack?): Boolean {
        return false
    }

    override fun appendTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Text>?,
        context: TooltipContext?
    ) {
        super.appendTooltip(stack, world, tooltip, context)
        appendHexFlagToTooltip(stack, world, tooltip, context)
    }
}