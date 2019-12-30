package xyz.fluxinc.chatpronouns;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ConfigurationManager<Plugin extends JavaPlugin> {

    YamlConfiguration configuration;
    Plugin instance;

    /**
     * A Class used for managing configuration files within a FluxCore plugin
     * @param instance The plugin that is being used
     * @param configuration The name of the configuration file
     */
    public ConfigurationManager(Plugin instance, String configuration) {
        YamlConfiguration config = new YamlConfiguration();
        File configFile = new File(instance.getDataFolder(), configuration);
        if (!configFile.exists()) {
            instance.saveResource(configuration, false);
        }
        try { config.load(configFile); }
        catch (InvalidConfigurationException | IOException e) { e.printStackTrace(); Bukkit.getPluginManager().disablePlugin(instance); }
        this.configuration = config;
        this.instance = instance;
    }

    /**
     * Returns the configuration file that is being managed
     * @return The configuration file
     */
    public YamlConfiguration getConfig() { return this.configuration; }

}
