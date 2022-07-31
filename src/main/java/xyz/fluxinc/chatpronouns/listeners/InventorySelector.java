package xyz.fluxinc.chatpronouns.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.fluxinc.chatpronouns.ChatPronouns;
import xyz.fluxinc.chatpronouns.storage.PronounSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InventorySelector implements Listener {

    private final List<Player> open;
    private final ChatPronouns instance;
    private final PronounSet male;
    private final PronounSet female;
    private final PronounSet nonbinary;

    public InventorySelector(ChatPronouns instance, PronounSet male, PronounSet female, PronounSet nonbinary) {
        this.instance = instance;
        this.male = male;
        this.female = female;
        this.nonbinary = nonbinary;
        this.open = new ArrayList<>();
    }

    public void showInventory(Player player) {
        this.open.add(player);
        player.openInventory(this.getInventory());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) throws IOException {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (!open.contains(player)) return;
            event.setCancelled(true);
            PronounSet set = null;
            switch (event.getCurrentItem().getType()) {
                case WHITE_WOOL:
                    set = nonbinary;
                    break;
                case LIGHT_BLUE_WOOL:
                    set = male;
                    break;
                case PINK_WOOL:
                    set = female;
                    break;
                case BARRIER:
                    break;
                default:
                    return;
            }
            instance.getStorageManager().setPronouns(player, set);
            if (set == null) {
                player.sendMessage(instance.getLanguageManager().generateRemovedPronouns());
            } else {
                player.sendMessage(instance.getLanguageManager().generateSetPronounMessage(set));
                if (instance.shouldBroadcast()) {
                    instance.getServer().broadcastMessage(instance.getLanguageManager().generateBroadcastMessage(player, set));
                }
            }
            open.remove(player);
            player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event) throws IOException {
        if (event.getPlayer() instanceof Player && open.contains((Player) event.getPlayer())) {
            instance.getStorageManager().setPronouns((Player) event.getPlayer(), null);
            open.remove((Player) event.getPlayer());
        }
    }

    private Inventory getInventory() {
        Inventory graphicalInterface = Bukkit.createInventory(null, 27, "Select Your Pronouns");
        ItemStack male = new ItemStack(Material.LIGHT_BLUE_WOOL);
        ItemMeta maleMeta = male.getItemMeta();
        maleMeta.setDisplayName("Male");
        male.setItemMeta(maleMeta);
        ItemStack female = new ItemStack(Material.PINK_WOOL);
        ItemMeta femaleMeta = female.getItemMeta();
        femaleMeta.setDisplayName("Female");
        female.setItemMeta(femaleMeta);
        ItemStack nb = new ItemStack(Material.WHITE_WOOL);
        ItemMeta nbMeta = nb.getItemMeta();
        nbMeta.setDisplayName("Non-Binary");
        nb.setItemMeta(nbMeta);
        ItemStack unset = new ItemStack(Material.BARRIER);
        ItemMeta unsetMeta = unset.getItemMeta();
        unsetMeta.setDisplayName("Rather Not Say");
        unset.setItemMeta(unsetMeta);
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
        return graphicalInterface;
    }
}
