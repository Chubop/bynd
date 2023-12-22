package bsh.beyond.util;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMovementUtil {

    // This method checks if a player has moved from one block to another
    public static boolean isMoving(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
    
        // Check if the 'to' and 'from' locations are not null
        if (to == null || from == null) {
            return false;
        }
    
        // Check if the X, Y, or Z coordinates have changed
        if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ()) {
            return true;
        }
    
        // Check if the pitch or yaw (head movement) has changed significantly
        // Lower the threshold to 1 degree
        if (Math.abs(from.getPitch() - to.getPitch()) > 1 || Math.abs(from.getYaw() - to.getYaw()) > 1) {
            return true;
        }
    
        return false;
    }
}
