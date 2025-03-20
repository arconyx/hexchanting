package gay.thehivemind.hexchanting.items.armour

import net.minecraft.item.ItemStack
import net.minecraft.item.ShieldItem

class HexShield(settings: Settings?) : ShieldItem(settings), HexArmour {
    override fun canRepair(stack: ItemStack?, ingredient: ItemStack?): Boolean {
        return false
    }
}