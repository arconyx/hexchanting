package gay.thehivemind.hexchanting.items.armour

import at.petrak.hexcasting.common.lib.HexItems
import net.minecraft.item.ArmorItem
import net.minecraft.item.ArmorMaterial
import net.minecraft.recipe.Ingredient
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.util.StringIdentifiable

object AmethystArmourMaterial : ArmorMaterial, StringIdentifiable {
    private const val DURABILITY_MULTIPLIER = 25
    private const val ENCHANTABILITY = 20
    private const val TOUGHNESS = 2F
    private const val KNOCKBACK_RESISTANCE = 0F

    override fun getDurability(type: ArmorItem.Type): Int {
        return DURABILITY_MULTIPLIER * when (type) {
            ArmorItem.Type.HELMET -> 11
            ArmorItem.Type.CHESTPLATE -> 16
            ArmorItem.Type.LEGGINGS -> 15
            ArmorItem.Type.BOOTS -> 13
        }
    }

    override fun getProtection(type: ArmorItem.Type): Int {
        return when (type) {
            ArmorItem.Type.HELMET -> 3
            ArmorItem.Type.CHESTPLATE -> 8
            ArmorItem.Type.LEGGINGS -> 6
            ArmorItem.Type.BOOTS -> 3
        }
    }

    override fun getEnchantability(): Int {
        return ENCHANTABILITY
    }

    override fun getEquipSound(): SoundEvent {
        return SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND
    }

    override fun getRepairIngredient(): Ingredient {
        return Ingredient.ofItems(HexItems.CHARGED_AMETHYST)
    }

    override fun getName(): String {
        return "hexchanting_amethyst"
    }

    override fun getToughness(): Float {
        return TOUGHNESS
    }

    override fun getKnockbackResistance(): Float {
        return KNOCKBACK_RESISTANCE
    }

    override fun asString(): String {
        return name
    }

}