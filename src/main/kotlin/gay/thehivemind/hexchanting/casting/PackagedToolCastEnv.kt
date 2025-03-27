package gay.thehivemind.hexchanting.casting

import at.petrak.hexcasting.api.casting.eval.env.PackagedItemCastEnv
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Hand

class PackagedToolCastEnv(caster: ServerPlayerEntity?, castingHand: Hand?, val tool: ItemStack) :
    PackagedItemCastEnv(caster, castingHand) {

    override fun extractMediaEnvironment(cost: Long, simulate: Boolean): Long {
        if (this.caster.isCreative) return 0

        val casterHexHolder = IXplatAbstractions.INSTANCE.findHexHolder(tool) ?: return cost
        val canCastFromInv = casterHexHolder.canDrawMediaFromInventory()
        val casterMediaHolder = IXplatAbstractions.INSTANCE.findMediaHolder(tool)

        var costLeft = cost
        // Start by drawing from the inventory, without overcasting
        if (canCastFromInv && costLeft > 0) {
            costLeft = this.extractMediaFromInventory(costLeft, false, simulate)
        }
        // Then draw from the item
        if (casterMediaHolder != null && costLeft > 0) {
            // The contracts on the AD and on this function are different.
            // ADs return the amount extracted, this wants the amount left
            val extracted = casterMediaHolder.withdrawMedia(costLeft, simulate)
            costLeft -= extracted
        }
        // Then overcast, if possible
        if (canCastFromInv && costLeft > 0 && this.canOvercast()) {
            costLeft = this.extractMediaFromInventory(costLeft, true, simulate)
        }
        return costLeft
    }
}