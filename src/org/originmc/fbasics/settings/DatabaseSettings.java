package org.originmc.fbasics.settings;


public class DatabaseSettings {

	public static String url;
	public static Boolean mysql;
	public static String user;
	public static String pass;
	public static String name;
	public static String prefix;
	public static String port;
	public static String host;


	public static void loadCommandSettings() {

		mysql = Boolean.valueOf(SettingsManager.getConfig().getBoolean("mysql.enabled"));
		user = SettingsManager.getConfig().getString("mysql.user-name");
		pass = SettingsManager.getConfig().getString("mysql.user-password");
		name = SettingsManager.getConfig().getString("mysql.database-name");
		prefix = SettingsManager.getConfig().getString("mysql.table-prefix");
		port = SettingsManager.getConfig().getString("mysql.port");
		host = SettingsManager.getConfig().getString("mysql.address");

	}
}