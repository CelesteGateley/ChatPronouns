package xyz.fluxinc.chatpronouns.commands;

import org.bukkit.entity.Player;
import xyz.fluxinc.chatpronouns.ChatPronouns;
import xyz.fluxinc.chatpronouns.storage.PronounSet;
import xyz.fluxinc.fluxcore.command.Command;

import java.io.IOException;

public class SetCustomCommand {

    private static final String cmd = "setcustompronouns";
    private final ChatPronouns instance;

    public SetCustomCommand(ChatPronouns instance) {
        this.instance = instance;
        getSetCustomCommand().register();
    }

    public Command getSetCustomCommand() {
        Command command = new Command(cmd).player("name").string("pronouns").string("icon");
        return command.executor((sender, args) -> {
            if (!sender.hasPermission("chatpronouns.custom")) {
                return;
            }
            Player player = (Player) args[0];
            if (player == null) {
                sender.sendMessage(instance.getLanguageManager().generateInvalidUsageMessage());
                return;
            }
            try {
                PronounSet set = new PronounSet((String) args[1], (String) args[2]);
                instance.getStorageManager().setPronouns(player, set);
                player.sendMessage(instance.getLanguageManager().generateSetPronounMessage(set));
                sender.sendMessage(instance.getLanguageManager().generateTargetSetPronouns(player, set));
                if (instance.shouldBroadcast()) {
                    instance.getServer().broadcastMessage(instance.getLanguageManager().generateBroadcastMessage(player, set));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
