package xyz.fluxinc.chatpronouns.commands;

import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import xyz.fluxinc.chatpronouns.ChatPronouns;
import xyz.fluxinc.chatpronouns.storage.PronounSet;
import xyz.fluxinc.fluxcore.command.Command;

import java.io.IOException;

public class SetPronounsCommand {

    private final ChatPronouns instance;
    private static final String cmd = "setpronouns";
    private final PronounSet male;
    private final PronounSet female;
    private final PronounSet nonbinary;

    public SetPronounsCommand(ChatPronouns instance, PronounSet male, PronounSet female, PronounSet nonbinary) {
        this.instance = instance;
        this.male = male;
        this.female = female;
        this.nonbinary = nonbinary;
        getDefaultCommand().register();
        getSetCommand().register();
    }

    public Command getDefaultCommand() {
        Command command = new Command(cmd);
        return command.executor((sender, args) -> {
            if (sender instanceof Player) {
                instance.getInventorySelector().showInventory((Player) sender);
            }
        });
    }

    public Command getSetCommand() {
        Command command = new Command(cmd).raw(new MultiLiteralArgument("male", "female", "non-binary", "unset")).player("player");
        return command.executor((sender, args) -> {
            Player target = args[1] != null && sender.hasPermission("chatpronouns.others") ? (Player) args[1] : (Player) sender;
            PronounSet set = null;
            switch ((String) args[0]) {
                case "male":
                    set = male;
                    break;
                case "female":
                    set = female;
                    break;
                case "nonbinary":
                    set = nonbinary;
                    break;
            }
            try {
                instance.getStorageManager().setPronouns(target, set);
                if (set != null) {
                    target.sendMessage(instance.getLanguageManager().generateSetPronounMessage(set));
                    if (instance.getConfiguration().getBoolean("broadcast-change")) {
                        instance.getServer().broadcastMessage(instance.getLanguageManager().generateBroadcastMessage(target, set));
                    }
                } else {
                    target.sendMessage(instance.getLanguageManager().generateRemovedPronouns());
                }
            } catch (IOException e) {
                sender.sendMessage("A fatal error has occurred");
            }
        });
    }
}
