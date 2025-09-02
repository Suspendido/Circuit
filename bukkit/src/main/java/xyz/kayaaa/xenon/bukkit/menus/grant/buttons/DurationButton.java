package xyz.kayaaa.xenon.bukkit.menus.grant.buttons;

import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import xyz.kayaaa.xenon.bukkit.XenonPlugin;
import xyz.kayaaa.xenon.bukkit.menus.grant.GrantMenu;
import xyz.kayaaa.xenon.bukkit.menus.grant.prompt.DurationPrompt;
import xyz.kayaaa.xenon.bukkit.tools.menu.Button;
import xyz.kayaaa.xenon.bukkit.tools.spigot.ItemBuilder;
import xyz.kayaaa.xenon.shared.grant.GrantProcedure;
import xyz.kayaaa.xenon.shared.rank.Rank;
import xyz.kayaaa.xenon.shared.tools.java.TimeUtils;

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
        return new ItemBuilder(Material.WATCH)
                .name("&7" + duration)
                .lore("", "&eClick to set duration!")
                .build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (duration.equalsIgnoreCase("Custom")) {
            player.closeInventory();
            ConversationFactory factory = new ConversationFactory(XenonPlugin.getInstance())
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
