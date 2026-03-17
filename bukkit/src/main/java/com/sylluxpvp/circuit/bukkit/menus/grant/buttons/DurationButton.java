package com.sylluxpvp.circuit.bukkit.menus.grant.buttons;

import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.bukkit.menus.grant.GrantMenu;
import com.sylluxpvp.circuit.bukkit.menus.grant.prompt.DurationPrompt;
import com.sylluxpvp.circuit.bukkit.tools.menu.Button;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ItemBuilder;
import com.sylluxpvp.circuit.shared.grant.GrantProcedure;
import com.sylluxpvp.circuit.shared.rank.Rank;
import com.sylluxpvp.circuit.shared.tools.java.TimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DurationButton extends Button {

    private final GrantProcedure<Rank> procedure;
    private final UUID target;
    private final String duration;

    public DurationButton(GrantProcedure<Rank> procedure, UUID target, String duration) {
        this.procedure = procedure;
        this.target = target;
        this.duration = duration;
    }

    @Override
    public ItemStack getButtonItem(Player p) {
        boolean isCustom = duration.equalsIgnoreCase("Custom");
        boolean isPermanent = duration.equalsIgnoreCase("Permanent");

        ItemBuilder builder = new ItemBuilder(isCustom ? Material.NAME_TAG : (isPermanent ? Material.NETHER_STAR : Material.CLOCK));
        builder.name((isPermanent ? "&c&l" : "&9&l") + duration);

        List<String> lore = new ArrayList<>();
        lore.add("");
        if (isPermanent) {
            lore.add("&7This grant will never expire.");
        } else if (isCustom) {
            lore.add("&7Enter a custom duration.");
            lore.add("&7Example: 7d, 30d, 1h");
        }
        lore.add("&aClick to select!");

        builder.lore(lore);
        return builder.build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (duration.equalsIgnoreCase("Custom")) {
            player.closeInventory();
            ConversationFactory factory = new ConversationFactory(CircuitPlugin.getInstance())
                    .withFirstPrompt(new DurationPrompt(procedure, target))
                    .withLocalEcho(false)
                    .withTimeout(60)
                    .thatExcludesNonPlayersWithMessage("Only players can use this!");

            Conversation conv = factory.buildConversation(player);
            conv.begin();
        } else {
            long time = (duration.equalsIgnoreCase("permanent") || duration.equalsIgnoreCase("perm")) ? -1 : TimeUtils.parseTime(duration);
            procedure.setDuration(time);
            Button.playSuccess(player);
            new GrantMenu(target, procedure, GrantMenu.Stage.REASON).openMenu(player);
        }
    }
}
