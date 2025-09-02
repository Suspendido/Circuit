package xyz.kayaaa.xenon.bukkit.tools.menu.button;

import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import xyz.kayaaa.xenon.bukkit.tools.menu.Button;
import xyz.kayaaa.xenon.bukkit.tools.menu.Menu;
import xyz.kayaaa.xenon.bukkit.tools.spigot.ItemBuilder;
import xyz.kayaaa.xenon.shared.tools.string.CC;

import java.util.Arrays;

@AllArgsConstructor
public class BackButton extends Button {

	private Menu back;

	@Override
	public ItemStack getButtonItem(Player player) {
		return new ItemBuilder(Material.REDSTONE)
				.name(CC.RED + CC.BOLD + "Back")
				.lore(Arrays.asList(
						CC.RED + "Click here to return to",
						CC.RED + "the previous menu.")
				)
				.build();
	}

	@Override
	public void clicked(Player player, ClickType clickType) {
		Button.playNeutral(player);
		back.openMenu(player);
	}

}
