package gay.thehivemind.hexchanting.items

import gay.thehivemind.hexchanting.entities.HexArrowEntity
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.PersistentProjectileEntity
import net.minecraft.item.ArrowItem
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.world.World

// Most of this code is actually useless, because every happens on the entity
class HexArrowItem(settings: Settings?) : ArrowItem(settings), HexImbuedItem {
    override fun createArrow(world: World?, stack: ItemStack?, shooter: LivingEntity?): PersistentProjectileEntity {
        val arrow = HexArrowEntity(world, shooter)
        stack?.let { arrow.initFromStack(it) }
        return arrow
    }

    override fun getMedia(stack: ItemStack): Long {
        return 0
    }

    override fun getMaxMedia(stack: ItemStack): Long {
        return 0
    }

    override fun setMedia(stack: ItemStack, media: Long) {}

    override fun canProvideMedia(stack: ItemStack?): Boolean {
        return false
    }

    override fun canRecharge(stack: ItemStack?): Boolean {
        return false
    }

    override fun canDrawMediaFromInventory(stack: ItemStack?): Boolean {
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