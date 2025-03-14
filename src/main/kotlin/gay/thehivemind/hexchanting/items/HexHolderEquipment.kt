package gay.thehivemind.hexchanting.items

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.item.HexHolderItem
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.*
import at.petrak.hexcasting.common.items.magic.ItemMediaHolder
import at.petrak.hexcasting.common.items.magic.ItemPackagedHex
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.server.world.ServerWorld
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.sign

// TODO: Repair will likely erase any hexes on them
// Enchanting might too

// This code is mostly copied from Hex Casting's ItemPackagedHex

// Do these args really need to be nullable?
// Probably better to be safe, we can never trust Java code
// But also these functions are poorly defined for null arguments
// So let them just error

/*
The current intent is that these items can use their durability in the place of media, but it comes at a steep
conversion cost. This makes it strictly worse than artifacts and incentivises having a personal media supply.

If I make the tools use charged amethysts in their construction I don't need to feel too bad about making them on
par with diamond. It also fits their unstable nature, consuming themselves for power.
 */

interface HexHolderEquipment : HexHolderItem {
    fun getDamageToMediaConversionFactor(): Long {
        return MediaConstants.DUST_UNIT / 10
    }

    // We want this to be a lower priority than anything else in your inventory
    override fun getConsumptionPriority(stack: ItemStack?): Int {
        return 500
    }

    fun damageToMedia(stack: ItemStack, damage: Int): Long {
        val remainingDurability = stack.maxDamage - damage
        return max(remainingDurability * getDamageToMediaConversionFactor(), 0)
    }

    fun mediaToDamage(stack: ItemStack, media: Long): Int {
        // Round up so people don't get to sneak free mana
        val damageEquivalent =
            media / getDamageToMediaConversionFactor() + media.rem(getDamageToMediaConversionFactor()).sign.absoluteValue
        return max(stack.maxDamage - damageEquivalent.toInt(), 0)
    }

    override fun getMedia(stack: ItemStack): Long {
        return damageToMedia(stack, stack.damage)
    }

    override fun getMaxMedia(stack: ItemStack): Long {
        return stack.maxDamage * getDamageToMediaConversionFactor()
    }

    override fun setMedia(stack: ItemStack, media: Long) {
        stack.damage = mediaToDamage(stack, media)
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

    override fun hasHex(stack: ItemStack?): Boolean {
        return stack?.hasList(ItemPackagedHex.TAG_PROGRAM, NbtElement.COMPOUND_TYPE) ?: false
    }

    // May well need to modify this to handle our nested list of patterns
    override fun getHex(stack: ItemStack?, level: ServerWorld?): MutableList<Iota>? {
        val patsTag = stack?.getList(ItemPackagedHex.TAG_PROGRAM, NbtElement.COMPOUND_TYPE.toInt()) ?: return null

        val out = ArrayList<Iota>()
        for (patTag in patsTag) {
            val tag = patTag.asCompound
            out.add(IotaType.deserialize(tag, level))
        }
        return out
    }

    override fun writeHex(stack: ItemStack?, program: MutableList<Iota>?, pigment: FrozenPigment?, media: Long) {
        if (stack == null || program == null) {
            return
        }

        val patsTag = NbtList()
        for (pat in program) {
            patsTag.add(IotaType.serialize(pat))
        }

        stack.putList(ItemPackagedHex.TAG_PROGRAM, patsTag)
        if (pigment != null) stack.putCompound(ItemPackagedHex.TAG_PIGMENT, pigment.serializeToNBT())

        ItemMediaHolder.withMedia(stack, 0, 0)
    }

    override fun clearHex(stack: ItemStack?) {
        stack?.remove(ItemPackagedHex.TAG_PROGRAM)
        stack?.remove(ItemPackagedHex.TAG_PIGMENT)
        stack?.remove(ItemMediaHolder.TAG_MEDIA)
        stack?.remove(ItemMediaHolder.TAG_MAX_MEDIA)
    }

    override fun getPigment(stack: ItemStack?): FrozenPigment? {
        val color = stack?.getCompound(ItemPackagedHex.TAG_PIGMENT) ?: return null
        return FrozenPigment.fromNBT(color)
    }


    companion object {
        fun extractList(patterns: List<Iota>, index: Int): List<Iota> {
            if (patterns.all { it is ListIota }) {
                // this seems ugly
                return (patterns[index] as ListIota).list.toList()
            }
            // if we can't extract a list just return the whole thing
            // i'm sure this can't go wrong
            return patterns
        }
    }
}