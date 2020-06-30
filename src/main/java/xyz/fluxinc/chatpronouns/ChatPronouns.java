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
import xyz.fluxinc.fluxcore.configuration.LanguageManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("NullPointerException")
public final class ChatPronouns extends JavaPlugin implements Listener, CommandExecutor {

    private YamlConfiguration storage;
    private YamlConfiguration config;
    private File storageFile;
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

        storage = new YamlConfiguration();
        storageFile = new File(getDataFolder(), "storage.yml");
        if (!storageFile.exists()) saveResource("storage.yml", false);

        config = new YamlConfiguration();
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) saveResource("config.yml", false);

        try {
            storage.load(storageFile);
            config.load(configFile);
        } catch (InvalidConfigurationException | IOException e) {
            e.printStackTrace();
        }

        useHover = config.getBoolean("use-hover");
        promptOnJoin = config.getBoolean("prompt-on-join");
        broadcast = config.getBoolean("broadcast-change");

        if (config.getBoolean("update-old-data")) {
            for (String key : storage.getKeys(false)) {
                if (storage.get(key) instanceof PronounSet) {
                    storage.set(key, new UserData((PronounSet) storage.get(key)));
                }
            }
            try { storage.save(storageFile); } catch (IOException e) { e.printStackTrace(); }
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
                        setPronouns(target, female);
                        sendSetPronounMessage(target, "She/Her");
                        if (target != sender) { sentTargetSetPronouns(sender, ((Player) sender).getDisplayName(), "She/Her"); }
                        break;
                    case "m":
                        setPronouns(target, male);
                        sendSetPronounMessage(target, "He/Him");
                        if (target != sender) { sentTargetSetPronouns(sender, ((Player) sender).getDisplayName(), "He/Him"); }
                        break;
                    case "n":
                        setPronouns(target, nonBinary);
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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!promptOnJoin) return;
        UserData data = (UserData) storage.get(event.getPlayer().getUniqueId().toString());
        if (data == null || data.doNotPrompt) {
            event.getPlayer().openInventory(graphicalInterface);
            hasOpenInventory.add(event.getPlayer());
        }
    }

    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player && hasOpenInventory.contains((Player) event.getPlayer())) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> event.getPlayer().openInventory(graphicalInterface), 1);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
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
                    return;
            }
        }
    }

    public PronounSet getPronouns(Player player) {
        Object key = storage.get("" + player.getUniqueId());
        if (key == null || storage.getSerializable("" + player.getUniqueId(), UserData.class) == null) {
            return null;
        }
        return storage.getSerializable("" + player.getUniqueId(), UserData.class).pronouns;
    }

    private void setPronouns(Player player, PronounSet pronounSet) {
        storage.set("" + player.getUniqueId(), new UserData(pronounSet));
        if (broadcast) {
            Map<String, String> args = new HashMap<>();
            args.put("displayname", player.getDisplayName());
            args.put("pronouns", pronounSet.hoverText);
            getServer().broadcastMessage(languageManager.generateMessage("broadcastMessage", args));
        }
        try {
            storage.save(storageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removePronouns(Player player) {
        storage.set("" + player.getUniqueId(), new UserData(true));
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
