package com.sylluxpvp.circuit.bukkit.menus.rank;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.bukkit.tools.menu.Menu;
import com.sylluxpvp.circuit.shared.rank.Rank;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.RankService;
import com.sylluxpvp.circuit.shared.tools.string.CC;

import java.util.logging.Logger;

public class RankInputHandler implements Listener {

    private static final Logger LOGGER = CircuitPlugin.getInstance().getLogger();

    public enum InputType {
        PREFIX,
        SUFFIX,
        WEIGHT,
        PERMISSION,
        CREATE
    }

    private static final String CANCEL_KEYWORD = "cancel";

    private final Player player;
    private final Rank rank;
    private final InputType type;
    private final Menu returnMenu;

    public RankInputHandler(Player player, Rank rank, InputType type, Menu returnMenu) {
        this.player = player;
        this.rank = rank;
        this.type = type;
        this.returnMenu = returnMenu;
    }

    public void start() {
        CircuitPlugin.getInstance().getServer().getPluginManager().registerEvents(this, CircuitPlugin.getInstance());

        switch (type) {
            case PREFIX:
                player.sendMessage(CC.translate("&aType the new prefix"));
                break;
            case SUFFIX:
                player.sendMessage(CC.translate("&aType the new suffix"));
                break;
            case WEIGHT:
                player.sendMessage(CC.translate("&aType the new weight"));
                break;
            case PERMISSION:
                player.sendMessage(CC.translate("&aType the permission to toggle"));
                break;
            case CREATE:
                player.sendMessage(CC.translate("&aType the name for the new rank"));
                break;
        }
        player.sendMessage(CC.translate("&7Type &c" + CANCEL_KEYWORD + " &7to cancel."));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!event.getPlayer().getUniqueId().equals(player.getUniqueId())) {
            return;
        }

        event.setCancelled(true);
        String message = event.getMessage().trim();

        if (message.equalsIgnoreCase(CANCEL_KEYWORD)) {
            cleanup();
            player.sendMessage(CC.translate("&cCancelled."));
            CircuitPlugin.getInstance().getServer().getScheduler().runTask(CircuitPlugin.getInstance(), () -> {
                if (rank != null) {
                    new RankEditorMenu(rank).openMenu(player);
                } else {
                    returnMenu.openMenu(player);
                }
            });
            return;
        }

        RankService service = ServiceContainer.getService(RankService.class);

        switch (type) {
            case PREFIX:
                String prefix = message.equalsIgnoreCase("none") ? "" : message;
                rank.setPrefix(prefix);
                service.save(rank);
                player.sendMessage(CC.translate("&aPrefix updated to: " + prefix + "Player"));
                LOGGER.info(player.getName() + " changed prefix of rank " + rank.getName() + " to: " + prefix);
                break;

            case SUFFIX:
                String suffix = message.equalsIgnoreCase("none") ? "" : message;
                rank.setSuffix(suffix);
                service.save(rank);
                player.sendMessage(CC.translate("&aSuffix updated to: Player" + suffix));
                LOGGER.info(player.getName() + " changed suffix of rank " + rank.getName() + " to: " + suffix);
                break;

            case WEIGHT:
                try {
                    int weight = Integer.parseInt(message);
                    if (weight < 0) {
                        player.sendMessage(CC.translate("&cWeight must be a positive number!"));
                        return;
                    }
                    rank.setWeight(weight);
                    service.save(rank);
                    player.sendMessage(CC.translate("&aWeight updated to: " + weight));
                    LOGGER.info(player.getName() + " changed weight of rank " + rank.getName() + " to: " + weight);
                } catch (NumberFormatException e) {
                    player.sendMessage(CC.translate("&cInvalid number! Please try again."));
                    return;
                }
                break;

            case PERMISSION:
                boolean hadPermission = rank.hasPermission(message);
                rank.setPermission(message);
                service.save(rank);
                if (hadPermission) {
                    player.sendMessage(CC.translate("&cRemoved permission: " + message));
                    LOGGER.info(player.getName() + " removed permission " + message + " from rank " + rank.getName());
                } else {
                    player.sendMessage(CC.translate("&aAdded permission: " + message));
                    LOGGER.info(player.getName() + " added permission " + message + " to rank " + rank.getName());
                }
                break;

            case CREATE:
                if (service.getRank(message) != null) {
                    player.sendMessage(CC.translate("&cA rank with that name already exists!"));
                    return;
                }
                Rank newRank = service.create(message);
                service.save(newRank);
                player.sendMessage(CC.translate("&aRank '" + message + "' has been created!"));
                LOGGER.info(player.getName() + " created rank: " + message);
                cleanup();
                CircuitPlugin.getInstance().getServer().getScheduler().runTask(CircuitPlugin.getInstance(), () -> {
                    new RankEditorMenu(newRank).openMenu(player);
                });
                return;
        }

        cleanup();
        CircuitPlugin.getInstance().getServer().getScheduler().runTask(CircuitPlugin.getInstance(), () -> {
            if (rank != null) {
                new RankEditorMenu(rank).openMenu(player);
            } else {
                returnMenu.openMenu(player);
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (event.getPlayer().getUniqueId().equals(player.getUniqueId())) {
            cleanup();
        }
    }

    private void cleanup() {
        HandlerList.unregisterAll(this);
    }
}
