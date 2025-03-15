package gay.thehivemind.hexchanting.items.tools

import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import gay.thehivemind.hexchanting.items.HexHolderEquipment
import net.minecraft.block.BlockState
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

interface HexTool : HexHolderEquipment {
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
                scaffoldCasting(stack, world, player, listOf(Vec3Iota(pos.toCenterPos())))
            }
        }
    }

    fun castPostHit(stack: ItemStack?, target: LivingEntity?, attacker: LivingEntity?) {
        if (stack != null && target != null && attacker != null && !attacker.world.isClient) {
            val player = attacker as? ServerPlayerEntity
            val world = player?.serverWorld
            if (player != null && world != null) {
                scaffoldCasting(stack, world, player, listOf(EntityIota(target)))
            }
        }
    }
}