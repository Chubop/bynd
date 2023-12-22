package bsh.beyond.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class BeyondCommandExecutor implements CommandExecutor {

    private final HashMap<UUID, Boolean> monsterPlayers;
    private final HashMap<UUID, Boolean> verbosePlayers;

    public BeyondCommandExecutor(HashMap<UUID, Boolean> monsterPlayers, HashMap<UUID, Boolean> verbosePlayers) {
        this.monsterPlayers = monsterPlayers;
        this.verbosePlayers = verbosePlayers;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Check if the command is 'beyond' and has the right number of arguments
        if (cmd.getName().equalsIgnoreCase("beyond") && args.length == 2 && args[0].equalsIgnoreCase("mode")) {
            Player targetPlayer = Bukkit.getPlayer(args[1]);
            if (targetPlayer == null) {
                sender.sendMessage(ChatColor.WHITE + "[" + ChatColor.LIGHT_PURPLE + "BEYOND" + ChatColor.WHITE + "] " + ChatColor.RED + "Player not found.");
                return true;
            }

            // Toggle the 'monster' status of the player
            UUID targetUUID = targetPlayer.getUniqueId();
            boolean isMonster = monsterPlayers.getOrDefault(targetUUID, false);
            if (isMonster) {
                monsterPlayers.remove(targetUUID);
            } else {
                monsterPlayers.put(targetUUID, true);
            }

            sender.sendMessage(ChatColor.WHITE + "[" + ChatColor.LIGHT_PURPLE + "BEYOND" + ChatColor.WHITE + "] " + ChatColor.GREEN + "Monster vision " + (!isMonster ? "enabled" : "disabled") + " for " + targetPlayer.getName());
            return true;
        }

        // New section to handle verbose command
        if (args.length == 2 && args[0].equalsIgnoreCase("verbose")) {
            Player targetPlayer = Bukkit.getPlayer(args[1]);
            if (targetPlayer == null) {
                sender.sendMessage(ChatColor.WHITE + "[" + ChatColor.LIGHT_PURPLE + "BEYOND" + ChatColor.WHITE + "] " + ChatColor.RED + "Player not found.");
                return true;
            }

            UUID targetUUID = targetPlayer.getUniqueId();
            boolean isVerbose = verbosePlayers.getOrDefault(targetUUID, false);
            verbosePlayers.put(targetUUID, !isVerbose);

            sender.sendMessage(ChatColor.WHITE + "[" + ChatColor.LIGHT_PURPLE + "BEYOND" + ChatColor.WHITE + "] " + ChatColor.GREEN + "Verbose mode " + (!isVerbose ? "enabled" : "disabled") + " for " + targetPlayer.getName());
            return true;
        }
        return false;
    }
}
