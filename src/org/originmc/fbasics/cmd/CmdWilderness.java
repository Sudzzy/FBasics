package org.originmc.fbasics.cmd;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.entity.BoardColls;
import com.massivecraft.factions.entity.FactionColls;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.originmc.fbasics.FBasics;

import java.util.List;
import java.util.Random;

public class CmdWilderness implements CommandExecutor {

    private final int xCenter;
    private final int zCenter;
    private final int maxRange;
    private final int minRange;
    private final String messageFailed;
    private final String messageSuccess;
    private final String messageWorld;
    private final String messageConsole;
    private final String messagePermission;
    private final List<String> worlds;
    private final List<String> blocks;
    private final String permissionWilderness = "fbasics.commands.wilderness";

    public CmdWilderness(FBasics plugin) {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration language = plugin.getLanguage();
        String error = language.getString("general.error.prefix");
        String info = language.getString("general.info.prefix");
        this.xCenter = config.getInt("wilderness.x-center");
        this.zCenter = config.getInt("wilderness.z-center");
        this.maxRange = config.getInt("wilderness.max-range");
        this.minRange = config.getInt("wilderness.min-range");
        this.messageFailed = error + language.getString("wilderness.error.failed");
        this.messageSuccess = info + language.getString("wilderness.info.success");
        this.messageWorld = error + language.getString("wilderness.error.world");
        this.messageConsole = error + language.getString("general.error.console");
        this.messagePermission = error + language.getString("general.error.permission");
        this.worlds = config.getStringList("wilderness.whitelisted-worlds");
        this.blocks = config.getStringList("wilderness.disabled-blocks");
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageConsole));
            return true;
        }

        if (!sender.hasPermission(this.permissionWilderness)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messagePermission));
        }

        Player player = (Player) sender;
        World world = player.getWorld();

        if (!this.worlds.contains(world.getName())) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageWorld));
            return true;
        }

        for (int counter = 0; counter < 25; counter++) {

            Random r = new Random();
            int xRange = this.minRange + r.nextInt(this.maxRange - this.minRange);
            int zRange = this.minRange + r.nextInt(this.maxRange - this.minRange);
            int x = this.xCenter + xRange;
            int z = this.zCenter + zRange;

            if (teleportCheck(player, world, x, z)) {
                return true;
            }
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageFailed));
        return true;
    }

    private boolean teleportCheck(Player sender, World world, int z, int x) {
        int y = world.getHighestBlockYAt(x, z);
        Block highest = world.getBlockAt(x, y - 1, z);
        String blockname = highest.getType().toString();

        if (this.blocks.contains(blockname)) {
            return false;
        }

        if (Bukkit.getPluginManager().getPlugin("Factions") != null) {

            String version = Bukkit.getPluginManager().getPlugin("Factions").getDescription().getVersion();

            if (version.startsWith("1")) {
                FLocation flocation = new FLocation(highest.getLocation());
                if (!Board.getFactionAt(flocation).isNone()) {
                    return false;
                }
            }

            if (version.startsWith("2")) {
                com.massivecraft.factions.entity.Faction wilderness = FactionColls.get().getForWorld(world.getName()).getNone();
                com.massivecraft.factions.entity.Faction faction = BoardColls.get().getFactionAt(PS.valueOf(highest));

                if (faction != wilderness) {
                    return false;
                }
            }
        }

        teleportPlayer(sender, world, x, z);
        return true;
    }

    private boolean teleportPlayer(Player player, World world, int x, int z) {
        if (player == null) return true;

        int y = world.getHighestBlockYAt(x, z);
        player.teleport(new Location(world, x + 0.5D, y, z + 0.5D));
        String msg = this.messageSuccess.replace("{name}", player.getName()).replace("{X}", "" + x).replace("{Y}", "" + y).replace("{Z}", "" + z).replace("{WORLD}", world.getName());
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        return true;
    }
}