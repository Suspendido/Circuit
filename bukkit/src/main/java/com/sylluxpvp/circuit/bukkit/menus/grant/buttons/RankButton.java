package com.sylluxpvp.circuit.bukkit.menus.grant.buttons;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import com.sylluxpvp.circuit.bukkit.menus.grant.GrantMenu;
import com.sylluxpvp.circuit.bukkit.tools.menu.Button;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ColorMapping;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ItemBuilder;
import com.sylluxpvp.circuit.shared.grant.GrantProcedure;
import com.sylluxpvp.circuit.shared.rank.Rank;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RankButton extends Button {

    private final GrantProcedure<Rank> procedure;
    private final UUID target;
    private final Rank rank;

    public RankButton(GrantProcedure<Rank> procedure, UUID target, Rank rank) {
        this.procedure = procedure;
        this.target = target;
        this.rank = rank;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        Material dyeMaterial = ColorMapping.getDyeMaterial(rank.getColor());
        ItemBuilder builder = new ItemBuilder(dyeMaterial);
        builder.name(rank.getColor() + rank.getName() + (rank.isStaff() ? " &7(Staff)" : ""));
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("&9&lRank Information");
        lore.add("&fPrefix: " + (rank.getPrefix().isEmpty() ? "&cEmpty" : rank.getPrefix() + player.getName()));
        lore.add("&fSuffix: " + (rank.getSuffix().isEmpty() ? "&cEmpty" : player.getName() + rank.getSuffix()));
        lore.add("&fWeight: &9" + rank.getWeight());
        lore.add("");
        lore.add("&aClick to select!");
        builder.lore(lore);
        return builder.build();
    }

    @Override
    public void clicked(Player p, ClickType clickType) {
        procedure.setData(rank);
        Button.playSuccess(p);
        new GrantMenu(target, procedure, GrantMenu.Stage.DURATION).openMenu(p);
    }
}