package bsh.beyond.listeners;

import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.items.ItemBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlayerListener implements Listener {

    private HashMap<UUID, Boolean> monsterPlayers;
    private JavaPlugin plugin;

    public PlayerListener(HashMap<UUID, Boolean> monsterPlayers, JavaPlugin plugin) {
        this.monsterPlayers = monsterPlayers;
        this.plugin = plugin;
    }

    private boolean checkCurrentItem(ItemStack itemInHand, ItemStack item){
        if(itemInHand.equals(item)){
            return true;
        }
        return false;
    }

    public void handleBoomStick(Player player, Action action, ItemStack itemInHand){

        // Check if the player is holding a stick, is in the monsterPlayers group, and if the action is a left-click
        if (itemInHand.getType() == Material.STICK &&
                (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) &&
                monsterPlayers.containsKey(player.getUniqueId())) {

            Vector direction = player.getLocation().getDirection();
            double horizontalMultiplier = 5; // Adjust as needed
            double verticalMultiplier = 1.2;   // Adjust as needed

            direction.multiply(horizontalMultiplier);
            direction.setY(direction.getY() * verticalMultiplier);

            player.setVelocity(direction);
        }
    }

    public void handleForcePull(Player player, Action action, ItemStack itemInHand){
        // Check if the player is holding a stick and if the action is a left-click
        if (itemInHand.getType() == Material.STICK &&
                (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) {

            // Find the targeted player
            List<Entity> nearbyEntities = player.getNearbyEntities(3, 3, 3); // Adjust range as needed
            Player targetedPlayer = null;
            for (Entity entity : nearbyEntities) {
                if (entity instanceof Player && player.hasLineOfSight(entity)) {
                    targetedPlayer = (Player) entity;
                    break;
                }
            }

            // Apply force to pull the targeted player
            if (targetedPlayer != null) {
                Location playerLocation = player.getLocation();
                Location targetLocation = targetedPlayer.getLocation();

                Vector pullDirection = playerLocation.toVector().subtract(targetLocation.toVector()).normalize();
                double horizontalMultiplier = 2; // Adjust as needed
                double verticalMultiplier = 1;   // Adjust as needed

                pullDirection.multiply(horizontalMultiplier);
                pullDirection.setY(pullDirection.getY() * verticalMultiplier);

                targetedPlayer.setVelocity(pullDirection);
            }
        }
    }

    public void handlePlayerFloat(Player player, Action action, ItemStack itemInHand) {
        // Check if the player right-clicks with an item in hand (can specify the item type if needed)

        if ((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {

            // Find nearby living entities within a 3x3x3 radius
            List<Entity> nearbyEntities = player.getNearbyEntities(3, 3, 3);
            for (Entity entity : nearbyEntities) {
                if (entity instanceof LivingEntity) {
                    LivingEntity livingEntity = (LivingEntity) entity;
                    // Apply Levitation effect for 5 seconds (100 ticks)
                    livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 100, 1));
                }
            }
        }
    }


    public void handleBloodBend(Player player, PlayerInteractEvent event) {
        int RADIUS = 5;
        if (player.isSneaking()) {
            // Find the living entity the player is looking at within 20 blocks
            LivingEntity targetedEntity = null;
            List<Entity> nearbyEntities = player.getNearbyEntities(RADIUS, RADIUS, RADIUS);

            for (Entity entity : nearbyEntities) {
                if (entity instanceof LivingEntity && player.hasLineOfSight(entity)) {
                    targetedEntity = (LivingEntity) entity;
                    break;
                }
            }

            final LivingEntity finalTargetedEntity = targetedEntity;

            // If a targeted entity is found
            if (finalTargetedEntity != null) {
                // Continuously move the entity based on the player's cursor
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.isSneaking() && player.hasLineOfSight(finalTargetedEntity)) {
                            Location lookLocation = player.getTargetBlock(null, 10).getLocation();
                            finalTargetedEntity.teleport(new Location(lookLocation.getWorld(),
                                    lookLocation.getX(),
                                    lookLocation.getY(),
                                    lookLocation.getZ(),
                                    finalTargetedEntity.getLocation().getYaw(),
                                    finalTargetedEntity.getLocation().getPitch()));

                            // Launch the entity on left-click
                            if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                                Vector launchDirection = player.getLocation().getDirection();
                                double launchPower = 2.0; // Adjust launch power as needed
                                finalTargetedEntity.setVelocity(launchDirection.multiply(launchPower));
                                this.cancel(); // Stop the runnable
                            }
                        } else {
                            this.cancel(); // Stop the runnable if the player is no longer sneaking or has lost line of sight
                        }
                    }
                }.runTaskTimer(plugin, 0L, 8L); // Schedule to run immediately and repeat every tick. 'plugin' should be your plugin instance
            }
        }
    }




    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

//        ItemStack boomStick = OraxenItems.getItemById("boom_stick").build();
//        ItemStack pullStick = OraxenItems.getItemById("pull_stick").build();
//        ItemStack floatStick = OraxenItems.getItemById("float_stick").build();
//        ItemStack sickStick = OraxenItems.getItemById("sick_stick").build();


        handleBloodBend(player, event);
        handleBoomStick(player, action, itemInHand);
        handleForcePull(player, action, itemInHand);
        // handlePlayerFloat(player, action, itemInHand);
    }
}
