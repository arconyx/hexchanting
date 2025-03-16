package gay.thehivemind.hexchanting.casting

import at.petrak.hexcasting.api.casting.eval.MishapEnvironment
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d

// TODO: Some mishaps could be applied to living entities
class DummyMishapEnvironment(world: ServerWorld?) : MishapEnvironment(world, null) {
    override fun yeetHeldItemsTowards(targetPos: Vec3d?) {
    }

    override fun dropHeldItems() {
    }

    override fun drown() {
    }

    override fun damage(healthProportion: Float) {
    }

    override fun removeXp(amount: Int) {
    }

    override fun blind(ticks: Int) {
    }
}