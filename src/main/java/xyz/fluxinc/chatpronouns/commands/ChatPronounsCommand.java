package xyz.fluxinc.chatpronouns.commands;

import xyz.fluxinc.chatpronouns.ChatPronouns;
import xyz.fluxinc.fluxcore.command.Command;

public class ChatPronounsCommand {

    private final ChatPronouns instance;
    private static final String cmd = "chatpronouns";
    private static final String[] aliases = {"cp",};

    public ChatPronounsCommand(ChatPronouns instance) {
        this.instance = instance;
    }

    public Command getReloadCommand() {
        Command command = new Command(cmd, aliases).literal("reload");
        return command.executor((sender, args) -> {
            if (!sender.hasPermission("chatpronouns.reload")) {
                sender.sendMessage(instance.getLanguageManager().generateNoPermissions());
                return;
            }
            instance.reload();
            sender.sendMessage(instance.getLanguageManager().generateMessage("reloaded"));
        });
    }
}
