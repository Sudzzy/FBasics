package org.originmc.fbasics.settings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.originmc.fbasics.FBasics;

public class SettingsManager {

	private FBasics plugin;
	public SettingsManager(FBasics plugin) {
		this.plugin = plugin;
	}


	private File configFile;
	private File languageFile;
	private static FileConfiguration config;
	private static FileConfiguration language;


	public void updateFiles() {

		String version = config.getString("version");


		if (version != null && version.equals(this.plugin.getDescription().getVersion())) {
			return;
		}


		if (version == null) {
			version = config.getString("Version").replace(".", "-");
		} else {
			version = version.replace(".", "-");
		}


		File oldConfigFile = new File(this.plugin.getDataFolder(), "old-config-" + version + ".yml");
		File oldLanguageFile = new File(this.plugin.getDataFolder(), "old-language-" + version + ".yml");

		this.configFile.renameTo(oldConfigFile);
		this.languageFile.renameTo(oldLanguageFile);

		getFiles();
	}


	public void getFiles() {

		this.configFile = new File(this.plugin.getDataFolder(), "config.yml");
		this.languageFile = new File(this.plugin.getDataFolder(), "language.yml");


		try {
			writeFiles();
		}

		catch (IOException e) {

			this.plugin.getLogger().severe("Plugin unable to write configuration files!");
			e.printStackTrace();

		}


		config = new YamlConfiguration();
		language = new YamlConfiguration();


		loadFiles();
	}


	private void writeFiles() throws IOException {

		if (!this.configFile.exists()) {

			this.configFile.getParentFile().mkdirs();
			copy(this.plugin.getResource("config.yml"), this.configFile);

		}


		if (!this.languageFile.exists()) {

			this.languageFile.getParentFile().mkdirs();
			copy(this.plugin.getResource("language.yml"), this.languageFile);

		}
	}


	private void copy(InputStream in, File file) throws IOException {

		OutputStream out = new FileOutputStream(file);

		byte[] buf = new byte[1024];

		int len;

		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}

		out.close();
		in.close();
	}


	public void loadFiles() {
		try {
			config.load(configFile);
			language.load(languageFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static FileConfiguration getConfig() {
		return config;
	}


	public static FileConfiguration getLanguage() {
		return language;
	}
}