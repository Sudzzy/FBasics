package org.originmc.fbasics.entity;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class CommandEditor {

    private static final List<CommandEditor> commandEditors = new ArrayList<>();
    private final double price;
    private final int cooldown;
    private final int warmup;
    private final String alias;
    private final String editor;
    private final String regex;
    private final String permission;
    private final List<String> factions;
    private final List<Material> blocks = new ArrayList<>();

    public CommandEditor(FileConfiguration config, String editor) {
        this.editor = editor;
        this.price = config.getDouble("commands.editors." + editor + ".price");
        this.warmup = config.getInt("commands.editors." + editor + ".warmup");
        this.cooldown = config.getInt("commands.editors." + editor + ".cooldown");
        this.alias = config.getString("commands.editors." + editor + ".alias");
        this.regex = config.getString("commands.editors." + editor + ".match");
        this.permission = config.getString("commands.editors." + editor + ".permission");
        this.factions = config.getStringList("commands.editors." + editor + ".factions");

        for (String material : config.getStringList("commands.editors." + editor + ".blocks")) {
            this.blocks.add(Material.getMaterial(material));
        }

        commandEditors.add(this);
    }

    public static CommandEditor getByCommand(String command) {
        for (CommandEditor commandEditor : commandEditors) {
            if (command.matches(commandEditor.getRegex())) {
                return commandEditor;
            }
        }
        return null;
    }

    public static CommandEditor getByEditor(String editor) {
        for (CommandEditor commandEditor : commandEditors) {
            if (commandEditor.toString().equals(editor)) {
                return commandEditor;
            }
        }
        return null;
    }

    public double getPrice() {
        return this.price;
    }

    public int getCooldown() {
        return this.cooldown;
    }

    public int getWarmup() {
        return this.warmup;
    }

    public String getAlias() {
        return this.alias;
    }

    public String getRegex() {
        return this.regex;
    }

    public String getPerm() {
        return this.permission;
    }

    public List<String> getFactions() {
        return this.factions;
    }

    public List<Material> getBlocks() {
        return this.blocks;
    }

    @Override
    public String toString() {
        return editor;
    }
}
