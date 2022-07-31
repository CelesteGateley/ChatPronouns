package xyz.fluxinc.chatpronouns.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import xyz.fluxinc.chatpronouns.ChatPronouns;
import xyz.fluxinc.chatpronouns.storage.UserData;

public class JoinPromptListener implements Listener {

    private final ChatPronouns instance;

    public JoinPromptListener(ChatPronouns instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UserData data = instance.getStorageManager().getUserData(event.getPlayer());
        if (data == null || !data.doNotPrompt) {
            event.getPlayer().sendMessage(instance.getLanguageManager().generateMessage("pronounsNotSet"));
        }
    }

}
