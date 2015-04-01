# FBasics

*Prevents all kinds of exploits and glitches within factions servers*

## Supports
* CraftBukkit & Spigot 1.7.9
* CraftBukkit & Spigot 1.7.10
* Spigot 1.7.10 / 1.8 Protocol Patch
* CraftBukkit & Spigot 1.8.0
* CraftBukkit & Spigot 1.8.3

## Features

* Patches many issues found on factions servers
  * Glitching through walls with horses, boats, and even Essentials' safe teleportation
  * Promoting players via Buycraft and Enjin with a safety catch
  * Teleporting through walls with enderpearls or to the top of the nether
  * Duplicating crops by placing doors and other materials next to them
  * Duplicating ores with mcMMO
  * Duplicating consumables by blowing up inventory blocks
  * Every single V-Clip and Phase hack through solid walls is blocked
* Supports both Vault 1.4.x and Vault 1.5.x
* Supports Factions for checking territories with commands, wilderness and enderpearls
  * _Most mainstream Faction builds are supported:_
  * Factions 2.7 (MassiveCore 2.7)
  * Factions 2.6 (MassiveCore 7.4)
  * FactionsUUID 1.6 by @drtshock
  * FactionsUUID 1.8 by @externo6
* Advanced command editing
  * Aliasing
  * Warmups (Cancelled on damage and movement)
  * Cooldowns
  * RegEx support
  * Blocking commands while in faction territory
  * Blocking commands while in specific block
  * Prices
  * Add your own permissions
* Other useful commands for factions servers:
  * Crates
  * Wilderness (Factions support)

## Development Builds
Obtain the latest compiled version of FBasics here: [Download Here](https://github.com/Sudzzy/FBasics/raw/master/FBasics/target/FBasics.jar "Download Here")
Please stick to normal released builds via Spigot if you wish to maintain a stable server, these builds are purely for testing.

## Commands

|Command|Description|
| ------------- | ------------- |
|/crate balance|View your own balance|
|/crate balance [player]|View the balance of another player|
|/crate open|Opens one of your crates|
|/crate add [player] [amount]|Give a player some crates|
|/crate pay [player] [amount]|Pay a player come crates|
|/crate remove [player] [amount]|Remove crates from a player|
|/crate set [player] [amount]|Set the amount of crates a player has|
|/fbasics|Displays the help page|
|/fbasics reload|Reloads the plugin|
|/fbasics version|View the current version you are using|
|/wilderness|Teleports the player to a random location|
|/safepromote [player] [old rank] [new rank] [world]|Promotes a player using group checks|

## Permissions

|Permission|Description|Default|
| ------------- | ------------- | ------------- |
|fbasics.bypass.antiloot|Bypass loot protection|operator|
|fbasics.bypass.boat|Bypass boat exploit protection|operator|
|fbasics.bypass.booklimiter|Bypass book exploit protection|operator|
|fbasics.bypass.command.blocks|Bypass command block limits|operator|
|fbasics.bypass.command.cooldowns|Bypass command cooldowns|operator|
|fbasics.bypass.command.economy|Bypass command fees|operator|
|fbasics.bypass.command.glitchable|Bypass blocked commands listed in config|operator|
|fbasics.bypass.command.territory|Bypass command territory limits|operator|
|fbasics.bypass.command.warmup|Bypass command warmups|operator|
|fbasics.bypass.dismount|Bypass dismount glitch protection|operator|
|fbasics.bypass.enderpearl|Bypass enderpearl glitch protection|operator|
|fbasics.bypass.nether|Bypass nether glitch protection|operator|
|fbasics.bypass.phase|Bypass V-Clip protection|operator|
|fbasics.command.crate|Use the crates command|everyone|
|fbasics.command.crate.balance|View crates balance|everyone|
|fbasics.command.crate.balance.other|View other players crates balance|everyone|
|fbasics.command.crate.change|Give / take / set players crates|operator|
|fbasics.command.crate.open|Open a crate|everyone|
|fbasics.command.crate.pay|Pay other players in crates|everyone|
|fbasics.command.fbasics|Use the default "/fbasics" command|everyone|
|fbasics.command.reload|Reload the plugin|operator|
|fbasics.command.safepromote|Safely promote a player|operator|
|fbasics.command.wilderness|Teleport to a random place in the wilderness|everyone|