package gay.thehivemind.hexchanting.items.armour

import net.minecraft.item.ArmorItem
import net.minecraft.item.ArmorMaterial
import net.minecraft.item.ItemStack

class HexArmorItem(material: ArmorMaterial?, type: Type?, settings: Settings?) : ArmorItem(material, type, settings),
    HexArmour {
    override fun canRepair(stack: ItemStack?, ingredient: ItemStack?): Boolean {
        return false
    }
}