package bsh.beyond.listeners;
import bsh.beyond.util.PlayerMovementUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PlayerMovementListener implements Listener {
    private final HashMap<UUID, Boolean> monsterPlayers;
    private final HashMap<UUID, Boolean> movingPlayers;
    private final HashMap<UUID, Boolean> verbosePlayers;
    private final HashMap<UUID, Long> playerEntryTimes;
    private final HashMap<UUID, Location> lastPlayerLocations = new HashMap<>();

    private static final Set<Material> LIGHT_SOURCES = new HashSet<>(
            Arrays.asList(
                    Material.TORCH,
                    Material.OCHRE_FROGLIGHT,
                    Material.PEARLESCENT_FROGLIGHT,
                    Material.VERDANT_FROGLIGHT,
                    Material.LANTERN,
                    Material.JACK_O_LANTERN,
                    Material.CAMPFIRE,
                    Material.LAVA_BUCKET,
                    Material.GLOWSTONE,
                    Material.GLOW_LICHEN,
                    Material.ENDER_CHEST,
                    Material.GLOW_BERRIES,
                    Material.BLAZE_POWDER,
                    Material.BLAZE_ROD,
                    Material.END_ROD,
                    Material.FIRE_CHARGE,
                    Material.NETHER_STAR,
                    Material.SEA_LANTERN
            )
    );

    public PlayerMovementListener(HashMap<UUID, Boolean> monsterPlayers, HashMap<UUID, Boolean> movingPlayers, HashMap<UUID, Boolean> verbosePlayers, JavaPlugin plugin) {
        this.monsterPlayers = monsterPlayers;
        this.movingPlayers = movingPlayers;
        this.verbosePlayers = verbosePlayers;
        this.playerEntryTimes = new HashMap<>();

        // Schedule the task to run every 10 ticks (0.5 seconds)
        new BukkitRunnable() {
            @Override
            public void run() {
                checkPlayerMovement();
            }
        }.runTaskTimer(plugin, 0, 10);
    }

    private void checkPlayerMovement() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();
            Location currentLocation = player.getLocation();
            Location lastLocation = lastPlayerLocations.getOrDefault(playerId, null);

            boolean isMoving;
            if (lastLocation != null && !lastLocation.equals(currentLocation)) {
                isMoving = true; // Player has moved
            } else {
                isMoving = false; // Player has not moved
            }

            // Update movingPlayers only if there's a change in movement status
            if (movingPlayers.getOrDefault(playerId, false) != isMoving) {
                movingPlayers.put(playerId, isMoving);
                handlePlayerVisibility(player, isMoving);
            }

            lastPlayerLocations.put(playerId, currentLocation.clone());
        }
    }

    private void handlePlayerVisibility(Player player, boolean isMoving) {
        World playerWorld = player.getWorld();

        for (Player monsterPlayer : Bukkit.getOnlinePlayers()) {
            // Check if both players are in the same world
            if (!monsterPlayer.getWorld().equals(playerWorld)) {
                continue;
            }

            double distanceSquared = monsterPlayer.getLocation().distanceSquared(player.getLocation());

            if (distanceSquared > 2500) continue;
            if (!monsterPlayers.containsKey(monsterPlayer.getUniqueId())) {
                monsterPlayer.showPlayer(player);
                continue;
            }
            if (isHoldingLightSource(player)) {
                monsterPlayer.showPlayer(player);
                continue;
            }

            sendVerboseMessage(monsterPlayer, player, isMoving);

            if (distanceSquared <= 121) {
                processPlayerWithinRadius(monsterPlayer, player);
            } else {
                playerEntryTimes.remove(player.getUniqueId());
                updateVisibilityOutsideRadius(monsterPlayer, player, isMoving);
            }
        }
    }

    private boolean isHoldingLightSource(Player player) {
        return LIGHT_SOURCES.contains(player.getInventory().getItemInMainHand().getType()) ||
                LIGHT_SOURCES.contains(player.getInventory().getItemInOffHand().getType());
    }

    private void sendVerboseMessage(Player monsterPlayer, Player player, boolean isMoving) {
        if (verbosePlayers.getOrDefault(monsterPlayer.getUniqueId(), false)) {
            String visibilityMessage = isMoving ?
                    "* " + player.getName() + " is now visible." :
                    player.getName() + " is no longer visible.";
            monsterPlayer.sendMessage(visibilityMessage);
        }
    }

    private void applyGlowingEffect(Player player) {
        // Apply glowing effect for 20 seconds
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20 * 20, 0));
    }

    private void revealPlayerToMonster(Player monsterPlayer, Player player) {
        monsterPlayer.showPlayer(player);
        applyGlowingEffect(player);

        if (!monsterPlayer.getUniqueId().equals(player.getUniqueId())) {
            playMonsterSound(monsterPlayer);
        }
    }

    private void playMonsterSound(Player monsterPlayer) {
        monsterPlayer.playSound(monsterPlayer.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 10.0F, 1.0F);
    }

    private void processPlayerWithinRadius(Player monsterPlayer, Player player) {
        long currentTime = System.currentTimeMillis();
        Long entryTime = playerEntryTimes.get(player.getUniqueId());

        if (entryTime == null) {
            playerEntryTimes.put(player.getUniqueId(), currentTime);
        } else if (currentTime - entryTime >= 3000) { // 3 seconds
            revealPlayerToMonster(monsterPlayer, player);
            playerEntryTimes.remove(player.getUniqueId()); // Reset the timer
        }
    }

    private void updateVisibilityOutsideRadius(Player monsterPlayer, Player player, boolean isMoving) {
        if (monsterPlayer.getUniqueId().equals(player.getUniqueId())) {
            return; // Do not hide or show the monster player to themselves.
        }

        if (isMoving) {
            monsterPlayer.showPlayer(player);
        } else {
            monsterPlayer.hidePlayer(player);
        }
    }

}
