package gay.thehivemind.hexchanting.items.tools

import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.SwordItem
import net.minecraft.item.ToolMaterial

class HexSword(toolMaterial: ToolMaterial?, attackDamage: Int, attackSpeed: Float, settings: Settings?) : SwordItem(
    toolMaterial, attackDamage,
    attackSpeed,
    settings
), HexTool {
    override fun postHit(stack: ItemStack?, target: LivingEntity?, attacker: LivingEntity?): Boolean {
        castPostHit(stack, target, attacker)
        return super.postHit(stack, target, attacker)
    }

    override fun canRepair(stack: ItemStack?, ingredient: ItemStack?): Boolean {
        return false
    }
}