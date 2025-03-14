package gay.thehivemind.hexchanting.items

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import gay.thehivemind.hexchanting.casting.PackagedToolCastEnv
import net.minecraft.block.BlockState
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

interface HexMiningTool : HexHolderEquipment {
    private enum class HOOKS(val index: Int) {
        MINE(0),
        HIT(1)
    }

    private fun scaffoldCasting(
        itemStack: ItemStack,
        world: ServerWorld,
        player: ServerPlayerEntity,
        hook: HOOKS,
        stack: List<Iota>
    ) {
        val instructionList = getHex(itemStack, world) ?: return
        val relevantInstructions = HexHolderEquipment.extractList(instructionList, hook.index)
        // You can't mine or attack with the tool in your offhand so this should be safe
        val context = PackagedToolCastEnv(player, Hand.MAIN_HAND)

        // Create empty casting image
        var castingImage = CastingImage()
        // prepare stack
        val castingStack = castingImage.stack.toMutableList()
        // I think this will convert the list instead of nesting it
        castingStack.add(ListIota(instructionList))
        // We don't need to add the player to the stack, Mind's Reflection exists
        castingStack.addAll(stack)
        castingImage = castingImage.copy(stack = castingStack.toList())

        val vm = CastingVM(castingImage, context)
        val clientView = vm.queueExecuteAndWrapIotas(relevantInstructions, world)

        // We'll probably want to do something more subtle in future but this will work for now
        if (clientView.resolutionType.success) {
            ParticleSpray(player.pos, Vec3d(0.0, 1.5, 0.0), 0.4, Math.PI / 3, 30)
                .sprayParticles(world, context.getPigment())
        }
    }

    fun castPostMine(
        stack: ItemStack?,
        world: World?,
        state: BlockState?,
        pos: BlockPos?,
        miner: LivingEntity?
    ) {
        // I hate Java's nulls
        if (stack != null && world != null && !world.isClient && miner != null && pos != null) {
            val serverWorld = world as? ServerWorld
            val player = miner as? ServerPlayerEntity
            if (serverWorld != null && player != null) {
                scaffoldCasting(stack, world, player, HOOKS.MINE, listOf(Vec3Iota(pos.toCenterPos())))
            }
        }
    }

    fun castPostHit(stack: ItemStack?, target: LivingEntity?, attacker: LivingEntity?) {
        if (stack != null && target != null && attacker != null && !attacker.world.isClient) {
            val player = attacker as? ServerPlayerEntity
            val world = player?.serverWorld
            if (player != null && world != null) {
                scaffoldCasting(stack, world, player, HOOKS.HIT, listOf(EntityIota(target)))
            }
        }
    }
}