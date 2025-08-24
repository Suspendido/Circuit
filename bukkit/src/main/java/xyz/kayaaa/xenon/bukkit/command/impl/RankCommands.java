package xyz.kayaaa.xenon.bukkit.command.impl;

import com.jonahseguin.drink.annotation.*;
import org.bukkit.entity.Player;
import xyz.kayaaa.xenon.bukkit.command.CommandBase;
import xyz.kayaaa.xenon.shared.rank.Rank;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.service.impl.RankService;
import xyz.kayaaa.xenon.shared.tools.string.CC;

public class RankCommands extends CommandBase {

    public RankCommands() {
        super("rank", true);
    }

    @Command(name = "create", desc = "Creates a new rank.", usage = "<name>")
    @Require("xenon.rank.create")
    public void create(@Sender Player player, String name) {
        RankService service = ServiceContainer.getService(RankService.class);
        if (service.getRank(name) != null) {
            player.sendMessage(CC.translate("&cThis rank already exists."));
            return;
        }

        Rank rank = service.create(name);
        service.save(rank);
        player.sendMessage(CC.translate("&aRank created with name " + name + "!"));
    }

    @Command(name = "delete", desc = "Deletes a rank.", usage = "<name>")
    @Require("xenon.rank.delete")
    public void delete(@Sender Player player, Rank rank) {
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

    @Command(name = "color", desc = "Sets a rank's color.", usage = "<color>")
    @Require("xenon.rank.color")
    public void color(@Sender Player player, Rank rank, String color) {
        if (rank == null) {
            player.sendMessage(CC.translate("&cThis rank doesn't exist."));
            return;
        }

        rank.setColor(color);
        ServiceContainer.getService(RankService.class).save(rank);
        player.sendMessage(CC.translate("&aYou set " + rank.getName() + "'s color to: " + color + rank.getName()));
    }

    @Command(name = "prefix", desc = "Sets a rank's prefix.", usage = "<prefix>")
    @Require("xenon.rank.prefix")
    public void prefix(@Sender Player player, Rank rank, @OptArg @Text String prefix) {
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

    @Command(name = "suffix", desc = "Sets a rank's suffix.", usage = "<suffix>")
    @Require("xenon.rank.suffix")
    public void suffix(@Sender Player player, Rank rank, @OptArg @Text String suffix) {
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

    @Command(name = "weight", desc = "Sets a rank's weight.", usage = "<weight>")
    @Require("xenon.rank.weight")
    public void weight(@Sender Player player, Rank rank, int weight) {
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

    @Command(name = "permission", desc = "Adds/removes a permission to a rank", usage = "<permission>")
    @Require("xenon.rank.permission")
    public void permission(@Sender Player player, Rank rank, String permission) {
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

    @Command(name = "info", desc = "Shows a rank info", usage = "<rank>")
    @Require("xenon.rank.info")
    public void info(@Sender Player player, Rank rank) {
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
        player.sendMessage(CC.translate("&9&lRank Booleans:"));
        player.sendMessage(CC.translate("&fStaff: &9" + rank.isStaff()));
        player.sendMessage(CC.translate("&fDefault: &9" + rank.isDefaultRank()));
        player.sendMessage(CC.translate("&fHidden: &9" + rank.isHidden()));
        player.sendMessage(CC.translate("&fPurchasable: &9" + rank.isPurchasable()));
        player.sendMessage(" ");
        player.sendMessage(CC.translate("&9&lRank Permissions:"));
        if (rank.getPermissions().isEmpty()) {
            player.sendMessage(CC.translate("&cNo permissions!"));
        } else {
            for (String permission : rank.getPermissions()) {
                player.sendMessage(CC.translate("&f- &9" + permission));
            }
        }
        player.sendMessage(" ");
    }

    @Command(name = "list", desc = "Lists all ranks")
    @Require("xenon.rank.list")
    public void list(@Sender Player player) {
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

}
