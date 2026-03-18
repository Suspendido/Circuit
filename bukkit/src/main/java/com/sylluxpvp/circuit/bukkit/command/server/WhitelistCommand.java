package com.sylluxpvp.circuit.bukkit.command.server;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.shared.rank.Rank;
import com.sylluxpvp.circuit.shared.server.Server;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.RankService;
import com.sylluxpvp.circuit.shared.tools.string.CC;

import java.util.UUID;

@CommandAlias("whitelist|wl")
@CommandPermission("circuit.command.whitelist")
public class WhitelistCommand extends BaseCommand {

    @HelpCommand
    @Syntax("[page]")
    public void doHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("on|enable")
    @Description("Enable whitelist on this server")
    public void onEnable(CommandSender sender) {
        Server server = CircuitPlugin.getInstance().getShared().getServer();
        server.setWhitelisted(true);

        String executor = sender instanceof Player ? sender.getName() : "Console";

        Bukkit.broadcastMessage(CC.translate("&fWhitelist has been &aenabled&f."));
        CircuitPlugin.getInstance().getLogger().info(executor + " enabled whitelist");
    }

    @Subcommand("off|disable")
    @Description("Disable whitelist on this server")
    public void onDisable(CommandSender sender) {
        Server server = CircuitPlugin.getInstance().getShared().getServer();
        server.setWhitelisted(false);

        String executor = sender instanceof Player ? sender.getName() : "Console";

        Bukkit.broadcastMessage(CC.translate("&fWhitelist has been &cdisabled&f."));
        CircuitPlugin.getInstance().getLogger().info(executor + " disabled whitelist");
    }

    @Subcommand("rank|setrank")
    @CommandCompletion("@ranks")
    @Description("Set the minimum rank required to join when whitelisted")
    public void onSetRank(CommandSender sender, @Name("rank") String rankName) {
        Server server = CircuitPlugin.getInstance().getShared().getServer();
        RankService rankService = ServiceContainer.getService(RankService.class);
        
        Rank rank = rankService.getRank(rankName);
        if (rank == null) {
            sender.sendMessage(CC.translate("&cRank not found: " + rankName));
            return;
        }

        server.setWhitelistRank(rank.getName());
        sender.sendMessage(CC.translate("&fWhitelist rank set to " + rank.getColor() + rank.getName() + "&f."));
    }

    @Subcommand("add")
    @CommandCompletion("@players")
    @Description("Add a player to the whitelist")
    public void onAdd(CommandSender sender, @Name("player") String playerName) {
        Server server = CircuitPlugin.getInstance().getShared().getServer();

        Player target = Bukkit.getPlayer(playerName);
        UUID targetUuid;
        String targetName;

        if (target != null) {
            targetUuid = target.getUniqueId();
            targetName = target.getName();
        } else {
            @SuppressWarnings("deprecation")
            org.bukkit.OfflinePlayer offline = Bukkit.getOfflinePlayer(playerName);
            targetUuid = offline.getUniqueId();
            targetName = offline.getName() != null ? offline.getName() : playerName;
        }

        if (server.isPlayerWhitelisted(targetUuid)) {
            sender.sendMessage(CC.translate("&c" + targetName + " is already whitelisted."));
            return;
        }

        server.addWhitelistedPlayer(targetUuid);
        sender.sendMessage(CC.translate("&a" + targetName + " &fhas been added to the whitelist."));
    }

    @Subcommand("remove")
    @CommandCompletion("@players")
    @Description("Remove a player from the whitelist")
    public void onRemove(CommandSender sender, @Name("player") String playerName) {
        Server server = CircuitPlugin.getInstance().getShared().getServer();

        Player target = Bukkit.getPlayer(playerName);
        UUID targetUuid;
        String targetName;

        if (target != null) {
            targetUuid = target.getUniqueId();
            targetName = target.getName();
        } else {
            @SuppressWarnings("deprecation")
            org.bukkit.OfflinePlayer offline = Bukkit.getOfflinePlayer(playerName);
            targetUuid = offline.getUniqueId();
            targetName = offline.getName() != null ? offline.getName() : playerName;
        }

        if (!server.isPlayerWhitelisted(targetUuid)) {
            sender.sendMessage(CC.translate("&c" + targetName + " is not whitelisted."));
            return;
        }

        server.removeWhitelistedPlayer(targetUuid);
        sender.sendMessage(CC.translate("&c" + targetName + " &fhas been removed from the whitelist."));
    }

    @Subcommand("status|info")
    @Description("View whitelist status")
    public void onStatus(CommandSender sender) {
        Server server = CircuitPlugin.getInstance().getShared().getServer();
        RankService rankService = ServiceContainer.getService(RankService.class);

        String status = server.isWhitelisted() ? "&aEnabled" : "&cDisabled";
        Rank whitelistRank = rankService.getRank(server.getWhitelistRank());
        String rankDisplay = whitelistRank != null 
                ? whitelistRank.getColor() + whitelistRank.getName() 
                : "&7" + server.getWhitelistRank();

        sender.sendMessage(CC.translate(""));
        sender.sendMessage(CC.translate("&c&lWhitelist Status"));
        sender.sendMessage(CC.translate(""));
        sender.sendMessage(CC.translate("&fStatus: " + status));
        sender.sendMessage(CC.translate("&fMinimum Rank: " + rankDisplay));
        sender.sendMessage(CC.translate("&fWhitelisted Players: &9" + server.getWhitelistedPlayers().size()));

        if (!server.getWhitelistedPlayers().isEmpty() && server.getWhitelistedPlayers().size() <= 10) {
            StringBuilder players = new StringBuilder();
            for (UUID uuid : server.getWhitelistedPlayers()) {
                if (players.length() > 0) players.append("&7, ");
                String name = Bukkit.getOfflinePlayer(uuid).getName();
                players.append("&f").append(name != null ? name : uuid.toString().substring(0, 8));
            }
            sender.sendMessage(CC.translate("&fPlayers: " + players));
        }
    }

    @Subcommand("clear")
    @Description("Clear all whitelisted players")
    public void onClear(CommandSender sender) {
        Server server = CircuitPlugin.getInstance().getShared().getServer();
        int count = server.getWhitelistedPlayers().size();
        server.getWhitelistedPlayers().clear();
        sender.sendMessage(CC.translate("&fCleared &c" + count + " &fplayers from the whitelist."));
    }
}
