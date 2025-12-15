package com.sylluxpvp.circuit.bukkit.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.bukkit.menus.tag.TagListMenu;
import com.sylluxpvp.circuit.bukkit.menus.tag.TagEditorMenu;
import com.sylluxpvp.circuit.shared.tag.Tag;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.TagService;
import com.sylluxpvp.circuit.shared.tools.string.CC;

@CommandAlias("tag")
@CommandPermission("circuit.cmd.tag")
public class TagCommands extends BaseCommand {

    @Subcommand("help")
    @CatchUnknown
    @Default
    public void doHelp(CommandSender sender) {
        sender.sendMessage(CC.translate("&8&m-----------------------------"));
        sender.sendMessage(CC.translate("&9/tag create <name>"));
        sender.sendMessage(CC.translate("&9/tag delete <tag>"));
        sender.sendMessage(CC.translate("&9/tag display <tag> <display>"));
        sender.sendMessage(CC.translate("&9/tag permission <tag> <permission>"));
        sender.sendMessage(CC.translate("&9/tag purchasable <tag>"));
        sender.sendMessage(CC.translate("&9/tag info <tag>"));
        sender.sendMessage(CC.translate("&9/tag list"));
        sender.sendMessage(CC.translate("&9/tag editor"));
        sender.sendMessage(CC.translate("&8&m-----------------------------"));
    }

    @Subcommand("create")
    @Description("Creates a new tag.")
    @CommandPermission("circuit.tag.create")
    @Syntax("<name>")
    public void create(Player player, String name) {
        TagService service = ServiceContainer.getService(TagService.class);
        if (service.getTag(name) != null) {
            player.sendMessage(CC.translate("&cThis tag already exists."));
            return;
        }

        Tag tag = service.create(name, player.getUniqueId());
        service.save(tag);
        player.sendMessage(CC.translate("&aTag created with name " + name + "!"));
    }

    @Subcommand("delete")
    @Description("Deletes a tag.")
    @CommandPermission("circuit.tag.delete")
    @CommandCompletion("@tags")
    @Syntax("<tag>")
    public void delete(Player player, String tagName) {
        TagService service = ServiceContainer.getService(TagService.class);
        Tag tag = service.getTag(tagName);
        if (tag == null) {
            player.sendMessage(CC.translate("&cThis tag doesn't exist."));
            return;
        }

        service.delete(tag);
        player.sendMessage(CC.translate("&aTag deleted!"));
    }

    @Subcommand("display")
    @Description("Sets a tag's display.")
    @CommandPermission("circuit.tag.display")
    @CommandCompletion("@tags @nothing")
    @Syntax("<tag> <display>")
    public void display(Player player, String tagName, String display) {
        TagService service = ServiceContainer.getService(TagService.class);
        Tag tag = service.getTag(tagName);
        if (tag == null) {
            player.sendMessage(CC.translate("&cThis tag doesn't exist."));
            return;
        }

        tag.setDisplay(display);
        service.save(tag);
        player.sendMessage(CC.translate("&aYou set " + tag.getName() + "'s display to: " + CC.translate(display)));
    }

    @Subcommand("permission")
    @Description("Sets a tag's permission.")
    @CommandPermission("circuit.tag.permission")
    @CommandCompletion("@tags @nothing")
    @Syntax("<tag> <permission>")
    public void permission(Player player, String tagName, String permission) {
        TagService service = ServiceContainer.getService(TagService.class);
        Tag tag = service.getTag(tagName);
        if (tag == null) {
            player.sendMessage(CC.translate("&cThis tag doesn't exist."));
            return;
        }

        tag.setPermission(permission);
        service.save(tag);
        player.sendMessage(CC.translate("&aYou set " + tag.getName() + "'s permission to " + permission + "!"));
    }

    @Subcommand("purchasable")
    @Description("Toggles a tag's purchasable mode.")
    @CommandPermission("circuit.tag.purchasable")
    @CommandCompletion("@tags")
    @Syntax("<tag>")
    public void purchasable(Player player, String tagName) {
        TagService service = ServiceContainer.getService(TagService.class);
        Tag tag = service.getTag(tagName);
        if (tag == null) {
            player.sendMessage(CC.translate("&cThis tag doesn't exist."));
            return;
        }

        tag.setPurchasable(!tag.isPurchasable());
        service.save(tag);
        player.sendMessage(CC.translate("&aYou set " + tag.getName() + "'s purchasable mode to " + tag.isPurchasable() + "!"));
    }

    @Subcommand("info")
    @Description("Shows a tag's info.")
    @CommandPermission("circuit.tag.info")
    @CommandCompletion("@tags")
    @Syntax("<tag>")
    public void info(Player player, String tagName) {
        TagService service = ServiceContainer.getService(TagService.class);
        Tag tag = service.getTag(tagName);
        if (tag == null) {
            player.sendMessage(CC.translate("&cThis tag doesn't exist."));
            return;
        }

        player.sendMessage(" ");
        player.sendMessage(CC.translate("&9&lTag Info:"));
        player.sendMessage(CC.translate("&fName: &9" + tag.getName()));
        player.sendMessage(CC.translate("&fDisplay: " + tag.getDisplay()));
        player.sendMessage(CC.translate("&fPermission: &9" + tag.getPermission()));
        player.sendMessage(CC.translate("&fPurchasable: &9" + tag.isPurchasable()));
        player.sendMessage(" ");
    }

    @Subcommand("list")
    @Description("Lists all tags.")
    @CommandPermission("circuit.tag.list")
    public void list(Player player) {
        TagService service = ServiceContainer.getService(TagService.class);
        player.sendMessage(" ");
        if (service.getTags().isEmpty()) {
            player.sendMessage(CC.translate("&cNo tags found."));
        } else {
            player.sendMessage(CC.translate("&9&lTags:"));
            for (Tag tag : service.getSortedTags()) {
                player.sendMessage(CC.translate("&7- " + tag.getDisplay() + " &7(" + tag.getName() + ")"));
            }
        }
        player.sendMessage(" ");
    }

    @Subcommand("editor")
    @Description("Opens the tag editor menu.")
    @CommandPermission("circuit.tag.editor")
    public void editor(Player player) {
        new TagListMenu().openMenu(player);
    }

    @Subcommand("edit")
    @Description("Opens the editor for a specific tag.")
    @CommandPermission("circuit.tag.editor")
    @CommandCompletion("@tags")
    @Syntax("<tag>")
    public void edit(Player player, String tagName) {
        TagService service = ServiceContainer.getService(TagService.class);
        Tag tag = service.getTag(tagName);
        if (tag == null) {
            player.sendMessage(CC.translate("&cThis tag doesn't exist."));
            return;
        }

        new TagEditorMenu(tag).openMenu(player);
    }

    @Subcommand("reload")
    @Description("Reloads all tags from database")
    @CommandPermission("circuit.tag.reload")
    public void reload(Player player) {
        TagService service = ServiceContainer.getService(TagService.class);
        service.getTags().clear();
        service.loadAll();
        player.sendMessage(CC.translate("&aTags reloaded from database! Loaded " + service.getTags().size() + " tags."));
    }
}
