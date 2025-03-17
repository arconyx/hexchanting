package gay.thehivemind.hexchanting.items.armour

import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import gay.thehivemind.hexchanting.items.HexHolderEquipment
import net.minecraft.entity.Entity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity

interface HexArmour : HexHolderEquipment {
    fun cast(itemStack: ItemStack, damageSource: DamageSource, amount: Float, target: Entity) {
        val player = target as? ServerPlayerEntity ?: return
        scaffoldCasting(
            itemStack, player.serverWorld, player, listOf(
            damageSource.attacker?.let { EntityIota(it) } ?: NullIota(),
            damageSource.source?.let { EntityIota(it) } ?: NullIota(),
            damageSource.position?.let { Vec3Iota(it) } ?: NullIota(),
            DoubleIota(amount.toDouble())
        ))
    }
}