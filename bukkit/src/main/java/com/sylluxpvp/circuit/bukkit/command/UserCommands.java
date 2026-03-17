package com.sylluxpvp.circuit.bukkit.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.sylluxpvp.circuit.shared.CircuitShared;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import com.sylluxpvp.circuit.shared.tools.string.CC;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

@CommandAlias("users|user")
@CommandPermission("circuit.cmd.user")
public class UserCommands extends BaseCommand {

    private final ProfileService profileService = ServiceContainer.getService(ProfileService.class);

    @Subcommand("help")
    @CatchUnknown
    @Default
    public void doHelp(CommandSender sender) {
        List<String> message = Arrays.asList(
                "&8&m-----------------------------",
                "&9/user addperm <name> <permission>",
                "&9/user removeperm <name> <permission>",
                "&9/user info <name>",
                "&9/user listperm <name>",
                "&9/user clearperm <name>",
                "&8&m-----------------------------"
        );

        for (String s : message) {
            sender.sendMessage(CC.translate(s));
        }
    }

    @Subcommand("addperm")
    @Description("Gives a permission to the player.")
    @CommandPermission("circuit.user.addperm")
    @Syntax("<name> <permission>")
    public void addperm(CommandSender sender, String name, String permission) {
        Profile profile = profileService.getProfileByName(name);

        if (profile == null) {
            sender.sendMessage(CC.translate("&cPlayer not found."));
            return;
        }

        if (profile.getPermissions().contains(permission)) {
            sender.sendMessage(CC.translate("&cThis player already has this permission."));
            return;
        }

        profile.getPermissions().add(permission);
        profileService.save(profile);

        sender.sendMessage(CC.translate("&aSuccessfully added permission &e" + permission + " &ato &e" + profile.getName()));

        Player target = Bukkit.getPlayer(profile.getUUID());
        if (target != null && target.isOnline()) {
            target.sendMessage(CC.translate("&aYour permissions has been updated."));
        }

        CircuitShared.getInstance().getLogger().log(sender.getName() + " added permission " + permission + " to " + profile.getName());
    }

    @Subcommand("removeperm")
    @Description("Removes a permission from the player.")
    @CommandPermission("circuit.user.removeperm")
    @Syntax("<name> <permission>")
    public void removeperm(CommandSender sender, String name, String permission) {
        Profile profile = profileService.getProfileByName(name);

        if (profile == null) {
            sender.sendMessage(CC.translate("&cPlayer not found."));
            return;
        }

        if (!profile.getPermissions().contains(permission)) {
            sender.sendMessage(CC.translate("&cThis player doesn't have this permission."));
            return;
        }

        profile.getPermissions().remove(permission);
        profileService.save(profile);

        sender.sendMessage(CC.translate("&cSuccessfully removed permission &e" + permission + " &cfrom &e" + profile.getName()));

        Player target = Bukkit.getPlayer(profile.getUUID());
        if (target != null && target.isOnline()) {
            target.sendMessage(CC.translate("&aYour permissions has been updated."));
        }

        CircuitShared.getInstance().getLogger().log(sender.getName() + " removed permission " + permission + " from " + profile.getName());
    }

    @Subcommand("info")
    @Description("Shows information about a player.")
    @CommandPermission("circuit.user.info")
    @Syntax("<name>")
    public void info(CommandSender sender, String name) {
        Profile profile = profileService.getProfileByName(name);

        if (profile == null) {
            sender.sendMessage(CC.translate("&cPlayer not found."));
            return;
        }

        sender.sendMessage(CC.translate("&8&m-----------------------------"));
        sender.sendMessage(CC.translate("&9Player: &f" + profile.getName()));
        sender.sendMessage(CC.translate("&9UUID: &f" + profile.getUUID()));
        sender.sendMessage(CC.translate("&9Color: &f" + profile.getColor() + "Color"));
        sender.sendMessage(CC.translate("&9Coins: &f" + profile.getCoins()));
        sender.sendMessage(CC.translate("&9VIP Status: " + (profile.hasSubscription() ? "&aYes" : "&cNo")));
        sender.sendMessage(CC.translate("&9Current Rank: &f" + (profile.getCurrentGrant() != null && profile.getCurrentGrant().getData() != null ? profile.getCurrentGrant().getData().getName() : "None")));
        sender.sendMessage(CC.translate("&9Active Tag: &f" + (profile.getActiveTag() != null ? profile.getActiveTag().getName() : "None")));
        sender.sendMessage(CC.translate("&9Permissions (&e" + profile.getPermissions().size() + "&9):"));

        if (profile.getPermissions().isEmpty()) {
            sender.sendMessage(CC.translate("  &7None"));
        } else {
            for (String perm : profile.getPermissions()) {
                sender.sendMessage(CC.translate("  &7- &f" + perm));
            }
        }

        sender.sendMessage(CC.translate("&8&m-----------------------------"));
    }

    @Subcommand("clearperm")
    @Description("Removes all permissions from a player.")
    @CommandPermission("circuit.user.clearperm")
    @Syntax("<name>")
    public void clearperm(CommandSender sender, String name) {
        Profile profile = profileService.getProfileByName(name);

        if (profile == null) {
            sender.sendMessage(CC.translate("&cPlayer not found."));
            return;
        }

        if (profile.getPermissions().isEmpty()) {
            sender.sendMessage(CC.translate("&cThis player has no permissions to clear."));
            return;
        }

        int count = profile.getPermissions().size();
        profile.getPermissions().clear();
        profileService.save(profile);

        sender.sendMessage(CC.translate("&aSuccessfully cleared &e" + count + " &apermission(s) from &e" + profile.getName()));

        Player target = Bukkit.getPlayer(profile.getUUID());
        if (target != null && target.isOnline()) {
            target.sendMessage(CC.translate("&cAll your custom permissions have been cleared."));
        }

        CircuitShared.getInstance().getLogger().log(sender.getName() + " cleared all permissions from " + profile.getName());
    }

    @Subcommand("listperm")
    @Description("Lists all permissions of a player.")
    @CommandPermission("circuit.user.listperm")
    @Syntax("<name>")
    public void listperm(CommandSender sender, String name) {
        Profile profile = profileService.getProfileByName(name);

        if (profile == null) {
            sender.sendMessage(CC.translate("&cPlayer not found."));
            return;
        }

        sender.sendMessage(CC.translate("&8&m-----------------------------"));
        sender.sendMessage(CC.translate("&9Permissions for &e" + profile.getName() + " &9(&e" + profile.getPermissions().size() + "&9):"));

        if (profile.getPermissions().isEmpty()) {
            sender.sendMessage(CC.translate("  &7This player has no custom permissions."));
        } else {
            int index = 1;
            for (String perm : profile.getPermissions()) {
                sender.sendMessage(CC.translate("  &9" + index + ". &f" + perm));
                index++;
            }
        }

        sender.sendMessage(CC.translate("&8&m-----------------------------"));
    }
}