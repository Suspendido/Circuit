package com.sylluxpvp.circuit.bukkit.command.player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.shared.grant.Grant;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.rank.Rank;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import com.sylluxpvp.circuit.shared.service.impl.RankService;
import com.sylluxpvp.circuit.shared.tools.string.CC;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@CommandAlias("list|online|players|who")
public class ListCommand extends BaseCommand {

    @Default
    public void onList(CommandSender sender) {
        RankService rankService = ServiceContainer.getService(RankService.class);
        ProfileService profileService = ServiceContainer.getService(ProfileService.class);

        String ranks = rankService.getRanks().stream()
                .sorted(Comparator.comparingInt(Rank::getWeight).reversed())
                .map(rank -> CC.translate(rank.getColor() + rank.getName()))
                .collect(Collectors.joining(CC.translate("&7, ")));

        sender.sendMessage(ranks);

        List<Player> sortedPlayers = Bukkit.getOnlinePlayers().stream()
                .sorted((p1, p2) -> {
                    Profile prof1 = profileService.find(p1.getUniqueId());
                    Profile prof2 = profileService.find(p2.getUniqueId());

                    int w1 = 0, w2 = 0;
                    if (prof1 != null) {
                        Grant<Rank> g1 = prof1.getCurrentGrant();
                        if (g1 != null && g1.getData() != null) w1 = g1.getData().getWeight();
                    }
                    if (prof2 != null) {
                        Grant<Rank> g2 = prof2.getCurrentGrant();
                        if (g2 != null && g2.getData() != null) w2 = g2.getData().getWeight();
                    }
                    return Integer.compare(w2, w1);
                })
                .collect(Collectors.toList());

        String playerList = sortedPlayers.stream()
                .map(player -> {
                    Profile profile = profileService.find(player.getUniqueId());
                    if (profile != null) {
                        Grant<Rank> grant = profile.getCurrentGrant();
                        if (grant != null && grant.getData() != null) {
                            return CC.translate(grant.getData().getColor() + player.getName());
                        }
                    }
                    return CC.translate("&7" + player.getName());
                })
                .collect(Collectors.joining(CC.translate("&7, ")));

        sender.sendMessage(CC.translate("&7(" + sortedPlayers.size() + "/" + Bukkit.getMaxPlayers() + ") [" + playerList + "&7]"));
    }
}
