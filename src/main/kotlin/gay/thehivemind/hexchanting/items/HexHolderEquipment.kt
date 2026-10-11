package gay.thehivemind.hexchanting.items

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import gay.thehivemind.hexchanting.casting.PackagedToolCastEnv
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.stat.Stats
import net.minecraft.util.Hand
import net.minecraft.util.math.Vec3d
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.sign

// TODO: Repair will likely erase any hexes on them
// Enchanting works fine in testing.

// This code is mostly copied from Hex Casting's ItemPackagedHex

// Do these args really need to be nullable?
// Probably better to be safe, we can never trust Java code
// But also these functions are poorly defined for null arguments
// So let them just error

/*
The current intent is that these items can use their durability in the place of media, but it comes at a steep
conversion cost. This makes it strictly worse than artefacts and incentivises having a personal media supply.

If I make the tools use charged amethysts in their construction I don't need to feel too bad about making them on
par with diamond. It also fits their unstable nature, consuming themselves for power.
 */

interface HexHolderEquipment : HexImbuedItem {
    // We want this to be a lower priority than anything else in your inventory
    override fun getConsumptionPriority(stack: ItemStack?): Int {
        return 500
    }

    /**
     * Get the current media this item holds
     */
    override fun getMedia(stack: ItemStack): Long {
        val remainingDurability = max(0, stack.maxDamage - stack.damage)
        return damageToMedia(remainingDurability)
    }

    /**
     * Get the maximum media this item can hold
     */
    override fun getMaxMedia(stack: ItemStack): Long {
        return damageToMedia(stack.maxDamage)
    }

    override fun setMedia(stack: ItemStack, media: Long) {
        // This bypasses item-breaking handling, which isn't great
        // We do this for two reasons.
        // - Ignoring unbreaking enchantments
        // - This may be called to repair the item instead of damaging it

        // TODO: mediaToDamage rounds up. Is this desirable?
        val durability = mediaToDamage(media)
        val damage = stack.maxDamage - durability

        // Clamp >= 0 so the item can't go past max durability
        // Clamp <= maxDamage so the item can't go into negative durability
        stack.damage = damage.toInt().coerceIn(0, stack.maxDamage)

        if (stack.damage >= stack.maxDamage) {
            stack.decrement(1)
        }
    }

    // We only want these items to be able to power themselves
    // It would be interesting if you could drain your gear with a careless hex,
    // but I don't  want to rewrite the inventory drain code to it right now
    override fun canProvideMedia(stack: ItemStack?): Boolean {
        return false
    }

    // You can recharge, but it'll be pricey
    // Better to repair with more materials than just shoving in media
    override fun canRecharge(stack: ItemStack?): Boolean {
        return true
    }

    override fun canDrawMediaFromInventory(stack: ItemStack?): Boolean {
        return true
    }

    fun scaffoldCasting(
        itemStack: ItemStack, world: ServerWorld, player: ServerPlayerEntity, stack: List<Iota>
    ) {
        val instructions = getHex(itemStack, world) ?: return
        // You can't mine or attack with the tool in your offhand so this should be safe
        // TODO: but not for armour. investigate
        val context = PackagedToolCastEnv(player, Hand.MAIN_HAND, itemStack)

        // We have to use copy to create a casting image with a given stack because the constructor is private
        val castingImage = CastingImage().copy(stack = stack)

        val vm = CastingVM(castingImage, context)
        val clientView = vm.queueExecuteAndWrapIotas(instructions, world)

        // We'll probably want to do something more subtle in future but this will work for now
        if (clientView.resolutionType.success) {
            ParticleSpray(player.pos, Vec3d(0.0, 1.5, 0.0), 0.4, Math.PI / 3, 30).sprayParticles(
                world,
                context.getPigment()
            )
        }

        // Notify client about item breaking
        if (itemStack.damage >= itemStack.maxDamage) {
            player.incrementStat(Stats.BROKEN.getOrCreateStat(itemStack.item))
            // Send message to client
            // Its simple but it works
            when (itemStack) {
                player.getEquippedStack(EquipmentSlot.HEAD) ->
                    return player.sendEquipmentBreakStatus(EquipmentSlot.HEAD)

                player.getEquippedStack(EquipmentSlot.CHEST) ->
                    return player.sendEquipmentBreakStatus(EquipmentSlot.CHEST)

                player.getEquippedStack(EquipmentSlot.LEGS) ->
                    return player.sendEquipmentBreakStatus(EquipmentSlot.LEGS)

                player.getEquippedStack(EquipmentSlot.FEET) ->
                    return player.sendEquipmentBreakStatus(EquipmentSlot.FEET)

                player.getEquippedStack(EquipmentSlot.MAINHAND) ->
                    return player.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND)

                player.getEquippedStack(EquipmentSlot.OFFHAND) ->
                    return player.sendEquipmentBreakStatus(EquipmentSlot.OFFHAND)

                else ->
                    return player.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND)
            }
        }
    }

    companion object {
        const val MEDIA_PER_DAMAGE = MediaConstants.DUST_UNIT / 10

        /**
         * Convert damage into equivalent media.
         */
        fun damageToMedia(damage: Int): Long {
            return MEDIA_PER_DAMAGE * damage
        }

        /**
         * Convert media into equivalent damage, rounding up
         */
        fun mediaToDamage(media: Long): Long {
            val base = media.floorDiv(MEDIA_PER_DAMAGE)
            // Kotlin doesn't have ceiling division so we do it ourselves.
            // The sign of the remainder matches the sign of `media`
            // `rem.sign` is 0 if the value is zero, else -1 or 1.
            // If there's no remainder rounder is zero
            // If media is positive and there is a remainder the rounder is 1
            // If media is negative and there's a reminder the sign is -1 so the abs value makes it 1.
            // But also why is media negative, dear caller?
            val rounder = media.rem(MEDIA_PER_DAMAGE).sign.absoluteValue
            // We don't case to int because that obscures how to handle overflows to the caller
            return base + rounder
        }
    }
}
