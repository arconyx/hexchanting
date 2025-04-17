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

        // Calling extractMediaFromInventory twice may allow Oneironaut's BottomlessMediaItem to be drawn from
        // twice per tick, but I don't think that is going to be a big deal.

        // We want to draw media in the order
        // Inventory
        // Item (durability)
        // Player (overcasting)
        // To avoid double counting media in the inventory we need special simulation handling.

        // Start by drawing from the inventory, without overcasting
        // Simulation is disabled here because we'll calculate it later
        // Hopefully we won't have any problems with difference between sim and reality due
        // to rounding of media amounts to item sizes
        if (canCastFromInv && costLeft > 0 && !simulate) {
            costLeft = this.extractMediaFromInventory(costLeft, false, false)
        }

        // Then draw from the item
        if (casterMediaHolder != null && costLeft > 0) {
            // The contracts on the AD and on this function are different.
            // ADs return the amount extracted, this wants the amount left
            val extracted = casterMediaHolder.withdrawMedia(costLeft, simulate)
            costLeft -= extracted
        }

        // Then overcast, if possible
        // This is also the simulation run for inventory + overcasting
        // This will cause an unnecessary second pass over the inventory for unenlightened casters
        // But it's the cleanest way to fix the inventory double counting during simulation.
        if (canCastFromInv && costLeft > 0) {
            costLeft = this.extractMediaFromInventory(costLeft, this.canOvercast(), true)
        }

        return costLeft
    }
}