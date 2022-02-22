package com.projecki.economy;

import com.projecki.economy.data.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;

public final class SimpleEconomy extends JavaPlugin {

    private final Map<UUID, EconomyData> economyData = new ConcurrentHashMap<>();

    @Override
    public void onLoad() {
        Data.initialize(this.getDataFolder());
    }

    @Override
    public void onEnable() {

        Commands commands = new Commands(this);
        requireNonNull(this.getCommand("economy"))
                .setExecutor((sender, command, label, args) -> {
                    commands.eco(sender, args);
                    return true;
                });
        requireNonNull(this.getCommand("balance"))
                .setExecutor((sender, command, label, args) -> {
                    commands.balance(sender, args);
                    return true;
                });

        EventListener listener = new EventListener(this);
        Bukkit.getPluginManager().registerEvents(listener, this);
    }

    @Override
    public void onDisable() {
        Data.close();
    }

    public Optional<EconomyData> getData(UUID uuid) {
        return Optional.ofNullable(economyData.get(uuid));
    }

    public Optional<EconomyData> getData(String name) {
        return economyData.values().stream()
                .filter(e -> e.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    CompletionStage<EconomyData> load(String name) {
        return EconomyData.load(name).thenApply(d -> {

            if (d != null) {
                economyData.put(d.getUuid(), d);
            }

            return d;
        });
    }

    void load(Player player) {
        // Load only if there is no cache - could be changed if we expect direct
        // changes to the db or changes from another source (i.e. multi-server)
        if (!economyData.containsKey(player.getUniqueId())) {
            EconomyData.load(player).thenAccept(d -> economyData.put(d.getUuid(), d));
        }
    }

    void save(Player player) {
        // Don't remove so that we have a cache for the next login
        EconomyData data = economyData.get(player.getUniqueId());
        if (data != null) {
            data.save();
        }
    }
}
