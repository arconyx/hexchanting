# Unreleased
### Breaking Changes
- Bump minimum Hex Casting version to 0.11.3

### Other Changes
- Version constraints on runtime dependencies in `fabric.mod.json` have been
tightened to exclude unlikely and probably broken combinations. The Minecraft
version has been locked to 1.20.1 and Fabric Language Kotlin has been limited
to versions <2.0. This should not break existing installations provided they use
Hex Casting 0.11.3.

# v1.2.0
You can now control whether the chestplate damage trigger fires before or after
the damage is applied to the player.

### Before
Before is the old behaviour and remains the default. The hex is triggered and
runs before damage is actually applied to the player. This permits tricks like
applying a precise amount of absorption.

### After
After is the new behaviour. It must be opted-into so as not to break existing
hexes. The damage is applied to the player and only then does the hex fire. This
prevents simple immortality hexes. The damage passed to the hex is the damage
taken after factors like armour and absorption are applied.

### Configuration
The behaviour is controlled by a configuration flag in the file
`hexchanting.properties` in your mod configuration directory. Changing this
flag will affect all hexes in new and existing worlds. It is technically safe to
update this value without creating a new world, however hexes relying on the old
triggering behaviour may no longer work as intended.

# v1.1.7
Fix leggings crash ([#15](https://github.com/arconyx/hexchanting/issues/15))

# v1.1.6
Translation updates for zh_cn by ChuijkYahus 

# v1.1.5
This release is identical to 1.1.4 and only exists because 1.1.4 failed to push
to Modrinth properly.

# v1.1.4
- zh_cn translation updates by ChuijkYahus
- en_us hexbook now follows community style conventions more closely
- en_us typo fixes
- Builds are now reproducible

# v1.1.3
zh_cn translation updates by ChuijkYahus

# v1.1.2
- Don't double count media in inventory during cost simulation
- Relax Fabric Language Kotlin dependency, allowing for Quilt compatibility.
- Move chestplate trigger to earlier in damage application, so absorption
doesn't bypass it.

# v1.1.1
- Store arrow media on arrow instead of casting env
- Arrows can no longer be picked up after being fired
- Hex book unlocks on the hexcasting:root advancement
- Arrows push block pos instead of null when hitting a block.

# v1.1.0
## Breaking Changes
Armour and arrow triggers have been changed.
### Arrows
Arrows now also cast when hitting a block, pushing null in place of the entity.
### Helmet
Helmets now trigger when a mob begins targeting the player.
### Chestplate
Chests trigger when the player takes damage, as before. Position has been
removed from the iotas pushed to the stack.
### Leggings
Leggings cast on death. They do not have access to the player's media so always
use durability.
### Boots
Boots now cast when the player has fallen a certain distance.

## Other Changes
- Add mod icon
- Lower imbuing cost
- Armour trims can now be used on amethyst armour
- Add missing items tags to tools and sword
- Display a Hexxy tooltip on imbued items

# v1.0.1
- Add Chinese (zh_cn) translations by ChuijkYahus
- Cleanup some logging and documentation

# v1.0.0
Initial release. Tools cast on use, armour casts when the player is damaged.
