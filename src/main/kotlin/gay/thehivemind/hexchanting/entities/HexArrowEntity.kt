package gay.thehivemind.hexchanting.entities

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.*
import gay.thehivemind.hexchanting.casting.ArrowCastEnv
import gay.thehivemind.hexchanting.items.HexArrowItem
import gay.thehivemind.hexchanting.items.HexchantingItems.HEX_ARROW
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.ArrowEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class HexArrowEntity(
    world: World?, owner: LivingEntity?
) : ArrowEntity(world, owner) {
    private var patterns: List<Iota> = listOf()
    var pigment: FrozenPigment? = FrozenPigment.DEFAULT.get()
    private var media: Long = MAX_MEDIA

    fun getMedia(): Long {
        return media
    }

    fun setMedia(value: Long) {
        media = value.coerceAtLeast(0).coerceAtMost(MAX_MEDIA)
    }

    override fun initFromStack(stack: ItemStack) {
        super.initFromStack(stack)
        if (stack.isOf(HEX_ARROW) && !world.isClient) {
            val arrow = stack.item as HexArrowItem
            this.patterns = arrow.getHex(stack, world as ServerWorld) ?: listOf()
            this.pigment = arrow.getPigment(stack)
        }
    }

    override fun onHit(target: LivingEntity) {
        super.onHit(target)
        cast(target, null)
    }

    override fun onBlockHit(blockHitResult: BlockHitResult?) {
        super.onBlockHit(blockHitResult)
        cast(null, blockHitResult?.blockPos)
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound?) {
        super.writeCustomDataToNbt(nbt)
        if (patterns.isNotEmpty()) {
            val nbtList = NbtList()
            patterns.forEach { nbtList.add(IotaType.serialize(it)) }
            nbt.putList(PROGRAM_TAG, nbtList)
        }
        pigment?.let { nbt.putCompound(PIGMENT_TAG, it.serializeToNBT()) }
        nbt.putLong(MEDIA_TAG, media)
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound?) {
        super.readCustomDataFromNbt(nbt)
        if (nbt?.hasList(PROGRAM_TAG) == true && !this.world.isClient) {
            val nbtList = nbt.getList(PROGRAM_TAG, NbtElement.COMPOUND_TYPE)
            patterns = nbtList.map { IotaType.deserialize(it.asCompound, this.world as ServerWorld) }
        } else {
            patterns = listOf()
        }
        if (nbt?.hasCompound(PIGMENT_TAG) == true) {
            pigment = FrozenPigment.fromNBT(nbt.getCompound(PIGMENT_TAG))
        }
        if (nbt?.hasLong(MEDIA_TAG) == true) {
            media = nbt.getLong(MEDIA_TAG)
        }
    }

    private fun cast(target: LivingEntity?, position: BlockPos?) {
        if (world.isClient) {
            return
        }
        val serverWorld = world as? ServerWorld ?: return
        val env = ArrowCastEnv(serverWorld, this)

        // Create empty casting image
        var castingImage = CastingImage()
        // prepare stack
        val castingStack = castingImage.stack.toMutableList()
//        castingStack.add(ListIota(patterns))
        castingStack.add(EntityIota(this))
        if (target != null) {
            castingStack.add(EntityIota(target))
        }
        if (position != null) {
            castingStack.add(Vec3Iota(position.toCenterPos()))
        }
        castingImage = castingImage.copy(stack = castingStack.toList())

        val vm = CastingVM(castingImage, env)
        val clientView = vm.queueExecuteAndWrapIotas(patterns, serverWorld)

        // We'll probably want to do something more subtle in future but this will work for now
        if (clientView.resolutionType.success) {
            ParticleSpray(pos, Vec3d(0.0, 1.5, 0.0), 0.4, Math.PI / 3, 30).sprayParticles(serverWorld, env.pigment)
        }
    }

    override fun asItemStack(): ItemStack {
        return ItemStack.EMPTY
    }

    // TODO: Is this working?
    override fun getColor(): Int {
        return 9267916
    }

    companion object {
        private const val PROGRAM_TAG = "hexchanting:program"
        private const val PIGMENT_TAG = "hexchanting:pigment"
        private const val MEDIA_TAG = "hexchanting:media"
        private const val MAX_MEDIA = MediaConstants.SHARD_UNIT
    }
}