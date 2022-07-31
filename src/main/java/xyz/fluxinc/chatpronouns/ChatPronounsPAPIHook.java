package xyz.fluxinc.chatpronouns;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import xyz.fluxinc.chatpronouns.storage.PronounSet;

public class ChatPronounsPAPIHook extends PlaceholderExpansion {

    private final ChatPronouns instance;

    public ChatPronounsPAPIHook(ChatPronouns instance) {
        this.instance = instance;
    }

    @Override
    public boolean persist(){ return true; }

    @Override
    public boolean canRegister(){ return true; }

    @Override
    public String getIdentifier() {
        return "chatpronouns";
    }

    @Override
    public String getAuthor() {
        return instance.getDescription().getAuthors().get(0);
    }

    @Override
    public String getVersion() {
        return instance.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) return "";
        PronounSet pronouns = instance.getStorageManager().getUserData(player).pronouns;
        if (pronouns == null) return "";

        switch (identifier) {
            case "miniature_tag": return pronouns.miniatureString;
            case "hover_pronouns": return pronouns.hoverText;
            case "full_hover": return ChatColor.GOLD + "Pronouns: " + ChatColor.YELLOW + pronouns.hoverText;
            case "full_hover_new": return ChatColor.GOLD + "Pronouns: " + ChatColor.YELLOW + pronouns.hoverText  +"\n";
            case "new_full_hover": return "\n" + ChatColor.GOLD + "Pronouns: " + ChatColor.YELLOW + pronouns.hoverText;
            case "new_full_hover_new": return "\n" + ChatColor.GOLD + "Pronouns: " + ChatColor.YELLOW + pronouns.hoverText + "\n";
            case "tag": return "[" + pronouns.miniatureString + ChatColor.WHITE + "] ";
            default: return "";
        }
    }
}
