package com.sylluxpvp.circuit.bukkit.menus.grant.prompt;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.bukkit.menus.grant.GrantMenu;
import com.sylluxpvp.circuit.shared.grant.GrantProcedure;
import com.sylluxpvp.circuit.shared.rank.Rank;
import com.sylluxpvp.circuit.shared.tools.java.TimeUtils;
import com.sylluxpvp.circuit.shared.tools.string.CC;

import java.util.UUID;

public class DurationPrompt extends StringPrompt {

    private final GrantProcedure<Rank> procedure;
    private final UUID target;

    public DurationPrompt(GrantProcedure<Rank> procedure, UUID target) {
        this.procedure = procedure;
        this.target = target;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return CC.translate("&aPlease type the new duration for this grant:");
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        Player player = (Player) context.getForWhom();
        if (!TimeUtils.isTime(input)) {
            procedure.setDuration(TimeUtils.parseTime("30d"));
            context.getForWhom().sendRawMessage(CC.translate("&cYou used a invalid duration, defaulting to 30d..."));
            return END_OF_CONVERSATION;
        }
        procedure.setDuration(TimeUtils.parseTime(input));
        context.getForWhom().sendRawMessage(CC.translate("&aDuration set to: &f" + TimeUtils.normalize(input)));
        new GrantMenu(target, procedure, GrantMenu.Stage.REASON).openMenu(player);
        return END_OF_CONVERSATION;
    }
}
