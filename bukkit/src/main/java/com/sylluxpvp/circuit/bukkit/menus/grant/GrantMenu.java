package com.sylluxpvp.circuit.bukkit.menus.grant;

import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import com.sylluxpvp.circuit.bukkit.menus.grant.buttons.DurationButton;
import com.sylluxpvp.circuit.bukkit.menus.grant.buttons.RankButton;
import com.sylluxpvp.circuit.bukkit.menus.grant.buttons.ReasonButton;
import com.sylluxpvp.circuit.bukkit.tools.menu.Button;
import com.sylluxpvp.circuit.bukkit.tools.menu.pagination.PaginatedMenu;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ItemBuilder;
import com.sylluxpvp.circuit.shared.grant.GrantProcedure;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.rank.Rank;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import com.sylluxpvp.circuit.shared.service.impl.RankService;
import com.sylluxpvp.circuit.shared.tools.java.TimeUtils;

import java.util.*;
import java.util.stream.Collectors;

public class GrantMenu extends PaginatedMenu {

    private static final int MENU_SIZE = 45;

    public enum Stage { RANK, DURATION, REASON }

    @Setter
    private GrantProcedure<Rank> procedure;
    private final UUID target;
    private final Stage stage;

    public GrantMenu(UUID target) {
        this(target, new GrantProcedure<>(), Stage.RANK);
    }

    public GrantMenu(UUID target, GrantProcedure<Rank> procedure, Stage stage) {
        this.target = target;
        this.procedure = procedure;
        this.procedure.setTarget(target);
        this.stage = stage;
    }

    @Override
    public int getSize() {
        return MENU_SIZE;
    }

    @Override
    public String getPrePaginatedTitle(Player player) {
        switch (stage) {
            case RANK:
                return "&9Grant: Select Rank";
            case DURATION:
                return "&9Grant: Select Duration";
            case REASON:
                return "&9Grant: Select Reason";
            default:
                return "&9Grant";
        }
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(4, new ProgressButton());

        if (stage != Stage.RANK) {
            buttons.put(MENU_SIZE - 6, new BackButton());
        }
        buttons.put(MENU_SIZE - 5, new CancelButton());

        return buttons;
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        if (procedure.getAuthor() == null) procedure.setAuthor(player.getUniqueId());

        switch (stage) {
            case RANK:
                Comparator<Rank> comparator = Comparator.comparingInt(Rank::getWeight).reversed();
                for (Rank rank : ServiceContainer.getService(RankService.class).getRanks().stream().filter(rank -> !rank.isDefaultRank()).sorted(comparator).collect(Collectors.toList())) {
                    buttons.put(buttons.size(), new RankButton(procedure, target, rank));
                }
                break;

            case DURATION:
                List<String> durations = Arrays.asList("Permanent", "30d", "14d", "7d", "3d", "1d", "12h", "1h", "Custom");
                for (String d : durations) {
                    buttons.put(buttons.size(), new DurationButton(procedure, target, d));
                }
                break;

            case REASON:
                List<String> reasons = Arrays.asList("Promotion", "Purchased", "Event Winner", "Giveaway", "Staff", "Custom");
                for (String r : reasons) {
                    buttons.put(buttons.size(), new ReasonButton(procedure, target, r));
                }
                break;
        }
        return buttons;
    }

    private class ProgressButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            Profile targetProfile = ServiceContainer.getService(ProfileService.class).find(target);
            String targetName = targetProfile != null ? targetProfile.getName() : Bukkit.getOfflinePlayer(target).getName();

            ItemBuilder builder = new ItemBuilder(Material.SKULL_ITEM);
            builder.skull(targetName);
            builder.name("&9&lGrant Progress");

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&fTarget: &9" + targetName);
            lore.add("");

            lore.add(getStepLine("Select Rank", Stage.RANK, procedure.getData() != null ? procedure.getData().getColor() + procedure.getData().getName() : null));
            lore.add(getStepLine("Select Duration", Stage.DURATION, procedure.getDuration() != 0 ? (procedure.getDuration() == -1 ? "&cPermanent" : TimeUtils.formatTimeShort(procedure.getDuration())) : null));
            lore.add(getStepLine("Select Reason", Stage.REASON, procedure.getReason()));

            builder.lore(lore);
            return builder.build();
        }

        private String getStepLine(String stepName, Stage stepStage, String value) {
            boolean isCurrent = stage == stepStage;
            boolean isComplete = value != null;

            String icon = isComplete ? "&a✓" : (isCurrent ? "&e▶" : "&7○");
            String color = isCurrent ? "&e" : (isComplete ? "&a" : "&7");

            if (isComplete) {
                return icon + " " + color + stepName + "&7: " + value;
            }
            return icon + " " + color + stepName;
        }
    }

    private class BackButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.BED);
            builder.name("&c&lBack");

            List<String> lore = new ArrayList<>();
            lore.add("&7Go to previous step.");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Button.playNeutral(player);
            Stage previousStage = stage == Stage.REASON ? Stage.DURATION : Stage.RANK;
            new GrantMenu(target, procedure, previousStage).openMenu(player);
        }
    }

    private class CancelButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.REDSTONE);
            builder.name("&c&lCancel");

            List<String> lore = new ArrayList<>();
            lore.add("&7Cancel this grant.");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Button.playNeutral(player);
            player.closeInventory();
        }
    }
}
