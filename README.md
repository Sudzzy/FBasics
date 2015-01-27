## What is FBasics?

FBasics stands for "Factions Basics". The plugin provides you with an essential selection of features, designed for factions servers. It was initially developed to prevent all glitches / exploits that arise within my own factions server (OriginMC). Releasing this plugin is basically my little thank you to the community for helping me start out. Hopefully a few of you can benefit off this just as much as I have!

## [Download Latest Development Build](https://github.com/Sudzzy/FBasics/raw/master/target/FBasics.jar "Download Latest Development Build")

## Features

- Many glitches patched - including:
  - Horse glitches 
  - Enderpearl glitching (Factions support)
  - Glitching to the top of the nether
  - Cactus duplication glitch
  - Essentials' teleportation glitches
  - Buycraft / Enjin promotion glitches (With Safe Promote)
  - McMMO duplication glitch
  - TNT/Chest duplication glitches

- Advanced command editing:
  - Aliasing
  - Warmups (Cancelled on damage and movement)
  - Cooldowns
  - RegEx support
  - Blocking commands while in faction territory
  - Blocking commands while in specific block
  - Prices
  - Add your own permissions

- Other useful commands for factions servers:
  - Crates
  - Wilderness (Factions support)

## Supported Plugins (Latest version)
- FactionsUUID 1.6.9.5 by DrtShock
- Factions 2.6.0
- Factions 2.7.5
- MassiveCore 7.4.0 (Old)
- MassiveCore 2.7.5 (New)
- Vault 1.4.1
- Vault 1.5.2

## Supported Server Versions (Latest version)
- 1.7.2 -> 1.7.10 Craftbukkit
- 1.7 / 1.8 Spigot Protocol hack
- 1.8.1 Spigot

##Commands

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

##Admin Permissions

|Permission|Description|
| ------------- | ------------- |
|fbasics.admin|All admin permission nodes - Granted to Ops|
|fbasics.bypass.antilooter|Bypasses loot protection|
|fbasics.bypass.commands.block|Bypass commands blocks settings|
|fbasics.bypass.commands.cooldowns|Bypass commands cooldowns settings|
|fbasics.bypass.commands.economy|Bypass commands payment settings|
|fbasics.bypass.commands.glitchable|Bypass glitchable commands (see config)|
|fbasics.bypass.commands.territory|Bypass commands factions settings|
|fbasics.bypass.commands.warmup|Bypass commands warmup settings|
|fbasics.bypass.glitch.boat|Bypass boat movement patch|
|fbasics.bypass.glitch.dismount|Bypass dismount patch|
|fbasics.bypass.glitch.enderpearl|Bypass enderpearl patch|
|fbasics.bypass.glitch.nether|Bypass nether patch|
|fbasics.commands.crate.change|Access to change player crates|
|fbasics.commands.crate.change|Access to change player crates|
|fbasics.commands.reload|Access to reload the plugin|

##User Permissions

|Permission|Description|
| ------------- | ------------- |
|fbasics.user|All user permission nodes - Granted to everyone|
|fbasics.commands.crate.balance|Access to view your own balance|
|fbasics.commands.crate.balance.other|Access to view balance of other players|
|fbasics.commands.crate.open|Access to open your own crates|
|fbasics.commands.crate.pay|Access to pay another user with crates|
|fbasics.commands.wilderness|Access to use the wilderness command|
