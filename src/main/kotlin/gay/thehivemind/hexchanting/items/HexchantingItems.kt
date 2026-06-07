package gay.thehivemind.hexchanting.items

import gay.thehivemind.hexchanting.Hexchanting.MOD_ID
import gay.thehivemind.hexchanting.items.armour.AmethystArmourMaterial
import gay.thehivemind.hexchanting.items.armour.HexArmorItem
import gay.thehivemind.hexchanting.items.tools.*
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.item.ArmorItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemGroups
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier


object HexchantingItems {
    val HEX_ARROW = registerWithGroup("amethyst_arrow", HexArrowItem(Item.Settings()), ItemGroups.COMBAT)

    val HEX_AXE = registerWithGroup(
        "amethyst_axe", HexAxe(HexToolMaterials.AMETHYST, 5F, -3f, Item.Settings()), ItemGroups.TOOLS
    )
    val HEX_HOE = registerWithGroup(
        "amethyst_hoe", HexHoe(HexToolMaterials.AMETHYST, -3, 0f, Item.Settings()), ItemGroups.TOOLS
    )
    val HEX_PICKAXE = registerWithGroup(
    "amethyst_pickaxe", HexPickaxe(HexToolMaterials.AMETHYST, 1, -2.8f, Item.Settings()), ItemGroups.TOOLS
    )
    val HEX_SHOVEL = registerWithGroup(
    "amethyst_shovel", HexShovel(HexToolMaterials.AMETHYST, 1.5F, -3f, Item.Settings()), ItemGroups.TOOLS
    )
    val HEX_SWORD = registerWithGroup(
    "amethyst_sword", HexSword(HexToolMaterials.AMETHYST, 3, -2.4F, Item.Settings()), ItemGroups.COMBAT
    )

    val HEX_CHESTPLATE = registerWithGroup(
    "amethyst_chestplate",
    HexArmorItem(AmethystArmourMaterial, ArmorItem.Type.CHESTPLATE, Item.Settings()),
    ItemGroups.COMBAT
    )
    val HEX_HELMET = registerWithGroup(
    "amethyst_helmet",
    HexArmorItem(AmethystArmourMaterial, ArmorItem.Type.HELMET, Item.Settings()),
    ItemGroups.COMBAT
    )
    val HEX_LEGGINGS = registerWithGroup(
    "amethyst_leggings",
    HexArmorItem(AmethystArmourMaterial, ArmorItem.Type.LEGGINGS, Item.Settings()),
    ItemGroups.COMBAT
    )
    val HEX_BOOTS = registerWithGroup(
    "amethyst_boots",
    HexArmorItem(AmethystArmourMaterial, ArmorItem.Type.BOOTS, Item.Settings()),
    ItemGroups.COMBAT
    )

//    val HEX_SHIELD = registerWithGroup(
//    "amethyst_shield", HexShield(Item.Settings()), ItemGroups.COMBAT
//    )

    private fun registerWithGroup(name: String, item: Item, group: RegistryKey<ItemGroup>): Item {
        // Create the item key.
        val itemKey: RegistryKey<Item> = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, name))
        // Register the item.
        Registry.register(Registries.ITEM, itemKey, item)
        // Register a function to the event that adds the item
        ItemGroupEvents.modifyEntriesEvent(group).register { it.add { item } }
        return item
    }
}