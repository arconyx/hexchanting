package gay.thehivemind.hexchanting

import at.petrak.hexcasting.api.casting.eval.SpecialPatterns
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.PatternIota
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.lib.HexItems
import at.petrak.hexcasting.common.lib.hex.HexActions
import at.petrak.hexcasting.xplat.IXplatAbstractions
import gay.thehivemind.hexchanting.casting.HexchantingPatterns
import gay.thehivemind.hexchanting.items.HexImbuedItem
import gay.thehivemind.hexchanting.items.HexchantingItems
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest
import net.minecraft.entity.EquipmentSlot
import net.minecraft.test.GameTest
import net.minecraft.test.GameTestException
import net.minecraft.test.TestContext
import net.minecraft.util.Hand

class HexchantingGameTest : FabricGameTest {
    val logger = Hexchanting.LOGGER

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    fun testImbuingChestplate(context: TestContext) {
        val player = context.createMockCreativeServerPlayerInWorld()

        // Equip staff and item to imbue
        player.equipStack(EquipmentSlot.MAINHAND, HexItems.STAFF_OAK.defaultStack)
        val chestplate = HexchantingItems.HEX_CHESTPLATE
        player.equipStack(EquipmentSlot.OFFHAND, chestplate.defaultStack)

        // Cast imbuing hex with staff
        val vm = IXplatAbstractions.INSTANCE.getStaffcastVM(player, Hand.OFF_HAND)
        val hexToImbue: List<Iota> = actionsAsPatternIota(HexActions.GET_CASTER.prototype)
        val iotas = makeImbueIotaList(hexToImbue)
        val castingResult = vm.queueExecuteAndWrapIotas(iotas, player.serverWorld)
        logger.info("Offhand item is {} with nbt {} and count {}", player.offHandStack.item, player.offHandStack.nbt, player.offHandStack.count)
        context.assertTrue(castingResult.resolutionType.success, "Imbuing a hex into the item failed")

        // Retrieve and verify imbued item
        val imbuedItem = player.offHandStack
        context.assertTrue(imbuedItem.item == chestplate, "Offhand item does not match expected item type")
        val hexHolderItem = imbuedItem.item as? HexImbuedItem ?: throw GameTestException("Imbued item can't be cast to HexImbuedItem")
        val imbuedHex = hexHolderItem.getHex(imbuedItem, player.serverWorld) ?: throw GameTestException("Cannot retrieve hex from HexImbuedItem")
        logger.debug("Imbued hex is {}, expected hex is {}", imbuedHex.first().serialize(), hexToImbue.first().serialize())
        context.assertTrue(iotaListsAreEqual(imbuedHex, hexToImbue ), "Imbued hex differs from expected hex")

        context.complete()
    }

    /**
     * Creates a hex that casts imbues an item with the input hex.
     *
     * This wraps the input in introspection/retrospection and then casts
     * Imbue Equipment.
     */
    private fun makeImbueIotaList(iotaToImbue: List<Iota>): List<Iota> {
        val iotas = mutableListOf<Iota>(PatternIota(SpecialPatterns.INTROSPECTION))
        iotas.addAll(iotaToImbue)
        iotas.add(PatternIota(SpecialPatterns.RETROSPECTION))
        iotas.add(PatternIota(HexchantingPatterns.HEX_IMBUE_EQUIPMENT.prototype))
        return iotas.toList()
    }

    /**
     * A simple helper to convert a list of HexActions into a list of equivalent pattern iota
     */
    private fun actionsAsPatternIota(vararg actions: HexPattern): List<PatternIota> {
        return actions.map { p -> PatternIota(p) }
    }

    /**
     * Check if two lists of iota are equal. This means they are the same length and iota in the same index are
     * (approximately) equal.
     *
     * [Iota] don't have an `Iota.equals` function defined. This means they compare using referential equality, which has
     * lead to false negatives. However, Hex Casting has an equals pattern. This is implemented using [Iota.tolerates]
     * and uses structural equality. This isn't exact equality - doubles use a tolerance - but it is sufficient for our
     * purposes.
     */
    private fun iotaListsAreEqual(a: List<Iota>, b: List<Iota>): Boolean {
        if (a.size == b.size) {
            return a.zip(b).all { pair -> Iota.tolerates(pair.first, pair.second) }
        }
        return false
    }
}