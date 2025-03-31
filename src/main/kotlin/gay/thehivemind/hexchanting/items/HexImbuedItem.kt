package gay.thehivemind.hexchanting.items

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.PatternIota
import at.petrak.hexcasting.api.item.HexHolderItem
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.*
import at.petrak.hexcasting.common.items.magic.ItemPackagedHex
import at.petrak.hexcasting.common.lib.hex.HexActions
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

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

    fun appendHexFlagToTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Text>?,
        context: TooltipContext?
    ) {
        val patterns = stack?.getList(TAG_PROGRAM, NbtElement.COMPOUND_TYPE.toInt())
        if (patterns != null && patterns.size > 0) {
            // Self-indulgent variable colouring
            val time = world?.time?.div(5)?.rem(814621)?.toFloat() ?: 10F // magic number
            val pigment = getPigment(stack)?.colorProvider?.getColor(time, stack.holder?.pos ?: Vec3d(0.3, 0.4, 0.5))
            // PatternIota.display adds styling we're then going to overwrite
            // since I can't be bothered adding inline as a dependency to do it properly
            val hexxy = PatternIota.display(HexActions.EVAL.prototype)
            val proudHexxy =
                hexxy.copy().fillStyle(hexxy.style.withColor(pigment ?: 16777215)) // == Formatting.White.colorValue
            tooltip?.add(proudHexxy)
        }
    }
}