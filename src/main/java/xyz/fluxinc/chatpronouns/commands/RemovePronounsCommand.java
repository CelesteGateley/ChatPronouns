package xyz.fluxinc.chatpronouns.commands;

import org.bukkit.entity.Player;
import xyz.fluxinc.chatpronouns.ChatPronouns;
import xyz.fluxinc.fluxcore.command.Command;

import java.io.IOException;

public class RemovePronounsCommand {

    private static final String cmd = "removepronouns";
    private final ChatPronouns instance;

    public RemovePronounsCommand(ChatPronouns instance) {
        this.instance = instance;
        getRemoveCommand().register();
    }

    public Command getRemoveCommand() {
        Command command = new Command(cmd).player("player");
        return command.executor((sender, args) -> {
            Player cmdTarget = args[0] != null && sender.hasPermission("chatpronouns.others") ? (Player) args[0] : (Player) sender;
            try {
                instance.getStorageManager().setPronouns(cmdTarget, null);
                if (cmdTarget == sender) {
                    sender.sendMessage(instance.getLanguageManager().generateRemovedPronouns());
                } else {
                    sender.sendMessage(instance.getLanguageManager().generateTargetRemovedPronouns(cmdTarget));
                    cmdTarget.sendMessage(instance.getLanguageManager().generateRemovedPronouns());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
    }
}
