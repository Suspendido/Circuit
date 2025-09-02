package xyz.kayaaa.xenon.bukkit.menus.grant.prompt;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import xyz.kayaaa.xenon.bukkit.menus.grant.buttons.ProcedureButton;
import xyz.kayaaa.xenon.bukkit.service.BukkitGrantService;
import xyz.kayaaa.xenon.bukkit.tools.menu.Button;
import xyz.kayaaa.xenon.bukkit.tools.menu.menus.ConfirmMenu;
import xyz.kayaaa.xenon.shared.grant.GrantProcedure;
import xyz.kayaaa.xenon.shared.rank.Rank;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.tools.string.CC;

import java.util.UUID;

public class ReasonPrompt extends StringPrompt {

    private final GrantProcedure<Rank> procedure;
    private final UUID target;

    public ReasonPrompt(GrantProcedure<Rank> procedure, UUID target) {
        this.procedure = procedure;
        this.target = target;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return CC.translate("&aPlease type the reason for this grant:");
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        Player player = (Player) context.getForWhom();
        procedure.setReason(input);
        context.getForWhom().sendRawMessage(CC.translate("&aReason set to: &f" + input));
        new ConfirmMenu(
                "&9Confirm grant?",
                result -> {
                    if (result) {
                        ServiceContainer.getService(BukkitGrantService.class).applyGrant(player.getUniqueId(), target, procedure);
                        Button.playSuccess(player);
                    } else {
                        player.sendMessage(CC.RED + "You cancelled the grant procedure.");
                        Button.playFail(player);
                    }
                },
                true,
                new ProcedureButton(procedure)
        ).openMenu(player);
        return END_OF_CONVERSATION;
    }
}
