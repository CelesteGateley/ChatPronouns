package xyz.fluxinc.chatpronouns;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.fluxinc.chatpronouns.storage.PronounSet;
import xyz.fluxinc.chatpronouns.storage.StorageManager;
import xyz.fluxinc.chatpronouns.storage.UserData;
import xyz.fluxinc.fluxcore.configuration.LanguageManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@SuppressWarnings("NullPointerException")
public final class ChatPronouns extends JavaPlugin implements Listener, CommandExecutor {

    private StorageManager storageManager;
    private YamlConfiguration config;
    private LanguageManager<ChatPronouns> languageManager;

    private boolean useHover;
    private boolean promptOnJoin;
    private boolean broadcast;

    private final PronounSet female = new PronounSet("&dF", "She/Her");
    private final PronounSet male = new PronounSet("&bM", "He/Him");
    private final PronounSet nonBinary = new PronounSet("&fN", "They/Them");

    private static final Inventory graphicalInterface = Bukkit.createInventory(null, 27, "Select Your Pronouns");
    private final List<Player> hasOpenInventory = new ArrayList<>();
    static {
        ItemStack male = new ItemStack(Material.LIGHT_BLUE_WOOL);
        ItemMeta maleMeta = male.getItemMeta(); maleMeta.setDisplayName("Male"); male.setItemMeta(maleMeta);
        ItemStack female = new ItemStack(Material.PINK_WOOL);
        ItemMeta femaleMeta = female.getItemMeta(); femaleMeta.setDisplayName("Female"); female.setItemMeta(femaleMeta);
        ItemStack nb = new ItemStack(Material.WHITE_WOOL);
        ItemMeta nbMeta = nb.getItemMeta(); nbMeta.setDisplayName("Non-Binary"); nb.setItemMeta(nbMeta);
        ItemStack unset = new ItemStack(Material.BARRIER);
        ItemMeta unsetMeta = unset.getItemMeta(); unsetMeta.setDisplayName("Rather Not Say"); unset.setItemMeta(unsetMeta);
        for (int i = 0; i < 27; i++) {
            if (i == 10) {
                graphicalInterface.setItem(i, male);
            } else if (i == 13) {
                graphicalInterface.setItem(i, nb);
            } else if (i == 16) {
                graphicalInterface.setItem(i, female);
            } else if (i == 22) {
                graphicalInterface.setItem(i, unset);
            } else {
                graphicalInterface.setItem(i, new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
            }
        }
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        ConfigurationSerialization.registerClass(PronounSet.class);
        ConfigurationSerialization.registerClass(UserData.class);
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null)
            new ChatPronounsPAPIHook(this).register();

        languageManager = new LanguageManager<>(this, "lang.yml");

        File storageFile = new File(getDataFolder(), "storage.yml");
        if (!storageFile.exists()) saveResource("storage.yml", false);
        try { storageManager = new StorageManager(storageFile); } catch (IOException | InvalidConfigurationException e) { throw new RuntimeException(e); }

        config = new YamlConfiguration();
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) saveResource("config.yml", false);

        try {
            config.load(configFile);
        } catch (InvalidConfigurationException | IOException e) {
            e.printStackTrace();
        }

        useHover = config.getBoolean("use-hover");
        promptOnJoin = config.getBoolean("prompt-on-join");
        broadcast = config.getBoolean("broadcast-change");

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
                    ((Player) sender).openInventory(graphicalInterface);
                    hasOpenInventory.add((Player) sender);
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
                        try {
                            setPronouns(target, female);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        sendSetPronounMessage(target, "She/Her");
                        if (target != sender) { sentTargetSetPronouns(sender, ((Player) sender).getDisplayName(), "She/Her"); }
                        break;
                    case "m":
                        try {
                            setPronouns(target, male);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        sendSetPronounMessage(target, "He/Him");
                        if (target != sender) { sentTargetSetPronouns(sender, ((Player) sender).getDisplayName(), "He/Him"); }
                        break;
                    case "n":
                        try {
                            setPronouns(target, nonBinary);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        sendSetPronounMessage(target, "They/Them");
                        if (target != sender) { sentTargetSetPronouns(sender, ((Player) sender).getDisplayName(), "They/Them"); }
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
                try {
                    setPronouns(player, new PronounSet(args[1], args[2]));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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
                try {
                    removePronouns(cmdTarget);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (cmdTarget == sender) {
                    sendRemovedPronouns(sender);
                } else {
                    sendRemovedPronouns(cmdTarget);
                    sendTargetRemovedPronouns(sender, args[0]);
                }
        }
        return true;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!promptOnJoin) return;
        UserData data = storageManager.getUserData(event.getPlayer());
        if (data == null || !data.doNotPrompt) {
            event.getPlayer().sendMessage(languageManager.generateMessage("pronounsNotSet"));
        }
    }

    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event) throws IOException {
        if (event.getPlayer() instanceof Player && hasOpenInventory.contains((Player) event.getPlayer())) {
            //Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> event.getPlayer().openInventory(graphicalInterface), 10);
            removePronouns((Player) event.getPlayer());
            hasOpenInventory.remove((Player) event.getPlayer());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) throws IOException {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (!hasOpenInventory.contains(player)) return;
            event.setCancelled(true);
            switch (event.getCurrentItem().getType()) {
                case WHITE_WOOL:
                    setPronouns(player, nonBinary);
                    sendSetPronounMessage(player, "They/Them");
                    hasOpenInventory.remove(player);
                    player.closeInventory();
                    return;
                case LIGHT_BLUE_WOOL:
                    setPronouns(player, male);
                    sendSetPronounMessage(player, "He/Him");
                    hasOpenInventory.remove(player);
                    player.closeInventory();
                    return;
                case PINK_WOOL:
                    setPronouns(player, female);
                    sendSetPronounMessage(player, "She/Her");
                    hasOpenInventory.remove(player);
                    player.closeInventory();
                    return;
                case BARRIER:
                    removePronouns(player);
                    hasOpenInventory.remove(player);
                    player.closeInventory();
                    return;
                default:
            }
        }
    }

    public PronounSet getPronouns(Player player) {
        return storageManager.getUserData(player).pronouns;
    }

    private void setPronouns(Player player, PronounSet pronounSet) throws IOException {
        storageManager.setPronouns(player, pronounSet);
        if (broadcast) {
            Map<String, String> args = new HashMap<>();
            args.put("displayname", player.getDisplayName());
            args.put("pronouns", pronounSet.hoverText);
            getServer().broadcastMessage(languageManager.generateMessage("broadcastMessage", args));
        }

    }

    private void removePronouns(Player player) throws IOException {
        storageManager.setPronouns(player, null);
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

    private void sentTargetSetPronouns(CommandSender sender, String player, String pronouns) {
        Map<String, String> args = new HashMap<>();
        args.put("player", player);
        args.put("pronouns", pronouns);
        sender.sendMessage(languageManager.generateMessage("setOthersPronouns", args));
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
