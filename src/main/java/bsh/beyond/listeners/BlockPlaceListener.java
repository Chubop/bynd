package bsh.beyond.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.entity.Player;

public class BlockPlaceListener implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){

        // Get the block being placed
        Block placedBlock = event.getBlockPlaced();

        // Start checking from the block below the placed block
        Block blockToCheck = placedBlock.getRelative(0, -1, 0);

        // Loop to find the first non-air block below the placed block
        while (blockToCheck.getType() == Material.AIR && blockToCheck.getY() > 0) {
            blockToCheck = blockToCheck.getRelative(0, -1, 0);
        }

        // Check if the first non-air block is water
        if (blockToCheck.getType() == Material.WATER) {
            // Cancel the block placement
            event.setCancelled(true);

            // Get the player who placed the block
            Player player = event.getPlayer();

            // Send a light red message to the player
            player.sendMessage(ChatColor.RED + " Placing blocks above water is disabled for this event.");
        }
    }
}