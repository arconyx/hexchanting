package gay.thehivemind.hexchanting

import gay.thehivemind.hexchanting.casting.HexchantingPatterns
import gay.thehivemind.hexchanting.config.HexchantingProperties
import gay.thehivemind.hexchanting.items.HexchantingItems
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Hexchanting : ModInitializer {
    const val MOD_ID = "hexchanting"
    val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)
    lateinit var CONFIG: HexchantingProperties

    override fun onInitialize() {
        this.CONFIG = HexchantingProperties.fromPath(
            FabricLoader.getInstance().configDir.resolve("hexchanting.properties"), copyDefault = true)

        // call to init some registries

        @Suppress("UnusedExpression")
        HexchantingItems
        @Suppress("UnusedExpression")
        HexchantingPatterns
    }
}