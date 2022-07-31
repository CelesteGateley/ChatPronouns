package xyz.fluxinc.chatpronouns.storage;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class StorageManager {

    private final YamlConfiguration storage;
    private final File file;

    public StorageManager(File file) throws IOException, InvalidConfigurationException {
        this.file = file;
        this.storage = new YamlConfiguration();
        this.storage.load(file);
    }

    public UserData getUserData(OfflinePlayer player) {
        Object key = storage.get("" + player.getUniqueId());
        if (key == null || storage.getSerializable("" + player.getUniqueId(), UserData.class) == null) {
            return null;
        }
        return storage.getSerializable("" + player.getUniqueId(), UserData.class);
    }

    public void setUserData(OfflinePlayer player, UserData userData) throws IOException {
        storage.set("" + player.getUniqueId(), userData);
        storage.save(file);
    }

    public void setPronouns(OfflinePlayer player, PronounSet pronouns) throws IOException {
        UserData userData = this.getUserData(player);
        userData.pronouns = pronouns;
        this.setUserData(player, userData);
    }

    public void setPrompt(OfflinePlayer player, boolean prompt) throws IOException {
        UserData userData = this.getUserData(player);
        userData.doNotPrompt = prompt;
        this.setUserData(player, userData);
    }
}
