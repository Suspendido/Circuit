package com.sylluxpvp.circuit.bukkit.command.staff;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.bukkit.menus.player.UserPermissionsMenu;
import com.sylluxpvp.circuit.bukkit.module.impl.PunishmentModule;
import com.sylluxpvp.circuit.bukkit.profile.BukkitProfile;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import com.sylluxpvp.circuit.shared.tools.string.CC;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias(value="userperm|uperm|userpermission")
@CommandPermission(value="circuit.command.userperm")
public class UserPermissionCommand extends BaseCommand {
    private static final String DEGRADED_MESSAGE = "&cPermission system is temporarily unavailable. Please try again later.";

    @HelpCommand
    @Syntax(value="[page]")
    public void doHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }

    @Subcommand(value="add|give")
    @CommandCompletion(value="@players")
    @Syntax(value="<player> <permission>")
    @Description(value="Add a permission to a player")
    public void onAdd(CommandSender sender, @Name(value="player") String playerName, @Name(value="permission") String permission) {
        if (!this.canWrite(sender)) {
            return;
        }
        this.resolveProfile(playerName, sender, profile -> {
            if (profile.hasPermission(permission)) {
                sender.sendMessage(CC.translate("&c" + profile.getName() + " already has the permission: " + permission));
                return;
            }
            (ServiceContainer.getService(ProfileService.class).saveWithPendingPermission(profile, permission, true).thenAccept(success -> Bukkit.getScheduler().runTask(CircuitPlugin.getInstance(), () -> {
                if (success) {
                    profile.addPermission(permission);
                    sender.sendMessage(CC.translate("&aAdded permission &f" + permission + " &ato &f" + profile.getName() + "&a."));
                    CircuitPlugin.getInstance().getLogger().info(this.getSenderName(sender) + " added permission " + permission + " to " + profile.getName());
                    this.refreshPlayerPermissions(profile);
                } else {
                    sender.sendMessage(CC.translate("&cFailed to save. Database may be unavailable."));
                }
            }))).exceptionally(ex -> {
                Bukkit.getScheduler().runTask(CircuitPlugin.getInstance(), () -> sender.sendMessage(CC.translate("&cSystem unavailable. Please try again later.")));
                return null;
            });
        });
    }

    @Subcommand(value="remove|take")
    @CommandCompletion(value="@players")
    @Syntax(value="<player> <permission>")
    @Description(value="Remove a permission from a player")
    public void onRemove(CommandSender sender, @Name(value="player") String playerName, @Name(value="permission") String permission) {
        if (!this.canWrite(sender)) {
            return;
        }
        this.resolveProfile(playerName, sender, profile -> {
            if (!profile.hasPermission(permission)) {
                sender.sendMessage(CC.translate("&c" + profile.getName() + " doesn't have the permission: " + permission));
                return;
            }
            (ServiceContainer.getService(ProfileService.class).saveWithPendingPermission(profile, permission, false).thenAccept(success -> Bukkit.getScheduler().runTask(CircuitPlugin.getInstance(), () -> {
                if (success) {
                    profile.removePermission(permission);
                    sender.sendMessage(CC.translate("&cRemoved permission &f" + permission + " &cfrom &f" + profile.getName() + "&c."));
                    CircuitPlugin.getInstance().getLogger().info(this.getSenderName(sender) + " removed permission " + permission + " from " + profile.getName());
                    this.refreshPlayerPermissions(profile);
                } else {
                    sender.sendMessage(CC.translate("&cFailed to save. Database may be unavailable."));
                }
            }))).exceptionally(ex -> {
                Bukkit.getScheduler().runTask(CircuitPlugin.getInstance(), () -> sender.sendMessage(CC.translate("&cSystem unavailable. Please try again later.")));
                return null;
            });
        });
    }

    @Subcommand(value="list|info")
    @CommandCompletion(value="@players")
    @Syntax(value="<player>")
    @Description(value="List a player's individual permissions")
    public void onList(CommandSender sender, @Name(value="player") String playerName) {
        this.resolveProfile(playerName, sender, profile -> {
            sender.sendMessage(CC.translate(""));
            sender.sendMessage(CC.translate("&9&lPermissions for " + profile.getName()));
            sender.sendMessage(CC.translate(""));
            if (profile.getPermissions().isEmpty()) {
                sender.sendMessage(CC.translate("&7No individual permissions assigned."));
            } else {
                sender.sendMessage(CC.translate("&fTotal: &9" + profile.getPermissions().size()));
                for (String perm : profile.getPermissions()) {
                    if (perm.startsWith("-")) {
                        sender.sendMessage(CC.translate("&c- " + perm));
                        continue;
                    }
                    sender.sendMessage(CC.translate("&a+ " + perm));
                }
            }
        });
    }

    @Subcommand(value="menu|gui")
    @CommandCompletion(value="@players")
    @Syntax(value="<player>")
    @Description(value="Open the permissions menu for a player")
    public void onMenu(Player sender, @Name(value="player") String playerName) {
        this.resolveProfile(playerName, sender, profile -> new UserPermissionsMenu(profile).openMenu(sender));
    }

    @Subcommand(value="clear")
    @CommandCompletion(value="@players")
    @Syntax(value="<player>")
    @Description(value="Clear all individual permissions from a player")
    public void onClear(CommandSender sender, @Name(value="player") String playerName) {
        if (!this.canWrite(sender)) {
            return;
        }
        this.resolveProfile(playerName, sender, profile -> {
            int count = profile.getPermissions().size();
            if (count == 0) {
                sender.sendMessage(CC.translate("&c" + profile.getName() + " has no permissions to clear."));
                return;
            }
            sender.sendMessage(CC.translate("&7Processing..."));
            (ServiceContainer.getService(ProfileService.class).saveWithClearedPermissions(profile).thenAccept(success -> Bukkit.getScheduler().runTask(CircuitPlugin.getInstance(), () -> {
                if (success) {
                    profile.getPermissions().clear();
                    sender.sendMessage(CC.translate("&cCleared &f" + count + " &cpermissions from &f" + profile.getName() + "&c."));
                    CircuitPlugin.getInstance().getLogger().info(this.getSenderName(sender) + " cleared all permissions from " + profile.getName());
                    this.refreshPlayerPermissions(profile);
                } else {
                    sender.sendMessage(CC.translate("&cFailed to save. Database may be unavailable."));
                }
            }))).exceptionally(ex -> {
                Bukkit.getScheduler().runTask(CircuitPlugin.getInstance(), () -> sender.sendMessage(CC.translate("&cSystem unavailable. Please try again later.")));
                return null;
            });
        });
    }

    private void resolveProfile(String playerName, CommandSender sender, Consumer<Profile> callback) {
        Player onlinePlayer = Bukkit.getPlayer(playerName);
        if (onlinePlayer != null) {
            Profile profile = ServiceContainer.getService(ProfileService.class).find(onlinePlayer.getUniqueId());
            if (profile != null) {
                callback.accept(profile);
            } else {
                sender.sendMessage(CC.translate("&cProfile not found for online player: " + playerName));
            }
            return;
        }
        sender.sendMessage(CC.translate("&7Looking up player " + playerName + "..."));
        this.fetchUUID(playerName).thenAccept(uuid -> {
            if (uuid == null) {
                sender.sendMessage(CC.translate("&cCould not find player: " + playerName));
                return;
            }
            ServiceContainer.getService(ProfileService.class).loadAsync(uuid).thenAccept(profile -> {
                if (profile == null) {
                    sender.sendMessage(CC.translate("&cProfile not found for: " + playerName));
                    return;
                }
                Bukkit.getScheduler().runTask(CircuitPlugin.getInstance(), () -> callback.accept(profile));
            });
        });
    }

    private void refreshPlayerPermissions(Profile profile) {
        Player player = Bukkit.getPlayer(profile.getUUID());
        if (player == null || !player.isOnline()) return;
        BukkitProfile.applyPermissions(player, profile);
    }

    private boolean canWrite(CommandSender sender) {
        PunishmentModule punishmentModule = CircuitPlugin.getInstance().getModuleManager().getModule(PunishmentModule.class);
        if (punishmentModule != null && !punishmentModule.canWrite()) {
            sender.sendMessage(CC.translate(DEGRADED_MESSAGE));
            return false;
        }
        return true;
    }

    private String getSenderName(CommandSender sender) {
        return sender instanceof Player ? sender.getName() : "Console";
    }

    private CompletableFuture<UUID> fetchUUID(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    String line;
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
                    String uuidString = json.get("id").getAsString();
                    String formattedUuid = uuidString.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
                    return UUID.fromString(formattedUuid);
                }
            } catch (Exception e) {
                CircuitPlugin.getInstance().getLogger().warning("Failed to fetch UUID for " + playerName + ": " + e.getMessage());
            }
            return null;
        });
    }
}

