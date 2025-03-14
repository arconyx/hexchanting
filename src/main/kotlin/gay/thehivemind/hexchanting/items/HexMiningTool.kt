package gay.thehivemind.hexchanting.items

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.eval.env.PackagedItemCastEnv
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
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

    private fun castPostMine(
        itemStack: ItemStack,
        world: ServerWorld,
        pos: BlockPos,
        player: ServerPlayerEntity
    ) {
        // TODO: Only extra sublist
        val instructionList = getHex(itemStack, world) ?: return
        // You can't mine with a tool in your offhand so this should be safe
        val context = PackagedItemCastEnv(player, Hand.MAIN_HAND)

        // TODO: We could pass the block state in here and return a hexal item type iota but I don't want to deal with setting up that dependency right now

        // Create empty casting image
        var castingImage = CastingImage()

        // prepare stack
        val castingStack = castingImage.stack.toMutableList()
        // I think this will convert the list instead of nesting it
        castingStack.add(ListIota(instructionList))
        castingStack.add(EntityIota(player)) // we don't need this, mind's reflection exists
        castingStack.add(Vec3Iota(pos.toCenterPos()))
        castingImage = castingImage.copy(stack = castingStack.toList())

        val vm = CastingVM(castingImage, context)
        val clientView = vm.queueExecuteAndWrapIotas(instructionList, world)

        // We'll probably want to do something more subtle in future but this will work for now
        if (clientView.resolutionType.success) {
            ParticleSpray(player.pos, Vec3d(0.0, 1.5, 0.0), 0.4, Math.PI / 3, 30)
                .sprayParticles(world, context.getPigment())
        }
    }

    fun safeCastPostMine(
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
                castPostMine(stack, serverWorld, pos, player)
            }
        }
    }
}