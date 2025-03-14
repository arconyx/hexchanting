package gay.thehivemind.hexchanting.casting

import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.lib.hex.HexActions
import gay.thehivemind.hexchanting.Hexchanting.MOD_ID
import gay.thehivemind.hexchanting.casting.spells.OpImbueEquipment
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier


//
object HexchantingPatterns {
    @JvmStatic
    fun init() {
        register("imbue_equipment", "dqaqdqaqdqaeadawadadawadadawa", HexDir.WEST, OpImbueEquipment())

    }

    private fun register(name: String, signature: String, startDir: HexDir, action: Action) {
        Registry.register(
            HexActions.REGISTRY,
            Identifier.of(MOD_ID, name),
            ActionRegistryEntry(HexPattern.fromAngles(signature, startDir), action)
        )
    }


}