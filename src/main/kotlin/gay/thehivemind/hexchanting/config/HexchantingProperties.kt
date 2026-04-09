package gay.thehivemind.hexchanting.config

import gay.thehivemind.hexchanting.Hexchanting
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

/**
 * This holds configuration for Hexchanting.
 *
 * It is static during runtime and assumed to be loaded at launch from a properties
 * file using the associated [fromPath] function.
 *
 * Configuration values are set independently of the world save and so may change during the
 * lifetime of the world (but not while it is running). Any issues that may occur from this should be documented
 * in the configuration file.
 *
 * The default configuration file lives in the mod resources folder.
 */
data class HexchantingProperties(
    /**
     * The amethyst chestplate triggers a hex when the player is damaged.
     * When `true` the damage is applied to the player and then the hex is triggered. The damage value provided to the
     * hex has armour, enchantments and absorption applied.
     * When `false` the hex is triggered and then damage is applied. This permits tricks like applying absorption
     * to negate the damage. Modifiers to the damage are not applied to the damage value sent to the hex.
     */
    val applyDamageBeforeChestplateTrigger: Boolean,
) {
    companion object {
        /**
         * Load configuration from a Java `properties` file at the given path and translate it to our configuration
         * object. If this file does not exist then the default configuration is used.
         *
         * @param copyDefault Whether to copy the default properties file to the given path if it doesn't already exist.
         */
        fun fromPath(path: Path, copyDefault: Boolean): HexchantingProperties {
            val properties = loadPropertiesFile(path, copyDefault)

            return when (val version = getOptionalLong(properties, "version", 1)) {
                1L -> HexchantingProperties(
                    applyDamageBeforeChestplateTrigger = getOptionalBoolean(properties, "applyDamageBeforeChestplateTrigger", false)
                )
                else -> throw RuntimeException("Invalid properties version $version. Accepted values: 1")
            }
        }

        /**
         * Handles the IO around loading a properties file. If loading fails an empty properties object is returned.
         *
         * Optionally creates a default properties file at the given path when no file is found.
         */
        private fun loadPropertiesFile(path: Path, copyDefault: Boolean): Properties {
            val properties = Properties()

            // If no properties file exists then copy one out of our resources
            if (copyDefault && Files.notExists(path)) {
                HexchantingProperties::class.java.classLoader.getResourceAsStream("hexchanting.properties")
                    .use { defaultPropertiesStream ->
                        if (defaultPropertiesStream == null) {
                            Hexchanting.LOGGER.error("Unable to find default properties resource.")
                        } else {
                            try {
                                Files.copy(defaultPropertiesStream, path)
                                Hexchanting.LOGGER.info("Copied default properties file to $path")
                            } catch (e: IOException) {
                                // We might fail because the config folder is read only, for example
                                // We don't want this to crash the application
                                Hexchanting.LOGGER.error(
                                    "Unable to copy default properties file to $path due to IOException",
                                    e
                                )
                            } catch (e: FileAlreadyExistsException) {
                                Hexchanting.LOGGER.warn(
                                    "Unable to copy default properties to $path because file already exists",
                                    e
                                )
                            } catch (e: Exception) {
                                throw RuntimeException(
                                    "Unable to copy default properties file to $path with unexpected cause",
                                    e
                                )
                            }
                        }

                    }
            }

            try {
                Files.newInputStream(path).use { propertiesStream ->
                    properties.load(propertiesStream)
                }
            } catch (e: IOException) {
                Hexchanting.LOGGER.warn("Unable to read config file due to IOException. Defaults will be used.", e)
            }

            return properties
        }

        /**
         * Extract the value of the key from the properties object as a `Long` if present, else the default.
         */
        private fun getOptionalLong(properties: Properties, key: String, default: Long): Long {
            val stringValue = properties.getProperty(key) ?: return default
            val intValue = stringValue.toLongOrNull()
                ?: throw RuntimeException("Unable to parse value $stringValue as a long integer for key $key in properties file")
            return intValue
        }

        /**
         * Extract the value of the key from the properties object as a `Boolean` if present, else the default.
         *
         * Only "true" and "false" are accepted (without quotes). Case-sensitive.
         */
        private fun getOptionalBoolean(properties: Properties, key: String, default: Boolean): Boolean {
            val stringValue = properties.getProperty(key) ?: return default
            val boolValue = stringValue.toBooleanStrictOrNull()
                ?: throw RuntimeException("Unable to parse value $stringValue as a long integer for key $key in properties file")
            return boolValue
        }
    }

}