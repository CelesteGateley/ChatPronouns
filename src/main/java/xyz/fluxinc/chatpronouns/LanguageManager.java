package xyz.fluxinc.chatpronouns;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.fluxinc.fluxcore.configuration.ConfigurationManager;

import java.util.Map;

public class LanguageManager<Plugin extends JavaPlugin> extends ConfigurationManager<Plugin> {

    /**
     * A class used specifically for managing language files
     * @param plugin The plugin instance using the language file
     * @param langFile The name of the language file
     */
    public LanguageManager(Plugin plugin, String langFile) { super(plugin, langFile); }

    /**
     * Generate a message to be output into chat
     * @param key The language key
     * @param variables The variables to be replaced
     * @return The formatted message
     */
    public String generateMessage(String key, Map<String, String> variables) {
        String prefix = configuration.getString("prefix");
        String msg = configuration.getString(key);
        if (prefix == null || msg == null) { instance.getLogger().severe("Invalid Lang File, missing elements prefix or " + key); return ""; }
        for (Map.Entry<String,String> pair : variables.entrySet()) { msg = msg.replaceAll("%" + pair.getKey() + "%", pair.getValue()); }
        return ChatColor.translateAlternateColorCodes('&', prefix + " " + msg);
    }

    /**
     * Generate a message to be output into chat
     * @param key The language key
     * @return The formatted message
     */
    public String generateMessage(String key) {
        String prefix = configuration.getString("prefix");
        String msg = configuration.getString(key);
        if (prefix == null || msg == null) { instance.getLogger().severe("Invalid Lang File, missing elements prefix or " + key); return ""; }
        return ChatColor.translateAlternateColorCodes('&', prefix + " " + msg);
    }

    /**
     * Returns a raw config key with colors, but no prefix
     * @param key The key to return
     * @return The semi-formatted string
     */
    public String getKey(String key) {
        String msg = configuration.getString(key);
        if (msg == null) { instance.getLogger().severe("Invalid Lang File, missing elements prefix or " + key); return ""; }
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
