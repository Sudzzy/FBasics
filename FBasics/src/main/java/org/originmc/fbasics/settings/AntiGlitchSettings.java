package org.originmc.fbasics.settings;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.task.AntiPhaseTask;

import java.util.ArrayList;
import java.util.List;

import static org.originmc.fbasics.util.SettingsUtils.getMaterialList;

@Data
public final class AntiGlitchSettings implements ISettings {

    private static final String PREFIX = "antiglitch-";

    private static final String PHASE = PREFIX + "phase";

    private static final String FACTION_MAP = PREFIX + "faction-map";

    private static final String DISMOUNT_CLIPPING = PREFIX + "dismount-clipping";

    private static final String NETHER_ROOF = PREFIX + "nether-roof";

    private static final String NETHER_ROOF_MESSAGE = PREFIX + "nether-roof-message";

    private static final String BONEMEAL_DISPENSERS = PREFIX + "bonemeal-dispensers";

    private static final String MCMMO_MINING = PREFIX + "mcmmo-mining";

    private static final String MCMMO_MINING_BLOCKS = PREFIX + "mcmmo-mining-blocks";

    private static final String INVENTORY_DUPE = PREFIX + "inventory-dupe";

    private static final String CROP_DUPE = PREFIX + "crop-dupe";

    private static final String CROP_DUPE_MESSAGE = PREFIX + "crop-dupe-message";

    private static final String CROP_DUPE_CROP_BLOCKS = PREFIX + "crop-dupe-crop-blocks";

    private static final String CROP_DUPE_DENY_BLOCKS = PREFIX + "crop-dupe-deny-blocks";

    private static final String BOOK_LIMIT = PREFIX + "book-limit";

    private static final String BOOK_LIMIT_MESSAGE = PREFIX + "book-limit-message";

    private final FBasics plugin;

    private final AntiGlitchEnderpearlsSettings enderpearls;

    private final AntiPhaseTask phaseTask;

    private ConfigurationSection configuration;

    private boolean phase = false;

    private int phaseTaskId = 0;

    private boolean factionMap = false;

    private boolean dismountClipping = false;

    private boolean netherRoof = false;

    private String netherRoofMessage = "";

    private boolean bonemealDispensers = false;

    private boolean mcmmoMining = false;

    private List<Material> mcmmoMiningBlocks = new ArrayList<>();

    private boolean inventoryDupe = false;

    private boolean cropDupe = false;

    private String cropDupeMessage = "";

    private List<Material> cropDupeCropBlocks = new ArrayList<>();

    private List<Material> cropDupeDenyBlocks = new ArrayList<>();

    private int bookLimit = 0;

    private String bookLimitMessage = "";

    public AntiGlitchSettings(FBasics plugin) {
        this.plugin = plugin;
        this.enderpearls = new AntiGlitchEnderpearlsSettings(plugin);
        this.phaseTask = new AntiPhaseTask(plugin);
    }

    @Override
    public void load() {
        configuration = plugin.getConfig();
        phase = configuration.getBoolean(PHASE, false);

        if (phaseTaskId != 0) {
            Bukkit.getScheduler().cancelTask(phaseTaskId);
            phaseTaskId = 0;
        }

        if (phase) {
            phaseTaskId = Bukkit.getScheduler().runTaskTimer(plugin, phaseTask, 1, 1).getTaskId();
        }

        factionMap = configuration.getBoolean(FACTION_MAP, false);
        dismountClipping = configuration.getBoolean(DISMOUNT_CLIPPING, false);
        netherRoof = configuration.getBoolean(NETHER_ROOF, false);
        netherRoofMessage = configuration.getString(NETHER_ROOF_MESSAGE, "");
        bonemealDispensers = configuration.getBoolean(BONEMEAL_DISPENSERS, false);
        mcmmoMining = configuration.getBoolean(MCMMO_MINING, false);
        mcmmoMiningBlocks = getMaterialList(configuration.getStringList(MCMMO_MINING_BLOCKS));
        inventoryDupe = configuration.getBoolean(INVENTORY_DUPE, false);
        cropDupe = configuration.getBoolean(CROP_DUPE, false);
        cropDupeMessage = configuration.getString(CROP_DUPE_MESSAGE, "");
        cropDupeCropBlocks = getMaterialList(configuration.getStringList(CROP_DUPE_CROP_BLOCKS));
        cropDupeDenyBlocks = getMaterialList(configuration.getStringList(CROP_DUPE_DENY_BLOCKS));
        bookLimit = configuration.getInt(BOOK_LIMIT);
        bookLimitMessage = configuration.getString(BOOK_LIMIT_MESSAGE, "");
        enderpearls.load();
    }

}
