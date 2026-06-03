package gay.thehivemind.hexchanting

import net.minecraft.test.GameTest
import net.minecraft.test.TestContext
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest
import net.minecraft.block.Blocks

class HexchantingGameTest : FabricGameTest {
    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    fun test(context: TestContext) {
        context.expectBlock(Blocks.STRUCTURE_BLOCK, 0, 0, 0);
        context.complete()
    }
}