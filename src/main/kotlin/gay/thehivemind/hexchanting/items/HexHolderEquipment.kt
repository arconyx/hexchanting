package gay.thehivemind.hexchanting.items

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.item.HexHolderItem
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.*
import at.petrak.hexcasting.common.items.magic.ItemMediaHolder
import at.petrak.hexcasting.common.items.magic.ItemPackagedHex
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.server.world.ServerWorld

// This code is mostly copied from Hex Casting's ItemPackagedHex

// Do these args really need to be nullable?
// Probably better to be safe, we can never trust Java code :(
interface HexHolderEquipment : HexHolderItem {
    override fun getMedia(stack: ItemStack?): Long {
        return 0
    }

    override fun getMaxMedia(stack: ItemStack?): Long {
        return 0
    }

    // No media capacity so we don't need any way to store it
    override fun setMedia(stack: ItemStack?, media: Long) {
        return
    }

    // Wouldn't it be interesting if this was true?
    override fun canProvideMedia(stack: ItemStack?): Boolean {
        return false
    }

    override fun canRecharge(stack: ItemStack?): Boolean {
        return false
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

        ItemMediaHolder.withMedia(stack, media, media)
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
}