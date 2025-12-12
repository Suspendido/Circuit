package com.sylluxpvp.circuit.bukkit.menus.grant.buttons;

import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.bukkit.menus.grant.GrantConfirmMenu;
import com.sylluxpvp.circuit.bukkit.menus.grant.prompt.ReasonPrompt;
import com.sylluxpvp.circuit.bukkit.tools.menu.Button;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ItemBuilder;
import com.sylluxpvp.circuit.shared.grant.GrantProcedure;
import com.sylluxpvp.circuit.shared.rank.Rank;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReasonButton extends Button {

    private final GrantProcedure<Rank> procedure;
    private final UUID target;
    private final String reason;

    public ReasonButton(GrantProcedure<Rank> procedure, UUID target, String reason) {
        this.procedure = procedure;
        this.target = target;
        this.reason = reason;
    }

    @Override
    public ItemStack getButtonItem(Player p) {
        boolean isCustom = reason.equalsIgnoreCase("Custom");

        ItemBuilder builder = new ItemBuilder(isCustom ? Material.NAME_TAG : Material.PAPER);
        builder.name("&9&l" + reason);

        List<String> lore = new ArrayList<>();
        lore.add("");
        if (isCustom) {
            lore.add("&7Enter a custom reason.");
        }
        lore.add("&aClick to select!");

        builder.lore(lore);
        return builder.build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (reason.equalsIgnoreCase("Custom")) {
            player.closeInventory();
            ConversationFactory factory = new ConversationFactory(CircuitPlugin.getInstance())
                    .withFirstPrompt(new ReasonPrompt(procedure, target))
                    .withLocalEcho(false)
                    .withTimeout(60)
                    .thatExcludesNonPlayersWithMessage("Only players can use this!");

            Conversation conv = factory.buildConversation(player);
            conv.begin();
        } else {
            procedure.setReason(reason);
            Button.playSuccess(player);
            new GrantConfirmMenu(target, procedure).openMenu(player);
        }
    }
}
