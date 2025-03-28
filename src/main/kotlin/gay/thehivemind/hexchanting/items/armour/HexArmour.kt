package gay.thehivemind.hexchanting.items.armour

import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import gay.thehivemind.hexchanting.items.HexHolderEquipment
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity

interface HexArmour : HexHolderEquipment {
    fun castOnHit(itemStack: ItemStack, damageSource: DamageSource, amount: Float, target: Entity) {
        if (target.world.isClient) return
        val player = target as? ServerPlayerEntity ?: return
        scaffoldCasting(
            itemStack, player.serverWorld, player, listOf(
            damageSource.attacker?.let { EntityIota(it) } ?: NullIota(),
            damageSource.source?.let { EntityIota(it) } ?: NullIota(),
            damageSource.position?.let { Vec3Iota(it) } ?: NullIota(),
            DoubleIota(amount.toDouble())
        ))
    }

    fun castOnFall(itemStack: ItemStack, distance: Double, target: PlayerEntity) {
        if (target.world.isClient) return
        val player = target as? ServerPlayerEntity ?: return
        scaffoldCasting(
            itemStack, player.serverWorld, player, listOf(DoubleIota(distance))
        )
    }

    fun castOnDeath(itemStack: ItemStack, damageSource: DamageSource, target: PlayerEntity) {
        if (target.world.isClient) return
        val player = target as? ServerPlayerEntity ?: return
        scaffoldCasting(
            itemStack, player.serverWorld, player, listOf(
                damageSource.attacker?.let { EntityIota(it) } ?: NullIota(),
                damageSource.source?.let { EntityIota(it) } ?: NullIota(),
                damageSource.position?.let { Vec3Iota(it) } ?: NullIota(),
            )
        )
    }

    fun castOnAggro(itemStack: ItemStack, mob: LivingEntity, player: ServerPlayerEntity) {
        if (player.world.isClient) return
        scaffoldCasting(itemStack, player.serverWorld, player, listOf(EntityIota(mob)))
    }
}