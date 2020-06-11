package xyz.fluxinc.chatpronouns;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.fluxinc.fluxcore.configuration.LanguageManager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class ChatPronouns extends JavaPlugin implements Listener, CommandExecutor {

    private YamlConfiguration storage;
    private YamlConfiguration config;
    private File storageFile;
    private File configFile;
    private LanguageManager languageManager;
    private boolean useHover;

    @Override
    public void onEnable() {
        // Plugin startup logic
        ConfigurationSerialization.registerClass(PronounSet.class);
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null)
            new ChatPronounsPAPIHook(this).register();

        languageManager = new LanguageManager(this, "lang.yml");

        storage = new YamlConfiguration();
        storageFile = new File(getDataFolder(), "storage.yml");
        if (!storageFile.exists()) saveResource("storage.yml", false);

        config = new YamlConfiguration();
        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) saveResource("config.yml", false);

        useHover = config.getBoolean("use-hover");

        try {
            storage.load(storageFile);
        } catch (InvalidConfigurationException | IOException e) {
            e.printStackTrace();
        }

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("setpronouns").setExecutor(this);
        getCommand("setcustompronouns").setExecutor(this);
    }

    @EventHandler
    public void chatEvent(AsyncPlayerChatEvent chatEvent) {
        if (!config.getBoolean("modify-chat")) {
            return;
        }
        PronounSet pronouns = getPronouns(chatEvent.getPlayer());
        if (pronouns == null) {
            return;
        }

        if (useHover) {
            String format = languageManager.getFormattedString("chatFormat");
            format = format.replace("%display%", chatEvent.getPlayer().getDisplayName());
            format = format.replace("%player%", chatEvent.getPlayer().getName());
            format = format.replace("%message%", chatEvent.getMessage());


            TextComponent component = new TextComponent();
            TextComponent mainComponent = new TextComponent(ChatColor.translateAlternateColorCodes('&', format));

            TextComponent prefixComponent = new TextComponent(ChatColor.translateAlternateColorCodes('&', "&f[" + pronouns.miniatureString + "&f]"));
            prefixComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(pronouns.hoverText).create()));
            component.addExtra(prefixComponent);
            component.addExtra(" ");

            component.addExtra(mainComponent);

            if (!chatEvent.isCancelled()) {
                chatEvent.setCancelled(true);
                for (Player player : getServer().getOnlinePlayers()) player.spigot().sendMessage(component);
            }
        } else {
            chatEvent.setFormat(ChatColor.translateAlternateColorCodes('&', "&f[" + pronouns.miniatureString + "&f] &r") + chatEvent.getFormat());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName().toLowerCase()) {
            case "setpronouns":
                Player target = (Player) sender;
                if (args.length < 1) {
                    sendInvalidUsageMessage(sender);
                    return true;
                }
                if (args.length == 2 && sender.hasPermission("chatpronouns.others")) {
                    target = getServer().getPlayer(args[1]);
                    if (target == null) {
                        sendInvalidPlayer(sender, args[1]);
                        return true;
                    }
                }
                switch (args[0].toLowerCase()) {
                    case "f":
                        setPronouns(target, new PronounSet("&dF", "She/Her"));
                        sendSetPronounMessage(target, "She/Her");
                        break;
                    case "m":
                        setPronouns(target, new PronounSet("&bM", "He/Him"));
                        sendSetPronounMessage(target, "He/Him");
                        break;
                    case "n":
                        setPronouns(target, new PronounSet("&fN", "They/Them"));
                        sendSetPronounMessage(target, "They/Them");
                        break;
                    default:
                        sendInvalidUsageMessage(sender);
                        break;
                }
                return true;
            case "setcustompronouns":
                if (args.length < 3) {
                    sendInvalidUsageCustomMessage(sender);
                    return true;
                }
                Player player = getServer().getPlayer(args[0]);
                if (player == null) {
                    sendInvalidUsageCustomMessage(sender);
                    return true;
                }
                setPronouns(player, new PronounSet(args[1], args[2]));
                sendSetPronounMessage(player, args[2]);
                sendSetPronounOthersMessage(sender, player, args[2]);
                return true;
            case "removepronouns":
                Player cmdTarget = (Player) sender;
                if (args.length == 1 && sender.hasPermission("chatpronouns.others")) {
                    cmdTarget = getServer().getPlayer(args[0]);
                    if (cmdTarget == null) {
                        sendInvalidPlayer(sender, args[0]);
                        return true;
                    }
                }
                removePronouns(cmdTarget);
                if (cmdTarget == sender) {
                    sendRemovedPronouns(sender);
                } else {
                    sendRemovedPronouns(cmdTarget);
                    sendTargetRemovedPronouns(sender, args[0]);
                }
        }
        return true;
    }

    public PronounSet getPronouns(Player player) {
        Object key = storage.get("" + player.getUniqueId());
        if (key == null) {
            return null;
        }
        return storage.getSerializable("" + player.getUniqueId(), PronounSet.class);
    }

    private void setPronouns(Player player, PronounSet pronounSet) {
        storage.set("" + player.getUniqueId(), pronounSet);
        try {
            storage.save(storageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removePronouns(Player player) {
        storage.set("" + player.getUniqueId(), null);
        try {
            storage.save(storageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendSetPronounMessage(CommandSender sender, String pronouns) {
        Map<String, String> args = new HashMap<>();
        args.put("pronouns", pronouns);
        sender.sendMessage(languageManager.generateMessage("setPronouns", args));
    }

    private void sendInvalidPlayer(CommandSender sender, String player) {
        Map<String, String> args = new HashMap<>();
        args.put("player", player);
        sender.sendMessage(languageManager.generateMessage("unknownPlayer", args));
    }

    private void sendRemovedPronouns(CommandSender sender) {
        sender.sendMessage(languageManager.generateMessage("removedPronouns"));
    }

    private void sendTargetRemovedPronouns(CommandSender sender, String player) {
        Map<String, String> args = new HashMap<>();
        args.put("player", player);
        sender.sendMessage(languageManager.generateMessage("removedOthersPronouns", args));
    }

    private void sendInvalidUsageCustomMessage(CommandSender sender) {
        sender.sendMessage(languageManager.generateMessage("invalidUsageCustom"));
    }

    private void sendInvalidUsageMessage(CommandSender sender) {
        sender.sendMessage(languageManager.generateMessage("invalidUsage"));
    }

    private void sendSetPronounOthersMessage(CommandSender sender, Player player, String pronouns) {
        Map<String, String> args = new HashMap<>();
        args.put("pronouns", pronouns);
        args.put("display", player.getDisplayName());
        args.put("player", player.getName());
        sender.sendMessage(languageManager.generateMessage("setOthersPronouns", args));
    }


}
