package gay.thehivemind.hexchanting.items

import gay.thehivemind.hexchanting.Hexchanting.MOD_ID
import net.minecraft.item.Item
import net.minecraft.item.ToolMaterials
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.BlockTags
import net.minecraft.util.Identifier


object HexchantingItems {
    private fun register(name: String, item: Item): Item {
        // Create the item key.
        val itemKey: RegistryKey<Item> = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, name))
        // Register the item.
        Registry.register(Registries.ITEM, itemKey, item)
        return item
    }

    val HEX_PICKAXE = register(
        "amethyst_pickaxe",
        HexMiningToolItem(1f, -2.8f, ToolMaterials.IRON, BlockTags.PICKAXE_MINEABLE, Item.Settings())
    )
}