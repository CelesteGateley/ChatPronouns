package xyz.fluxinc.chatpronouns;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import xyz.fluxinc.chatpronouns.storage.PronounSet;
import xyz.fluxinc.fluxcore.hooks.Placeholder;

public class ChatPronounsPAPIHook extends Placeholder {

    private final ChatPronouns instance;

    public ChatPronounsPAPIHook(ChatPronouns instance) {
        super(instance);
        this.instance = instance;
    }

    @Override
    public String placeholder(Player player, String identifier) {
        if (player == null) return "";
        PronounSet pronouns = instance.getStorageManager().getUserData(player).pronouns;
        if (pronouns == null) return "";

        switch (identifier) {
            case "miniature_tag":
                return pronouns.miniatureString;
            case "hover_pronouns":
                return pronouns.hoverText;
            case "full_hover":
                return ChatColor.GOLD + "Pronouns: " + ChatColor.YELLOW + pronouns.hoverText;
            case "full_hover_new":
                return ChatColor.GOLD + "Pronouns: " + ChatColor.YELLOW + pronouns.hoverText + "\n";
            case "new_full_hover":
                return "\n" + ChatColor.GOLD + "Pronouns: " + ChatColor.YELLOW + pronouns.hoverText;
            case "new_full_hover_new":
                return "\n" + ChatColor.GOLD + "Pronouns: " + ChatColor.YELLOW + pronouns.hoverText + "\n";
            case "tag":
                return "[" + pronouns.miniatureString + ChatColor.WHITE + "] ";
            default:
                return "";
        }
    }
}
