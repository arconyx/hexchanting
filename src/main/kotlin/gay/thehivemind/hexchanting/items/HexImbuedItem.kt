package gay.thehivemind.hexchanting.items

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.item.HexHolderItem
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.*
import at.petrak.hexcasting.common.items.magic.ItemPackagedHex
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.server.world.ServerWorld

interface HexImbuedItem : HexHolderItem {
    companion object {
        const val TAG_PROGRAM: String = ItemPackagedHex.TAG_PROGRAM
        const val TAG_PIGMENT: String = ItemPackagedHex.TAG_PIGMENT
    }

    override fun hasHex(stack: ItemStack?): Boolean {
        return stack?.hasList(TAG_PROGRAM, NbtElement.COMPOUND_TYPE) ?: false
    }

    override fun getHex(stack: ItemStack?, level: ServerWorld?): MutableList<Iota>? {
        val patsTag = stack?.getList(TAG_PROGRAM, NbtElement.COMPOUND_TYPE.toInt()) ?: return null

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

        stack.putList(TAG_PROGRAM, patsTag)
        if (pigment != null) stack.putCompound(TAG_PIGMENT, pigment.serializeToNBT())
    }

    override fun clearHex(stack: ItemStack?) {
        stack?.remove(TAG_PROGRAM)
        stack?.remove(TAG_PIGMENT)
    }

    override fun getPigment(stack: ItemStack?): FrozenPigment? {
        val color = stack?.getCompound(TAG_PIGMENT) ?: return null
        return FrozenPigment.fromNBT(color)
    }
}