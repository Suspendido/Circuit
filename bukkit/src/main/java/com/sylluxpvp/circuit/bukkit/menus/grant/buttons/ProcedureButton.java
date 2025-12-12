package com.sylluxpvp.circuit.bukkit.menus.grant.buttons;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.sylluxpvp.circuit.bukkit.tools.menu.Button;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ItemBuilder;
import com.sylluxpvp.circuit.shared.grant.GrantProcedure;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.rank.Rank;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import com.sylluxpvp.circuit.shared.tools.java.TimeUtils;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ProcedureButton extends Button {

    private final GrantProcedure<Rank> procedure;

    @Override
    public ItemStack getButtonItem(Player player) {
        ItemBuilder builder = new ItemBuilder(Material.BOOK);
        builder.name("&9Grant Procedure");
        Profile target = ServiceContainer.getService(ProfileService.class).find(procedure.getTarget());
        Profile author = ServiceContainer.getService(ProfileService.class).find(procedure.getAuthor());
        if (target == null || author == null) {
            return builder.build();
        }

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("&fTarget: " + target.getCurrentGrant().getData().getColor() + target.getName());
        lore.add("&fAuthor: " + author.getCurrentGrant().getData().getColor() + author.getName());
        lore.add("");
        lore.add("&fRank: " + procedure.getData().getColor() + procedure.getData().getName());
        lore.add("&fDuration: &9" + (procedure.getDuration() == -1 ? "Permanent" : TimeUtils.formatTimeShort(procedure.getDuration())));
        lore.add("&fReason: &9" + procedure.getReason());
        builder.lore(lore);
        return builder.build();
    }
}
