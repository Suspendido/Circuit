package xyz.kayaaa.xenon.bukkit.menus.grant.buttons;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import xyz.kayaaa.xenon.bukkit.menus.grant.GrantMenu;
import xyz.kayaaa.xenon.bukkit.tools.menu.Button;
import xyz.kayaaa.xenon.bukkit.tools.spigot.ColorMapping;
import xyz.kayaaa.xenon.bukkit.tools.spigot.ItemBuilder;
import xyz.kayaaa.xenon.shared.grant.GrantProcedure;
import xyz.kayaaa.xenon.shared.rank.Rank;

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
        ItemBuilder builder = new ItemBuilder(Material.INK_SACK);
        builder.durability(ColorMapping.getItemDurability(rank.getColor()));
        builder.name(rank.getColor() + rank.getName() + (rank.isStaff() ? " &7(Staff)" : ""));
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("&9&lRank Information");
        lore.add("&fPrefix: " + (rank.getPrefix().isEmpty() ? "&cEmpty" : rank.getPrefix() + player.getName()));
        lore.add("&fSuffix: " + (rank.getSuffix().isEmpty() ? "&cEmpty" : player.getName() + rank.getSuffix()));
        lore.add("&fWeight: &9" + rank.getWeight());
        lore.add("");
        lore.add("&eClick to select!");
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
