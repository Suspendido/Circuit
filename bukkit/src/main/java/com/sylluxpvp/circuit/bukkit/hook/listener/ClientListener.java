package com.sylluxpvp.circuit.bukkit.hook.listener;

import com.sylluxpvp.circuit.bukkit.hook.ClientHook;
import com.sylluxpvp.circuit.bukkit.tools.spigot.TaskUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ClientListener implements Listener {
   private final ClientHook hook;

   @EventHandler
   private void onPlayerJoin(PlayerJoinEvent e) {
      TaskUtil.runTaskLater(() -> hook.overrideServerRichPresence(e.getPlayer()), 20L);
   }

   public ClientListener(ClientHook hook) {
      this.hook = hook;
   }
}
