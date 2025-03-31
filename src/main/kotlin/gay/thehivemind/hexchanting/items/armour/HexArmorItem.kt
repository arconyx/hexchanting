package gay.thehivemind.hexchanting.items.armour

import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ArmorItem
import net.minecraft.item.ArmorMaterial
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.world.World

class HexArmorItem(material: ArmorMaterial?, type: Type?, settings: Settings?) : ArmorItem(material, type, settings),
    HexArmour {
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