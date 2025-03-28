package gay.thehivemind.hexchanting

import gay.thehivemind.hexchanting.Hexchanting.MOD_ID
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier

object HexchantingTags {
    val BYPASS_ARMOUR = TagKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(MOD_ID, "bypasses_armour_trigger"))
}