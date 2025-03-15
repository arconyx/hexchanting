package gay.thehivemind.hexchanting.casting.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.api.casting.mishaps.MishapOthersName
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.xplat.IXplatAbstractions
import gay.thehivemind.hexchanting.items.HexHolderEquipment
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

// TODO: How to handle in circles
class OpImbueEquipment : SpellAction {
    override val argc = 1
    private val mediaCost = 10 * MediaConstants.CRYSTAL_UNIT

    override fun execute(
        args: List<Iota>,
        env: CastingEnvironment
    ): SpellAction.Result {
        val patterns = args.getList(0, argc).toList()

        val (handStack) = env.getHeldItemToOperateOn {
            val hexHolder = IXplatAbstractions.INSTANCE.findHexHolder(it)
            it.item is HexHolderEquipment && hexHolder != null
        }
            ?: throw MishapBadOffhandItem(
                ItemStack.EMPTY.copy(),
                Text.of("amethyst equipment")
            ) // TODO: don't use a raw string

        val trueName = MishapOthersName.getTrueNameFromArgs(patterns, env.castingEntity as? ServerPlayerEntity)
        if (trueName != null)
            throw MishapOthersName(trueName)


        return SpellAction.Result(
            Spell(patterns, handStack),
            mediaCost,
            env.castingEntity?.let { listOf(ParticleSpray.burst(it.pos, 0.5)) } ?: listOf()
        )
    }

    private inner class Spell(val patterns: List<Iota>, val stack: ItemStack) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val hexHolder = IXplatAbstractions.INSTANCE.findHexHolder(stack) ?: return
            hexHolder.writeHex(patterns, env.pigment, 0)
        }
    }
}