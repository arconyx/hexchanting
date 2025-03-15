package gay.thehivemind.hexchanting.items

import gay.thehivemind.hexchanting.Hexchanting.MOD_ID
import gay.thehivemind.hexchanting.items.tools.HexAxe
import gay.thehivemind.hexchanting.items.tools.HexPickaxe
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.item.Item
import net.minecraft.item.ItemGroups
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier


object HexchantingItems {
    private val HEX_PICKAXE = register(
        "amethyst_pickaxe",
        HexPickaxe(HexToolMaterials.AMETHYST, 1, -2.8f, Item.Settings())
    )
    private val HEX_AXE = register(
        "amethyst_axe",
        HexAxe(HexToolMaterials.AMETHYST, 5F, -3f, Item.Settings())
    )

    init {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register { it.add(HEX_PICKAXE) }
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register { it.add(HEX_AXE) }
    }

    private fun register(name: String, item: Item): Item {
        // Create the item key.
        val itemKey: RegistryKey<Item> = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, name))
        // Register the item.
        Registry.register(Registries.ITEM, itemKey, item)
        return item
    }
}