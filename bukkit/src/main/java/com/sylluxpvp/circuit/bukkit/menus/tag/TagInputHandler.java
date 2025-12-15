package com.sylluxpvp.circuit.bukkit.menus.tag;

import lombok.RequiredArgsConstructor;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.bukkit.tools.menu.Menu;
import com.sylluxpvp.circuit.shared.tag.Tag;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.TagService;
import com.sylluxpvp.circuit.shared.tools.string.CC;

@RequiredArgsConstructor
public class TagInputHandler {

    private final Player player;
    private final Tag tag;
    private final InputType inputType;
    private final Menu returnMenu;

    public void start() {
        ConversationFactory factory = new ConversationFactory(CircuitPlugin.getInstance())
                .withFirstPrompt(new InputPrompt())
                .withLocalEcho(false)
                .withTimeout(60);

        player.beginConversation(factory.buildConversation(player));
    }

    private class InputPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            switch (inputType) {
                case CREATE:
                    return CC.translate("&aEnter the name for the new tag (or 'cancel' to cancel):");
                case DISPLAY:
                    return CC.translate("&aEnter the new display for the tag (or 'cancel' to cancel):");
                case PERMISSION:
                    return CC.translate("&aEnter the new permission for the tag (or 'cancel' to cancel):");
                default:
                    return CC.translate("&aEnter a value (or 'cancel' to cancel):");
            }
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("cancel")) {
                player.sendMessage(CC.translate("&cCancelled."));
                CircuitPlugin.getInstance().getServer().getScheduler().runTask(
                        CircuitPlugin.getInstance(), () -> returnMenu.openMenu(player));
                return Prompt.END_OF_CONVERSATION;
            }

            TagService service = ServiceContainer.getService(TagService.class);

            switch (inputType) {
                case CREATE:
                    if (service.getTag(input) != null) {
                        player.sendMessage(CC.translate("&cA tag with that name already exists!"));
                        CircuitPlugin.getInstance().getServer().getScheduler().runTask(
                                CircuitPlugin.getInstance(), () -> returnMenu.openMenu(player));
                        return Prompt.END_OF_CONVERSATION;
                    }
                    Tag newTag = service.create(input, player.getUniqueId());
                    service.save(newTag);
                    player.sendMessage(CC.translate("&aTag '" + input + "' created successfully!"));
                    CircuitPlugin.getInstance().getServer().getScheduler().runTask(
                            CircuitPlugin.getInstance(), () -> new TagEditorMenu(newTag).openMenu(player));
                    break;

                case DISPLAY:
                    tag.setDisplay(input);
                    service.save(tag);
                    player.sendMessage(CC.translate("&aDisplay updated to: " + input));
                    CircuitPlugin.getInstance().getServer().getScheduler().runTask(
                            CircuitPlugin.getInstance(), () -> new TagEditorMenu(tag).openMenu(player));
                    break;

                case PERMISSION:
                    tag.setPermission(input);
                    service.save(tag);
                    player.sendMessage(CC.translate("&aPermission updated to: " + input));
                    CircuitPlugin.getInstance().getServer().getScheduler().runTask(
                            CircuitPlugin.getInstance(), () -> new TagEditorMenu(tag).openMenu(player));
                    break;
            }

            return Prompt.END_OF_CONVERSATION;
        }
    }

    public enum InputType {
        CREATE,
        DISPLAY,
        PERMISSION
    }
}
