package gay.thehivemind.hexchanting.items.armour

import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.item.ShieldItem
import net.minecraft.text.Text
import net.minecraft.world.World

class HexShield(settings: Settings?) : ShieldItem(settings), HexArmour {
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