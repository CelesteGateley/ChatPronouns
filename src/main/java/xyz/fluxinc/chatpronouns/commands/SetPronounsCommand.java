package xyz.fluxinc.chatpronouns.commands;

import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.fluxinc.chatpronouns.ChatPronouns;
import xyz.fluxinc.chatpronouns.storage.PronounSet;
import xyz.fluxinc.fluxcore.command.Command;

import java.io.IOException;
import java.util.List;

public class SetPronounsCommand {

    private static final String cmd = "setpronouns";
    private final ChatPronouns instance;
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
        getSetOtherCommand().register();
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
        Command command = new Command(cmd).raw(new MultiLiteralArgument("gender", List.of("male", "female", "non-binary", "unset")));
        return command.executor((sender, args) -> {
            Player target = (Player) sender;
            setPronouns(sender, target, (String) args.get(0));
        });
    }

    public Command getSetOtherCommand() {
        Command command = new Command(cmd).raw(new MultiLiteralArgument("gender", List.of("male", "female", "non-binary", "unset"))).player("player");
        return command.executor((sender, args) -> {
            Player target = args.get(1) != null && sender.hasPermission("chatpronouns.others") ? (Player) args.get(1) : (Player) sender;
            setPronouns(sender, target, (String) args.get(0));
        });
    }

    private void setPronouns(CommandSender sender, Player target, String pronouns)  {
        PronounSet set = null;
        switch (pronouns) {
            case "male":
                set = male;
                break;
            case "female":
                set = female;
                break;
            case "non-binary":
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
    }
}
