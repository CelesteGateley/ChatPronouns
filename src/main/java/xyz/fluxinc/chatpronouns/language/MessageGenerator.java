package xyz.fluxinc.chatpronouns.language;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.fluxinc.chatpronouns.ChatPronouns;
import xyz.fluxinc.chatpronouns.storage.PronounSet;
import xyz.fluxinc.fluxcore.configuration.LanguageManager;

import java.util.HashMap;
import java.util.Map;

public class MessageGenerator extends LanguageManager<ChatPronouns> {
    
    public MessageGenerator(ChatPronouns plugin, String langFile) {
        super(plugin, langFile);
    }

    public String generateSetPronounMessage(PronounSet pronouns) {
        Map<String, String> args = new HashMap<>();
        args.put("pronouns", pronouns.hoverText);
        args.put("min", pronouns.miniatureString);
        return this.generateMessage("setPronouns", args);
    }

    public String generateBroadcastMessage(Player player, PronounSet pronouns) {
        Map<String, String> args = new HashMap<>();
        args.put("displayname", player.getDisplayName());
        args.put("pronouns", pronouns.hoverText);
        return this.generateMessage("broadcastMessage", args);
    }

    public String generateInvalidPlayer(String player) {
        Map<String, String> args = new HashMap<>();
        args.put("player", player);
        return this.generateMessage("unknownPlayer", args);
    }

    public String generateRemovedPronouns() {
        return this.generateMessage("removedPronouns");
    }

    public String generateTargetRemovedPronouns(String player) {
        Map<String, String> args = new HashMap<>();
        args.put("player", player);
        return this.generateMessage("removedOthersPronouns", args);
    }

    public String generateTargetSetPronouns(String player, PronounSet pronouns) {
        Map<String, String> args = new HashMap<>();
        args.put("player", player);
        args.put("pronouns", pronouns.hoverText);
        args.put("min", pronouns.miniatureString);
        return this.generateMessage("setOthersPronouns", args);
    }

    public String generateInvalidUsageCustomMessage() {
        return this.generateMessage("invalidUsageCustom");
    }

    public String generateInvalidUsageMessage() {
        return this.generateMessage("invalidUsage");
    }

    public String generateSetPronounOthersMessage(Player player, PronounSet pronouns) {
        Map<String, String> args = new HashMap<>();
        args.put("pronouns", pronouns.hoverText);
        args.put("min", pronouns.miniatureString);
        args.put("display", player.getDisplayName());
        args.put("player", player.getName());
        return this.generateMessage("setOthersPronouns", args);
    }
}
