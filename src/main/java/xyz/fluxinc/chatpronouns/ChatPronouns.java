package xyz.fluxinc.chatpronouns;

import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.fluxinc.chatpronouns.commands.RemovePronounsCommand;
import xyz.fluxinc.chatpronouns.commands.SetCustomCommand;
import xyz.fluxinc.chatpronouns.commands.SetPronounsCommand;
import xyz.fluxinc.chatpronouns.language.MessageGenerator;
import xyz.fluxinc.chatpronouns.listeners.ChatFormatListener;
import xyz.fluxinc.chatpronouns.listeners.InventorySelector;
import xyz.fluxinc.chatpronouns.listeners.JoinPromptListener;
import xyz.fluxinc.chatpronouns.storage.PronounSet;
import xyz.fluxinc.chatpronouns.storage.StorageManager;
import xyz.fluxinc.chatpronouns.storage.UserData;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("NullPointerException")
public final class ChatPronouns extends JavaPlugin implements Listener, CommandExecutor {

    private final PronounSet female = new PronounSet("&dF", "She/Her");
    private final PronounSet male = new PronounSet("&bM", "He/Him");
    private final PronounSet nonBinary = new PronounSet("&fN", "They/Them");
    private StorageManager storageManager;
    private YamlConfiguration config;
    private MessageGenerator languageManager;
    private boolean useHover;
    private boolean broadcast;
    private InventorySelector inventorySelector;

    public MessageGenerator getLanguageManager() {
        return this.languageManager;
    }

    public StorageManager getStorageManager() {
        return this.storageManager;
    }

    public YamlConfiguration getConfiguration() {
        return this.config;
    }

    public InventorySelector getInventorySelector() {
        return this.inventorySelector;
    }

    public boolean useHover() {
        return useHover;
    }

    public boolean shouldBroadcast() {
        return broadcast;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        ConfigurationSerialization.registerClass(PronounSet.class);
        ConfigurationSerialization.registerClass(UserData.class);
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null)
            new ChatPronounsPAPIHook(this).register();

        languageManager = new MessageGenerator(this, "lang.yml");

        File storageFile = new File(getDataFolder(), "storage.yml");
        if (!storageFile.exists()) saveResource("storage.yml", false);
        try {
            storageManager = new StorageManager(storageFile);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }

        config = new YamlConfiguration();
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) saveResource("config.yml", false);

        try {
            config.load(configFile);
        } catch (InvalidConfigurationException | IOException e) {
            e.printStackTrace();
        }

        useHover = config.getBoolean("use-hover");
        broadcast = config.getBoolean("broadcast-change");

        inventorySelector = new InventorySelector(this, male, female, nonBinary);
        getServer().getPluginManager().registerEvents(inventorySelector, this);

        if (config.getBoolean("modify-chat")) {
            getServer().getPluginManager().registerEvents(new ChatFormatListener(this), this);
        }

        if (config.getBoolean("prompt-on-join")) {
            getServer().getPluginManager().registerEvents(new JoinPromptListener(this), this);
        }

        new SetPronounsCommand(this, male, female, nonBinary);
        new SetCustomCommand(this);
        new RemovePronounsCommand(this);
    }
}
