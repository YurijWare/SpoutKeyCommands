package com.yurijware.bukkit.SpoutKeyCommands;

import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.player.SpoutPlayer;

import com.yurijware.bukkit.SpoutKeyCommands.SpoutKeyCommands.ChooseMode;

public class Commands implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		SpoutPlayer player = null;
		if (sender instanceof Player) {
			player = (SpoutPlayer) sender;
		}
		
		if (player != null && !player.isSpoutCraftEnabled()) {
			player.sendMessage(ChatColor.RED + "You need to use the spoutcraft client");
			return true;
		}
		
		if (args.length == 0) {
			return false;
			
		} else if (args.length >= 2 && args[0].equalsIgnoreCase("set")) {
			if (player == null) { return true; }
			setPersonalCmd(player, args);
			return true;
			
		} else if (args.length >= 2 && args[0].equalsIgnoreCase("gset")) {
			if (player == null) { return true; }
			setGlobalCmd(player, args);
			return true;
			
		} else if(args.length >= 1 && args.length <= 3 && args[0].equalsIgnoreCase("check")) {
			if (args.length > 1 && (args[1].equalsIgnoreCase("personal") ||
					args[1].equalsIgnoreCase("global"))) {
				checkCmd(sender, args);
				
			} else if (player != null) {
				sender.sendMessage(ChatColor.RED + "You must choose personal or global");
				
			} else {
				checkCmd(sender, new String[] {"check", "global"});
			}
			return true;
			
		} else if (args[0].equalsIgnoreCase("config")) {
			config(sender, args, label);
			return true;
			
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("clear")) {
				if (player == null) { return true; }
				if (!Permission.check(sender, "SpoutKeyCommands.use")) {
					sender.sendMessage(ChatColor.RED + "You need permission");
					return true;
				}
				
				List<PlayerCmd> list = SpoutKeyCommands.getInstance().getDatabase().find(PlayerCmd.class)
						.where().ieq("player", player.getName()).findList();
				SpoutKeyCommands.getInstance().getDatabase().delete(list);
				PlayerCmd.clear(player);
				
				sender.sendMessage(ChatColor.DARK_AQUA + "Commands have been cleared");
				return true;
				
			} else if (args[0].equalsIgnoreCase("gclear")) {
				if (!Permission.check(sender, "SpoutKeyCommands.set-global")) {
					sender.sendMessage(ChatColor.RED + "You need permission.");
					return true;
				}
				
				List<GlobalCmd> list = SpoutKeyCommands.getInstance().getDatabase()
						.find(GlobalCmd.class).findList();
				SpoutKeyCommands.getInstance().getDatabase().delete(list);
				GlobalCmd.clear();
				
				sender.sendMessage(ChatColor.DARK_AQUA + "Global commands have been cleared");
				return true;
				
			}  else if (args[0].equalsIgnoreCase("unset")) {
				if (player == null) { return true; }
				if (!Permission.check(sender, "SpoutKeyCommands.use")) {
					sender.sendMessage(ChatColor.RED + "You need permission");
					return true;
				}
				
				PlayerOptions.setMode(player, ChooseMode.UNSET);
				sender.sendMessage(ChatColor.DARK_AQUA + "Press a key to remove that command");
				return true;
				
			} else if (args[0].equalsIgnoreCase("gunset")) {
				if (player == null) { return true; }
				if (!Permission.check(sender, "SpoutKeyCommands.set-global")) {
					sender.sendMessage(ChatColor.RED + "You need permission");
					return true;
				}
				
				PlayerOptions.setMode(player, ChooseMode.GUNSET);
				sender.sendMessage(ChatColor.DARK_AQUA + "Press a key to remove that global command");
				return true;
				
			}
			
		}
		return false;
	}
	
	private void setPersonalCmd(Player player, String[] args) {
		if (!Permission.check(player, "SpoutKeyCommands.use")) {
			player.sendMessage(ChatColor.RED + "You need permission");
			return;
		}
		
		String arguments = args[1];
		for (int i = 2; i < args.length; i++) {
			arguments += " " + args[i];
		}
		if (arguments.charAt(0) == '/') {
			arguments = arguments.replace("/", "");
		}
		
		PlayerOptions.setMode(player, ChooseMode.SET);
		PlayerOptions.setCmd(player, arguments.toLowerCase());
		
		player.sendMessage(ChatColor.DARK_AQUA + "Press a key to set the command to that key");
	}
	
	private void setGlobalCmd(Player player, String[] args) {
		if (!Permission.check(player, "SpoutKeyCommands.use")) {
			player.sendMessage(ChatColor.RED + "You need permission");
			return;
		}
		
		String arguments = args[1];
		for (int i = 2; i < args.length; i++) {
			arguments += " " + args[i];
		}
		if (arguments.charAt(0) == '/') {
			arguments = arguments.replace("/", "");
		}
		
		PlayerOptions.setMode(player, ChooseMode.GSET);
		PlayerOptions.setCmd(player, arguments.toLowerCase());
		
		player.sendMessage(ChatColor.DARK_AQUA + "Press a key to set the global command to that key");
	}
	
	private void checkCmd(CommandSender sender, String[] args) {
		if (args.length == 2 && !Permission.check(sender, "SpoutKeyCommands.use")) {
			sender.sendMessage("Checking self");
			sender.sendMessage(ChatColor.RED + "You need permission");
			return;
		} else if (args.length == 3 && !Permission.check(sender, "SpoutKeyCommands.check-other")) {
			sender.sendMessage("Checking other: " + args[1]);
			sender.sendMessage(ChatColor.RED + "You need permission");
			return;
		}
		
		Player player = null;
		if (sender instanceof Player) {
			player = (SpoutPlayer) sender;
		}
		
		String name = "";
		String title = "";
		
		if (args.length == 3 && args[1].equalsIgnoreCase("personal")) {
			List<Player> list = Bukkit.getServer().matchPlayer(args[2]);
			
			if (list.size() == 1) {
				name = list.get(0).getName();
			} else if (list.size() > 1) {
				sender.sendMessage(ChatColor.RED + "Too many matches");
				return;
			} else {
				sender.sendMessage(ChatColor.RED + "Player not found");
				return;
			}
			title = ChatColor.GREEN + "===" + ChatColor.DARK_AQUA + " Personal keys for " +
					ChatColor.DARK_AQUA + name + ChatColor.GREEN + " ===";
			
		} else if (player != null && args[1].equalsIgnoreCase("personal")) {
			name = player.getName();
			title = ChatColor.GREEN + "===" + ChatColor.DARK_AQUA +
					" Personal keys " + ChatColor.GREEN + "===";
			
		} else if (args[1].equalsIgnoreCase("global")) {
			title = ChatColor.GREEN + "===" + ChatColor.DARK_AQUA +
					" Global keys " + ChatColor.GREEN + "===";
			
		} else {
			sender.sendMessage("You must specify a player");
			return;
		}
		
		sender.sendMessage(title);
		
		int count = 0;
		if (args[1].equalsIgnoreCase("personal")) {
			Iterator<PlayerCmd> itr = PlayerCmd.getList(name).iterator();
			while (itr.hasNext()) {
				PlayerCmd pc = itr.next();
				String k = pc.getCombination();
				String c = pc.getCommand();
				sender.sendMessage(ChatColor.AQUA + k + ": /" + c);
				count++;
			}
			
		} else if (args[1].equalsIgnoreCase("global")) {
			Iterator<GlobalCmd> itr = GlobalCmd.getList().iterator();
			while (itr.hasNext()) {
				GlobalCmd gc = itr.next();
				String k = gc.getCombination();
				String c = gc.getCommand();
				sender.sendMessage(ChatColor.AQUA + k + ": /" + c);
				count++;
			}
		}
		
		if (args[1].equalsIgnoreCase("global") && count == 0) {
			sender.sendMessage(ChatColor.RED + "There aren't any global keys set");
			
		} else if (args[1].equalsIgnoreCase("personal") && count == 0) {
			sender.sendMessage(ChatColor.RED + "You don't have any keys set");
			
		} else if (count == 0) {
			sender.sendMessage(ChatColor.RED + name +" don't have any keys set");
		}
	}
	
	private void config(CommandSender sender, String[] args, String label) {
		if (!Permission.check(sender, "SpoutKeyCommands.use")) {
			sender.sendMessage(ChatColor.RED + "You need permission");
			return;
		}
		
		SpoutPlayer player = null;
		if (sender instanceof Player) {
			player = (SpoutPlayer) sender;
		}
		
		if (args.length >= 2 && player != null && args[1].equalsIgnoreCase("preferred")) {
			PlayerOptions pk = PlayerOptions.getPlayer(player);
			if (args.length == 2) {
				sender.sendMessage(ChatColor.DARK_AQUA + "Preferred commands: " +
						ChatColor.GREEN + pk.getPreferred());
				return;
				
			} else if (args.length == 3 && (args[2].equalsIgnoreCase("personal") ||
					args[2].equalsIgnoreCase("global"))) {
				pk.setPreferred(args[2].toLowerCase());
				SpoutKeyCommands.getInstance().getDatabase().update(pk);
				sender.sendMessage(ChatColor.DARK_AQUA + "Preferred commands set to: " +
						ChatColor.GREEN + args[2].toLowerCase());
				return;
			} else {
				sender.sendMessage("Can only be global or personal");
				return;
			}
			
		} else if (args.length >= 3 && args[1].equalsIgnoreCase("default") &&
				args[2].equalsIgnoreCase("preferred") &&
				Permission.check(sender, "SpoutKeyCommands.admin")) {
			if (args.length == 3) {
				sender.sendMessage(ChatColor.DARK_AQUA + "Default preferred command: " +
						ChatColor.GREEN + GlobalOptions.getConf("default preferred"));
				return;
				
			} else if (args.length == 4 && (args[3].equalsIgnoreCase("personal") ||
					args[3].equalsIgnoreCase("global"))) {
				GlobalOptions.setConf("default preferred", args[3].toLowerCase());
				sender.sendMessage(ChatColor.DARK_AQUA + "Default preferred commands set to: " +
						ChatColor.GREEN + args[3].toLowerCase());
				return;
			} else {
				sender.sendMessage("Can only be global or personal");
				return;
			}
		}
		
		sender.sendMessage(ChatColor.GREEN + "===" + ChatColor.DARK_AQUA +
				" Available config options " + ChatColor.GREEN + "===");
		if (Permission.check(sender, "SpoutKeyCommands.admin")) {
			sender.sendMessage("/" + label + " config default preferred [personal|global]");
		}
		if (player != null) {
			sender.sendMessage("/" + label + " config preferred [personal|global]");
		}
	}
	
}
