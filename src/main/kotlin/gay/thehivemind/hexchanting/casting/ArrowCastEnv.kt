package gay.thehivemind.hexchanting.casting

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.MishapEnvironment
import at.petrak.hexcasting.api.casting.eval.env.PlayerBasedMishapEnv
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.xplat.IXplatAbstractions
import gay.thehivemind.hexchanting.entities.HexArrowEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.GameMode
import java.util.function.Predicate
import kotlin.math.max

class ArrowCastEnv(world: ServerWorld?, private val arrow: HexArrowEntity) : CastingEnvironment(world) {
    private val AMBIT_RADIUS = 4.0
    private val shooter = arrow.owner
    private val pigment = arrow.pigment

    /**
     * Gets the caster. Can be null if [.getCaster] is also null
     *
     * @return the entity casting
     */
    override fun getCastingEntity(): LivingEntity? {
        return shooter as? LivingEntity
    }

    @Deprecated(
        "Deprecated in Hex Casting",
        ReplaceWith("getCastingEntity()")
    )
    override fun getCaster(): ServerPlayerEntity? {
        return shooter as? ServerPlayerEntity
    }

    override fun getMishapEnvironment(): MishapEnvironment {
        if (shooter is ServerPlayerEntity) {
            return PlayerBasedMishapEnv(shooter)
        }
        return DummyMishapEnvironment(world)
    }

    override fun mishapSprayPos(): Vec3d {
        return shooter?.pos ?: arrow.pos
    }

    /**
     * Attempt to extract the given amount of media. Returns the amount of media left in the cost.
     *
     *
     * If there was enough media found, it will return less or equal to zero; if there wasn't, it will be
     * positive.
     */
    override fun extractMediaEnvironment(cost: Long, simulate: Boolean): Long {
        val remainingCost = cost - arrow.getMedia()
        if (!simulate) {
            arrow.setMedia(max(arrow.getMedia() - cost, 0))
        }
        return remainingCost
    }

    /**
     * Get if the vec is close enough, to the arrow ...
     * Doesn't take into account being out of the *world*.
     */
    override fun isVecInRangeEnvironment(vec: Vec3d): Boolean {
        return vec.squaredDistanceTo(arrow.pos) <= AMBIT_RADIUS * AMBIT_RADIUS + 0.00000000001
    }

    /**
     * Return whether the shooter can edit blocks at the given permission (i.e. not adventure mode, etc.)
     */
    override fun hasEditPermissionsAtEnvironment(pos: BlockPos?): Boolean {
        if (shooter is ServerPlayerEntity) {
            return shooter.interactionManager.gameMode != GameMode.ADVENTURE && world.canPlayerModifyAt(shooter, pos)
        }
        return false // just to be safe
    }

    override fun getCastingHand(): Hand {
        return Hand.MAIN_HAND
    }

    /**
     * Get all the item stacks this env can use.
     */
    override fun getUsableStacks(mode: StackDiscoveryMode?): MutableList<ItemStack> {
        if (shooter is ServerPlayerEntity) {
            return getUsableStacksForPlayer(mode, null, shooter)
        }
        return mutableListOf()
    }

    /**
     * Get the primary/secondary item stacks this env can use (i.e. main hand and offhand for the player).
     */
    override fun getPrimaryStacks(): MutableList<HeldItemInfo> {
        if (shooter is ServerPlayerEntity) {
            return getPrimaryStacksForPlayer(castingHand, shooter)
        }
        return mutableListOf()
    }

    /**
     * Attempt to replace the first stack found which matches the predicate with the stack to replace with.
     *
     * @return whether it was successful.
     */
    override fun replaceItem(stackOk: Predicate<ItemStack>?, replaceWith: ItemStack?, hand: Hand?): Boolean {
        if (shooter is ServerPlayerEntity) {
            return replaceItemForPlayer(stackOk, replaceWith, hand, shooter)
        }
        return false
    }

    override fun getPigment(): FrozenPigment {
        if (pigment != null) {
            return pigment
        } else if (shooter is ServerPlayerEntity) {
            return IXplatAbstractions.INSTANCE.getPigment(shooter)
        }
        return FrozenPigment.DEFAULT.get()
    }

    override fun setPigment(pigment: FrozenPigment?): FrozenPigment? {
        return null
    }

    override fun produceParticles(particles: ParticleSpray, pigment: FrozenPigment) {
        particles.sprayParticles(this.world, pigment)
    }

    override fun printMessage(message: Text?) {
        if (shooter is ServerPlayerEntity) {
            shooter.sendMessage(message)
        }
    }
}