package gay.thehivemind.hexchanting.items.tools

import at.petrak.hexcasting.common.lib.HexItems
import net.fabricmc.yarn.constants.MiningLevels
import net.minecraft.item.ToolMaterial
import net.minecraft.recipe.Ingredient
import java.util.function.Supplier

enum class HexToolMaterials(
    private val miningLevel: Int,
    private val itemDurability: Int,
    private val miningSpeed: Float,
    private val attackDamage: Float,
    private val enchantability: Int,
    private val repairIngredient: Supplier<Ingredient>
) : ToolMaterial {
    AMETHYST(MiningLevels.DIAMOND, 1200, 8.0F, 3.0F, 20, { Ingredient.ofItems(HexItems.CHARGED_AMETHYST) });

    override fun getDurability(): Int {
        return this.itemDurability
    }

    override fun getMiningSpeedMultiplier(): Float {
        return this.miningSpeed
    }

    override fun getAttackDamage(): Float {
        return this.attackDamage
    }

    override fun getMiningLevel(): Int {
        return this.miningLevel
    }

    override fun getEnchantability(): Int {
        return this.enchantability
    }

    override fun getRepairIngredient(): Ingredient {
        return repairIngredient.get()
    }
}