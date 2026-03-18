package com.sylluxpvp.circuit.bukkit.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.bukkit.menus.rank.RankEditorMenu;
import com.sylluxpvp.circuit.bukkit.menus.rank.RankListMenu;
import com.sylluxpvp.circuit.shared.rank.Rank;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.RankService;
import com.sylluxpvp.circuit.shared.tools.string.CC;

@CommandAlias("rank")
@CommandPermission("circuit.cmd.rank")
public class RankCommands extends BaseCommand {

    @Subcommand("help")
    @CatchUnknown @Default
    public void doHelp(CommandSender sender) {
        sender.sendMessage(CC.translate("&8&m-----------------------------"));
        sender.sendMessage(CC.translate("&9/rank create <name>"));
        sender.sendMessage(CC.translate("&9/rank delete <rank>"));
        sender.sendMessage(CC.translate("&9/rank color <rank> <color>"));
        sender.sendMessage(CC.translate("&9/rank prefix <rank> <prefix>"));
        sender.sendMessage(CC.translate("&9/rank suffix <rank> <suffix>"));
        sender.sendMessage(CC.translate("&9/rank weight <rank> <weight>"));
        sender.sendMessage(CC.translate("&9/rank info <rank>"));
        sender.sendMessage(CC.translate("&9/rank list"));
        sender.sendMessage(CC.translate("&9/rank editor"));
        sender.sendMessage(CC.translate("&8&m-----------------------------"));
    }

    @Subcommand("create")
    @Description("Creates a new rank.")
    @CommandPermission("circuit.rank.create")
    @CommandCompletion("@nothing")
    @Syntax("<name>")
    public void create(Player player, String name) {
        RankService service = ServiceContainer.getService(RankService.class);
        if (service.getRank(name) != null) {
            player.sendMessage(CC.translate("&cThis rank already exists."));
            return;
        }

        Rank rank = service.create(name);
        service.save(rank);
        player.sendMessage(CC.translate("&aRank created with name " + name + "!"));
    }

    @Subcommand("delete")
    @Description("Deletes a rank.")
    @CommandPermission("circuit.rank.delete")
    @CommandCompletion("@ranks")
    @Syntax("<rank>")
    public void delete(Player player, Rank rank) {
        RankService service = ServiceContainer.getService(RankService.class);
        if (rank == null || service.getRank(rank.getName()) == null) {
            player.sendMessage(CC.translate("&cThis rank doesn't exist."));
            return;
        }

        if (rank.isDefaultRank()) {
            player.sendMessage(CC.translate("&cThis rank is the default rank, you cannot delete it."));
            return;
        }

        service.delete(rank);
        service.saveAll();
        player.sendMessage(CC.translate("&aRank deleted!"));
    }

    @Subcommand("color")
    @Description("Sets a rank's color.")
    @CommandPermission("circuit.rank.color")
    @CommandCompletion("@ranks @nothing")
    @Syntax("<rank> <color>")
    public void color(Player player, Rank rank, String color) {
        if (rank == null) {
            player.sendMessage(CC.translate("&cThis rank doesn't exist."));
            return;
        }

        rank.setColor(color);
        ServiceContainer.getService(RankService.class).save(rank);
        player.sendMessage(CC.translate("&aYou set " + rank.getName() + "'s color to: " + color + rank.getName()));
    }

    @Subcommand("prefix")
    @Description("Sets a rank's prefix.")
    @CommandPermission("circuit.rank.prefix")
    @CommandCompletion("@ranks")
    @Syntax("<rank> [prefix]")
    public void prefix(Player player, Rank rank, @Optional String prefix) {
        if (rank == null) {
            player.sendMessage(CC.translate("&cThis rank doesn't exist."));
            return;
        }

        if (prefix == null) {
            prefix = "";
        }

        rank.setPrefix(prefix);
        ServiceContainer.getService(RankService.class).save(rank);
        player.sendMessage(CC.translate("&aYou set " + rank.getName() + "'s prefix! &7(preview below)"));
        player.sendMessage(CC.translate(prefix + "Test" + rank.getSuffix() + "&7: &fHey!"));
    }

    @Subcommand("suffix")
    @Description("Sets a rank's suffix.")
    @CommandPermission("circuit.rank.suffix")
    @CommandCompletion("@ranks")
    @Syntax("<rank> [suffix]")
    public void suffix(Player player, Rank rank, @Optional String suffix) {
        if (rank == null) {
            player.sendMessage(CC.translate("&cThis rank doesn't exist."));
            return;
        }

        if (suffix == null) {
            suffix = "";
        }

        rank.setSuffix(suffix);
        ServiceContainer.getService(RankService.class).save(rank);
        player.sendMessage(CC.translate("&aYou set " + rank.getName() + "'s suffix! &7(preview below)"));
        player.sendMessage(CC.translate(rank.getPrefix() + "Test" + suffix + "&7: &fHey!"));
    }

    @Subcommand("staff")
    @Description("Sets a rank's staff mode.")
    @CommandPermission("circuit.rank.staff")
    @CommandCompletion("@ranks")
    @Syntax("<rank>")
    public void staff(Player player, Rank rank) {
        if (rank == null) {
            player.sendMessage(CC.translate("&cThis rank doesn't exist."));
            return;
        }

        rank.setStaff(!rank.isStaff());
        ServiceContainer.getService(RankService.class).save(rank);
        player.sendMessage(CC.translate("&aYou set " + rank.getName() + "'s staff mode to " + rank.isStaff() + "!"));
    }

    @Subcommand("hidden")
    @Description("Sets a rank's hidden mode.")
    @CommandPermission("circuit.rank.hidden")
    @CommandCompletion("@ranks")
    @Syntax("<rank>")
    public void hidden(Player player, Rank rank) {
        if (rank == null) {
            player.sendMessage(CC.translate("&cThis rank doesn't exist."));
            return;
        }

        rank.setHidden(!rank.isHidden());
        ServiceContainer.getService(RankService.class).save(rank);
        player.sendMessage(CC.translate("&aYou set " + rank.getName() + "'s hiddden mode to " + rank.isHidden() + "!"));
    }

    @Subcommand("purchasable")
    @Description("Sets a rank's purchasable mode.")
    @CommandPermission("circuit.rank.purchasable")
    @CommandCompletion("@ranks")
    @Syntax("<rank>")
    public void purchasable(Player player, Rank rank) {
        if (rank == null) {
            player.sendMessage(CC.translate("&cThis rank doesn't exist."));
            return;
        }

        rank.setPurchasable(!rank.isPurchasable());
        ServiceContainer.getService(RankService.class).save(rank);
        player.sendMessage(CC.translate("&aYou set " + rank.getName() + "'s purchasable mode to " + rank.isPurchasable() + "!"));
    }

    @Subcommand("default")
    @Description("Sets a rank's default mode.")
    @CommandPermission("circuit.rank.default")
    @CommandCompletion("@ranks")
    @Syntax("<rank>")
    public void rankDefault(Player player, Rank rank) {
        if (rank == null) {
            player.sendMessage(CC.translate("&cThis rank doesn't exist."));
            return;
        }

        RankService service = ServiceContainer.getService(RankService.class);
        Rank defaultRank = service.getRanks().stream().filter(Rank::isDefaultRank).findFirst().orElse(null);
        if (defaultRank != null && rank != defaultRank) {
            player.sendMessage(CC.translate("&c" + defaultRank.getColor() + defaultRank.getName() + " &cis the default rank, please disable it's default mode before updating " + rank.getColor() + rank.getName() + "!"));
            return;
        }

        rank.setDefaultRank(!rank.isDefaultRank());
        service.save(rank);
        player.sendMessage(CC.translate("&aYou set " + rank.getName() + "'s default mode to " + rank.isDefaultRank() + "!"));
    }

    @Subcommand("weight")
    @Description("Sets a rank's weight.")
    @CommandPermission("circuit.rank.weight")
    @CommandCompletion("@ranks @nothing")
    @Syntax("<rank> <weight>")
    public void weight(Player player, Rank rank, int weight) {
        if (rank == null) {
            player.sendMessage(CC.translate("&cThis rank doesn't exist."));
            return;
        }

        if (weight < 0 || weight == Integer.MAX_VALUE) {
            player.sendMessage(CC.translate("&cWeight needs to be a number."));
            return;
        }

        rank.setWeight(weight);
        ServiceContainer.getService(RankService.class).save(rank);
        player.sendMessage(CC.translate("&aYou set " + rank.getName() + "'s weight to " + weight + "!"));
    }

    @Subcommand("permission")
    @Description("Adds/removes a permission to a rank")
    @CommandPermission("circuit.rank.permission")
    @CommandCompletion("@ranks @nothing")
    @Syntax("<rank> <permission>")
    public void permission(Player player, Rank rank, String permission) {
        if (rank == null) {
            player.sendMessage(CC.translate("&cThis rank doesn't exist."));
            return;
        }

        rank.setPermission(permission);
        ServiceContainer.getService(RankService.class).save(rank);
        if (rank.hasPermission(permission)) {
            player.sendMessage(CC.translate("&aYou added " + permission + " to " + rank.getColor() + rank.getName() + "!"));
        } else {
            player.sendMessage(CC.translate("&cYou removed " + permission + " from " + rank.getColor() + rank.getName() + "!"));
        }
    }

    @Subcommand("info")
    @Description("Shows a rank info")
    @CommandPermission("circuit.rank.info")
    @CommandCompletion("@ranks")
    @Syntax("<rank>")
    public void info(Player player, Rank rank) {
        if (rank == null) {
            player.sendMessage(CC.translate("&cThis rank doesn't exist."));
            return;
        }

        player.sendMessage(" ");
        player.sendMessage(CC.translate("&9&lRank Info:"));
        player.sendMessage(CC.translate("&fName: " + rank.getColor() + rank.getName()));
        player.sendMessage(CC.translate("&fPrefix: " + (rank.getPrefix().isEmpty() ? "&cEmpty" : rank.getPrefix() + player.getName())));
        player.sendMessage(CC.translate("&fSuffix: " + (rank.getSuffix().isEmpty() ? "&cEmpty" : player.getName() + rank.getSuffix())));
        player.sendMessage(CC.translate("&fWeight: &9" + rank.getWeight()));
        player.sendMessage(" ");
        player.sendMessage(CC.translate("&9&lRank Properties:"));
        player.sendMessage(CC.translate("&fStaff: &9" + rank.isStaff()));
        player.sendMessage(CC.translate("&fDefault: &9" + rank.isDefaultRank()));
        player.sendMessage(CC.translate("&fHidden: &9" + rank.isHidden()));
        player.sendMessage(CC.translate("&fPurchasable: &9" + rank.isPurchasable()));
        player.sendMessage(" ");
        player.sendMessage(CC.translate("&9&lRank Permissions:"));
        if (rank.getPermissions().isEmpty()) {
            player.sendMessage(CC.translate("&cNo permissions!"));
        } else {
            rank.getPermissions().forEach(permission -> player.sendMessage(CC.translate("&f- &9" + permission)));
        }
        player.sendMessage(" ");
    }

    @Subcommand("list")
    @Description("Lists all ranks")
    @CommandPermission("circuit.rank.list")
    public void list(Player player) {
        RankService service = ServiceContainer.getService(RankService.class);
        player.sendMessage(" ");
        if (service.getRanks().isEmpty()) {
            Rank rank = service.getDefaultRank();
            player.sendMessage(CC.translate("&7- " + rank.getColor() + rank.getName() + " &7(" + rank.getWeight() + ")"));
        } else {
            for (Rank rank : service.getRanks()) {
                player.sendMessage(CC.translate("&7- " + rank.getColor() + rank.getName() + " &7(" + rank.getWeight() + ")"));
            }
        }
        player.sendMessage(" ");
    }

    @Subcommand("editor")
    @Description("Opens the rank editor menu")
    @CommandPermission("circuit.rank.editor")
    public void editor(Player player) {
        new RankListMenu().openMenu(player);
    }

    @Subcommand("edit")
    @Description("Opens the editor for a specific rank")
    @CommandPermission("circuit.rank.editor")
    @CommandCompletion("@ranks")
    @Syntax("<rank>")
    public void edit(Player player, Rank rank) {
        if (rank == null) {
            player.sendMessage(CC.translate("&cThis rank doesn't exist."));
            return;
        }

        new RankEditorMenu(rank).openMenu(player);
    }

    @Subcommand("reload")
    @Description("Reloads all ranks from database")
    @CommandPermission("circuit.rank.reload")
    public void reload(Player player) {
        RankService service = ServiceContainer.getService(RankService.class);
        service.getRanks().clear();
        service.loadAll();
        player.sendMessage(CC.translate("&aRanks reloaded from database! Loaded " + service.getRanks().size() + " ranks."));
    }

//    @Subcommand("import ranks")
//    @CommandPermission("circuit.rank.import")
//    public void importRanks(Player player) {
//        LuckPermsImport importer = new LuckPermsImport(CircuitPlugin.getInstance().getLogger());
//
//        if (!importer.isAvailable()) {
//            player.sendMessage(CC.translate("&cLuckPerms is not available."));
//            return;
//        }
//
//        player.sendMessage(CC.translate("&aImporting ranks from LuckPerms..."));
//        AsyncExecutor.runAsync(() -> {
//            importer.executeImportRanks();
//            player.sendMessage(CC.translate("&aRanks imported! Check console for details."));
//        });
//    }
//
//    @Subcommand("import users")
//    @CommandPermission("circuit.rank.import")
//    public void importUsers(Player player) {
//        LuckPermsImport importer = new LuckPermsImport(CircuitPlugin.getInstance().getLogger());
//
//        if (!importer.isAvailable()) {
//            player.sendMessage(CC.translate("&cLuckPerms is not available."));
//            return;
//        }
//
//        player.sendMessage(CC.translate("&aImporting users from LuckPerms, check console for progress..."));
//        AsyncExecutor.runAsync(() -> {
//            try {
//                importer.importUsers().get(5, TimeUnit.MINUTES);
//                player.sendMessage(CC.translate("&aUsers imported!"));
//            } catch (Exception e) {
//                player.sendMessage(CC.translate("&cImport failed, check console."));
//            }
//        });
//    }
}