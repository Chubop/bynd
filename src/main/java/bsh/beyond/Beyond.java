package bsh.beyond;

import bsh.beyond.commands.BeyondCommandExecutor;
import bsh.beyond.listeners.BlockPlaceListener;
import bsh.beyond.listeners.PlayerListener;
import bsh.beyond.listeners.PlayerMovementListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public final class Beyond extends JavaPlugin {

    // HashMaps to track monster players and moving players
    private HashMap<UUID, Boolean> monsterPlayers;
    private HashMap<UUID, Boolean> movingPlayers;
    private HashMap<UUID, Boolean> verbosePlayers;

    @Override
    public void onEnable() {

        // Initialize the HashMaps
        monsterPlayers = new HashMap<>();
        movingPlayers = new HashMap<>();
        verbosePlayers = new HashMap<>();

        this.getCommand("beyond").setExecutor(new BeyondCommandExecutor(monsterPlayers, verbosePlayers));

        // Register the PlayerMovementListener
        getServer().getPluginManager().registerEvents(new PlayerMovementListener(monsterPlayers, movingPlayers, verbosePlayers, this), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(monsterPlayers, this), this);


        // Plugin startup logic
        getLogger().info("BEYOND is enabled.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        // Clear HashMaps
        monsterPlayers.clear();
        movingPlayers.clear();

        getLogger().info("BEYOND is disabled.");
    }
}
