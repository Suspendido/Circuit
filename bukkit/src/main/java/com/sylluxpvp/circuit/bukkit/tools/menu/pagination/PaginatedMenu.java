package com.sylluxpvp.circuit.bukkit.tools.menu.pagination;

import lombok.Getter;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.bukkit.tools.menu.Button;
import com.sylluxpvp.circuit.bukkit.tools.menu.Menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class PaginatedMenu extends Menu {

	@Getter private int page = 1; {
		setUpdateAfterClick(false);
	}

	@Override
	public String getTitle(Player player) {
		return getPrePaginatedTitle(player);
	}

	/**
	 * Changes the page number
	 *
	 * @param player player viewing the inventory
	 * @param mod    delta to modify the page number by
	 */
	public final void modPage(Player player, int mod) {
		page += mod;
		getButtons().clear();
		openMenu(player);
	}

	/**
	 * @param player player viewing the inventory
	 */
	public final int getPages(Player player) {
		int buttonAmount = getAllPagesButtons(player).size();

		if (buttonAmount == 0) {
			return 1;
		}

		return (int) Math.ceil(buttonAmount / (double) getMaxItemsPerPage(player));
	}

	@Override
	public final Map<Integer, Button> getButtons(Player player) {
		Map<Integer, Button> buttons = new HashMap<>();

		List<Integer> availableSlots = getAvailableSlots();
		int maxItemsPerPage = availableSlots.size();

		int minIndex = (page - 1) * maxItemsPerPage;
		int maxIndex = page * maxItemsPerPage;

		Map<Integer, Button> allButtons = getAllPagesButtons(player);
		List<Button> buttonList = new ArrayList<>(allButtons.values());

		int slotIndex = 0;
		for (int i = minIndex; i < maxIndex && i < buttonList.size() && slotIndex < availableSlots.size(); i++) {
			int slot = availableSlots.get(slotIndex);
			buttons.put(slot, buttonList.get(i));
			slotIndex++;
		}

		// navegação
		if (hasPrevious(player)) {
			buttons.put(0, new PageButton(-1, this));
		}
		if (hasNext(player)) {
			buttons.put(8, new PageButton(1, this));
		}

		// placeholders da barra superior
		for (int i = 1; i < 8; i++) {
			if (!buttons.containsKey(i)) {
				buttons.put(i, getPlaceholderButton());
			}
		}

		// borda só no final (não sobrescreve itens já colocados)
		fillBorder(buttons);

		// botões globais
		Map<Integer, Button> global = getGlobalButtons(player);
		if (global != null) {
			buttons.putAll(global);
		}

		return buttons;
	}

	public void fillBorder(Map<Integer, Button> buttons) {
		for (int i = 0; i < getSize(); i++) {
			if (isBorderSlot(i) && !buttons.containsKey(i)) {
				buttons.put(i, getPlaceholderButton());
			}
		}
	}

	private List<Integer> getAvailableSlots() {
		List<Integer> availableSlots = new ArrayList<>();

		for (int i = 0; i < getSize(); i++) {
			if (isBorderSlot(i)) continue;
			if (isReservedSlot(i)) continue;

			availableSlots.add(i);
		}

		return availableSlots;
	}

	/**
	 * Checks if there is a next page available
	 *
	 * @param player player viewing the inventory
	 * @return true if there is a next page
	 */
	public final boolean hasNext(Player player) {
		return page < getPages(player);
	}

	/**
	 * Checks if there is a previous page available
	 *
	 * @param player player viewing the inventory
	 * @return true if there is a previous page
	 */
	public final boolean hasPrevious(Player player) {
		return page > 1;
	}

	protected boolean isReservedSlot(int slot) {
		return slot == (this.getSize() - 5);
	}

	public int getMaxItemsPerPage(Player player) {
		return getAvailableSlots().size();
	}

	/**
	 * @param player player viewing the inventory
	 *
	 * @return a Map of button that returns items which will be present on all pages
	 */
	public Map<Integer, Button> getGlobalButtons(Player player) {
		return null;
	}

	/**
	 * @param player player viewing the inventory
	 *
	 * @return title of the inventory before the page number is added
	 */
	public abstract String getPrePaginatedTitle(Player player);

	/**
	 * @param player player viewing the inventory
	 *
	 * @return a map of button that will be paginated and spread across pages
	 */
	public abstract Map<Integer, Button> getAllPagesButtons(Player player);

}
