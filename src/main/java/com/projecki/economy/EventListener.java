package com.projecki.economy;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @since February 22, 2022
 * @author Andavin
 */
public record EventListener(SimpleEconomy economy) implements Listener {

    @EventHandler
    public void onJoin(PlayerLoginEvent event) {
        economy.load(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        economy.save(event.getPlayer());
    }
}
