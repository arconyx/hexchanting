package gay.thehivemind.hexchanting

import at.petrak.hexcasting.api.casting.eval.ExecutionClientView
import at.petrak.hexcasting.api.casting.eval.SpecialPatterns
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.PatternIota
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.common.lib.HexBlocks
import at.petrak.hexcasting.common.lib.HexItems
import at.petrak.hexcasting.common.lib.hex.HexActions
import at.petrak.hexcasting.xplat.IXplatAbstractions
import com.mojang.authlib.GameProfile
import gay.thehivemind.hexchanting.casting.HexchantingPatterns
import gay.thehivemind.hexchanting.items.HexHolderEquipment
import gay.thehivemind.hexchanting.items.HexImbuedItem
import gay.thehivemind.hexchanting.items.HexchantingItems
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest
import net.minecraft.entity.EntityType
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.ItemEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.ClientConnection
import net.minecraft.network.NetworkSide
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.test.GameTest
import net.minecraft.test.GameTestException
import net.minecraft.test.PositionedException
import net.minecraft.test.TestContext
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.world.GameMode
import java.util.*
import java.util.function.Predicate

class HexchantingGameTest : FabricGameTest {
    val logger = Hexchanting.LOGGER
    val getCasterHex = actionsAsPatternIota(HexActions.GET_CASTER.prototype)

    /// IMBUING ITEMS ==================================================================================================

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    fun testImbuingArrow(context: TestContext) {
        val player = createMockSurvivalServerPlayerInWorld(context)

        // Attempt with no media, expected to fail
        val attempt = imbueItemUsingStaff(HexchantingItems.HEX_ARROW, player, getCasterHex)
        context.assertFalse(attempt.resolutionType.success, "Imbuing succeeded despite insufficient media")
        player.inventory.clear()

        // Attempt with media, expected to succeed
        // We have to avoid inserting into slot 0 because it will be overwritten by adding to the mainhand
        player.inventory.insertStack(33, HexItems.CHARGED_AMETHYST.defaultStack.copyWithCount(9))
        imbueItemUsingStaffSuccessfully(HexchantingItems.HEX_ARROW, player, context)
        context.complete()
        player.onDisconnect()
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    fun testImbuingAxe(context: TestContext) {
        val player = context.createMockCreativeServerPlayerInWorld()
        imbueItemUsingStaffSuccessfully(HexchantingItems.HEX_AXE, player, context)
        context.complete()
        player.onDisconnect()
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    fun testImbuingHoe(context: TestContext) {
        val player = context.createMockCreativeServerPlayerInWorld()
        imbueItemUsingStaffSuccessfully(HexchantingItems.HEX_HOE, player, context)
        context.complete()
        player.onDisconnect()
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    fun testImbuingPickaxe(context: TestContext) {
        val player = context.createMockCreativeServerPlayerInWorld()
        imbueItemUsingStaffSuccessfully(HexchantingItems.HEX_PICKAXE, player, context)
        context.complete()
        player.onDisconnect()
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    fun testImbuingShovel(context: TestContext) {
        val player = context.createMockCreativeServerPlayerInWorld()
        imbueItemUsingStaffSuccessfully(HexchantingItems.HEX_SHOVEL, player, context)
        context.complete()
        player.onDisconnect()
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    fun testImbuingSword(context: TestContext) {
        val player = context.createMockCreativeServerPlayerInWorld()
        imbueItemUsingStaffSuccessfully(HexchantingItems.HEX_SWORD, player, context)
        context.complete()
        player.onDisconnect()
    }


    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    fun testImbuingHelmet(context: TestContext) {
        val player = context.createMockCreativeServerPlayerInWorld()
        imbueItemUsingStaffSuccessfully(HexchantingItems.HEX_HELMET, player, context)
        context.complete()
        player.onDisconnect()
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    fun testImbuingChestplate(context: TestContext) {
        val player = context.createMockCreativeServerPlayerInWorld()
        imbueItemUsingStaffSuccessfully(HexchantingItems.HEX_CHESTPLATE, player, context)
        context.complete()
        player.onDisconnect()
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    fun testImbuingLeggings(context: TestContext) {
        val player = context.createMockCreativeServerPlayerInWorld()
        imbueItemUsingStaffSuccessfully(HexchantingItems.HEX_LEGGINGS, player, context)
        context.complete()
        player.onDisconnect()
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    fun testImbuingBoots(context: TestContext) {
        val player = context.createMockCreativeServerPlayerInWorld()
        imbueItemUsingStaffSuccessfully(HexchantingItems.HEX_BOOTS, player, context)
        context.complete()
        player.onDisconnect()
    }

    /// CASTING TRIGGERS ===============================================================================================

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    fun testHelmetTrigger(context: TestContext) {
        // Setup parameters
        val item = HexchantingItems.HEX_HELMET
        val imbuedItem = imbueItem(
            item as HexImbuedItem, actionsAsPatternIota(
                HexActions.GET_CASTER.prototype,
                HexActions.`ENTITY_POS$FOOT`.prototype, HexActions.CONJURE_BLOCK.prototype
            )
        )
        val offset = BlockPos(2, 2, 2)

        // Check initial conditions
        context.dontExpectBlock(HexBlocks.CONJURED_BLOCK, offset)

        // Setup player
        val player = createMockSurvivalServerPlayerInWorld(context)
        val pos = context.getAbsolutePos(offset).toCenterPos()
        player.teleport(pos.x, pos.y, pos.z)
        player.equipStack(EquipmentSlot.HEAD, imbuedItem)
        val armourIndex = 3
        val initialArmourItem = player.inventory.armor[armourIndex]
        context.assertTrue(
            initialArmourItem.item == item && initialArmourItem.damage == 0,
            "Helmet is missing or damaged before casting. Armour list is ${player.inventory.armor}, we checked index $armourIndex."
        )

        // Trigger hex
        val mob = context.spawnMob(EntityType.ZOMBIE, offset.add(2, 2, 2))
        // tick the mob so targeting occurs
        // it seems to be flaky with only one tick so we do several
        for (i in 1..20) {
            mob.tick()
        }

        // Check expected outcome
        context.expectBlock(HexBlocks.CONJURED_BLOCK, offset)
        val finalArmourItem = player.inventory.armor[armourIndex]
        val expectedDamage = MediaConstants.DUST_UNIT / (item as HexHolderEquipment).getDamageToMediaConversionFactor()
        context.assertTrue(
            finalArmourItem.item == item && finalArmourItem.damage.toLong() == expectedDamage,
            "Helmet durability has changed by an unexpected amount (got ${finalArmourItem.damage}, expected ${expectedDamage})"
        )

        // Mark success and clean up
        context.complete()
        player.onDisconnect()
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    fun testChestplateTrigger(context: TestContext) {
        // Setup parameters
        val item = HexchantingItems.HEX_CHESTPLATE
        val imbuedItem = imbueItem(
            item as HexImbuedItem, actionsAsPatternIota(
                HexActions.GET_CASTER.prototype,
                HexActions.`ENTITY_POS$FOOT`.prototype, HexActions.CONJURE_BLOCK.prototype
            )
        )
        val offset = BlockPos(2, 2, 2)

        // Check initial conditions
        context.dontExpectBlock(HexBlocks.CONJURED_BLOCK, offset)

        // Setup player
        val player = createMockSurvivalServerPlayerInWorld(context)
        // The player gets 60 ticks of invulnerability on joining, which we need to clear so they can take damage
        for (i in 0..60) {
            player.tick()
        }
        val pos = context.getAbsolutePos(offset).toCenterPos()
        player.teleport(pos.x, pos.y, pos.z)
        player.equipStack(EquipmentSlot.CHEST, imbuedItem)
        val armourIndex = 2
        val initialArmourItem = player.inventory.armor[armourIndex]
        context.assertTrue(
            initialArmourItem.item == item && initialArmourItem.damage == 0,
            "Chestplate is missing or damaged before casting. Armour list is ${player.inventory.armor}, we checked index $armourIndex."
        )

        // Trigger hex
        player.damage(player.damageSources.fall(), 5.0F)

        // Check expected outcome
        context.expectBlock(HexBlocks.CONJURED_BLOCK, offset)
        val finalArmourItem = player.inventory.armor[armourIndex]
        val expectedDamage = MediaConstants.DUST_UNIT / (item as HexHolderEquipment).getDamageToMediaConversionFactor()
        context.assertTrue(
            finalArmourItem.item == item && finalArmourItem.damage.toLong() == expectedDamage,
            "Chestplate durability has changed by an unexpected amount (got ${finalArmourItem.damage}, expected ${expectedDamage})"
        )

        // Mark success and clean up
        context.complete()
        player.onDisconnect()
    }


    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    fun testLeggingsTrigger(context: TestContext) {
        // Setup parameters
        val item = HexchantingItems.HEX_LEGGINGS
        val imbuedItem = imbueItem(
            item as HexImbuedItem, actionsAsPatternIota(
                HexActions.GET_CASTER.prototype,
                HexActions.`ENTITY_POS$FOOT`.prototype, HexActions.CONJURE_BLOCK.prototype
            )
        )
        val offset = BlockPos(2, 2, 2)

        // Check initial conditions
        context.dontExpectBlock(HexBlocks.CONJURED_BLOCK, offset)

        // Setup player
        val player = createMockSurvivalServerPlayerInWorld(context)
        // The player gets 60 ticks of invulnerability on joining, which we need to clear so they can take damage
        for (i in 0..60) {
            player.tick()
        }
        val pos = context.getAbsolutePos(offset).toCenterPos()
        player.teleport(pos.x, pos.y, pos.z)
        player.equipStack(EquipmentSlot.HEAD, imbuedItem)
        val armourIndex = 3
        val initialArmourItem = player.inventory.armor[armourIndex]
        context.assertTrue(
            initialArmourItem.item == item && initialArmourItem.damage == 0,
            "Leggings are missing or damaged before casting. Armour list is ${player.inventory.armor}, we checked index $armourIndex."
        )

        // Trigger hex
        player.damage(player.damageSources.fall(), 25.0F)

        // Check expected outcome
        context.assertTrue(player.isDead, "Player is not dead")
        context.expectBlock(HexBlocks.CONJURED_BLOCK, offset)
        val finalArmourItem = expectAndReturnItemAt(context, item, offset, 2.0)
        val expectedDamage = MediaConstants.DUST_UNIT / (item as HexHolderEquipment).getDamageToMediaConversionFactor()
        context.assertTrue(
            finalArmourItem.item == item && finalArmourItem.damage.toLong() == expectedDamage,
            "Leggings durability has changed by an unexpected amount (got ${finalArmourItem.damage}, expected ${expectedDamage})"
        )

        // Mark success and clean up
        context.complete()
        player.onDisconnect()
    }

    /// UTILITIES ======================================================================================================

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


    /**
     * Spawn the given item in the player's offhand and imbue it with the given hex. Returns the casting result.
     */
    private fun imbueItemUsingStaff(item: Item, player: ServerPlayerEntity, hex: List<Iota>): ExecutionClientView {
        // Equip staff and item to imbue
        player.equipStack(EquipmentSlot.MAINHAND, HexItems.STAFF_OAK.defaultStack)
        player.equipStack(EquipmentSlot.OFFHAND, item.defaultStack)

        // Cast imbuing hex with staff
        val vm = IXplatAbstractions.INSTANCE.getStaffcastVM(player, Hand.OFF_HAND)

        val iotas = makeImbueIotaList(hex)
        val castingResult = vm.queueExecuteAndWrapIotas(iotas, player.serverWorld)
        return castingResult
    }

    /**
     * Simulate imbuing a hex into a Hexchanting item using staff casting and assert that it succeeds.
     *
     * Returns the imbued item stack.
     */
    private fun imbueItemUsingStaffSuccessfully(
        item: Item,
        player: ServerPlayerEntity,
        context: TestContext
    ): ItemStack? {
        val hexToImbue: List<Iota> = getCasterHex
        val castingResult = imbueItemUsingStaff(item, player, hexToImbue)
        context.assertTrue(castingResult.resolutionType.success, "Imbuing a hex into the item failed")

        // Retrieve and verify imbued item
        logger.debug(
            "Offhand item is {} with nbt {} and count {}",
            player.offHandStack.item,
            player.offHandStack.nbt,
            player.offHandStack.count
        )
        val imbuedItem = player.offHandStack
        context.assertTrue(imbuedItem.item == item, "Offhand item does not match expected item type")
        val hexHolderItem =
            imbuedItem.item as? HexImbuedItem ?: throw GameTestException("Imbued item can't be cast to HexImbuedItem")
        val imbuedHex = hexHolderItem.getHex(imbuedItem, player.serverWorld)
            ?: throw GameTestException("Cannot retrieve hex from HexImbuedItem")
        logger.debug(
            "Imbued hex is {}, expected hex is {}",
            imbuedHex.first().serialize(),
            hexToImbue.first().serialize()
        )
        context.assertTrue(iotaListsAreEqual(imbuedHex, hexToImbue), "Imbued hex differs from expected hex")
        return imbuedItem
    }

    /**
     * Change a player's game mode, asserting that the final game mode matches the input
     *
     * This is modified from the [TestContext.createMockCreativeServerPlayerInWorld] method. I am unsure as to why there
     * is no such method already. Maybe this will break something, but we will hope not...
     */
    fun createMockSurvivalServerPlayerInWorld(context: TestContext): ServerPlayerEntity {
        val player = ServerPlayerEntity(
            context.world.server, context.world, GameProfile(UUID.randomUUID(), "test-mock-survival-player")
        )
        context.world.server.playerManager
            .onPlayerConnect(ClientConnection(NetworkSide.SERVERBOUND), player)
        player.changeGameMode(GameMode.SURVIVAL)
        context.assertTrue(
            player.interactionManager.gameMode == GameMode.SURVIVAL,
            "GameMode is expected to be survival, but is not"
        )
        return player
    }

    /**
     * Create a stack of the given item and imbue it with the given iota list. This directly edits the item without
     * simulating casting of the imbuement hex.
     */
    private fun imbueItem(item: HexImbuedItem, hex: List<Iota>): ItemStack {
        val stack = (item as Item).defaultStack;
        item.writeHex(stack, hex, null, 0)
        return stack
    }

    /**
     * This is a modification of [TestContext.expectItemAt] that returns the found item stack.
     */
    fun expectAndReturnItemAt(context: TestContext, item: Item, pos: BlockPos?, radius: Double): ItemStack {
        val blockPos: BlockPos = context.getAbsolutePos(pos)

        for (entity in context.world.getEntitiesByType(
            EntityType.ITEM,
            Box(blockPos).expand(radius),
            Predicate { obj: ItemEntity? -> obj?.isAlive ?: false })) {
            val itemEntity = entity as ItemEntity
            if (itemEntity.stack.item == item) {
                return itemEntity.stack
            }
        }

        throw PositionedException(
            "Expected ${item.name.string} item",
            blockPos,
            pos,
            context.tick
        )
    }
}