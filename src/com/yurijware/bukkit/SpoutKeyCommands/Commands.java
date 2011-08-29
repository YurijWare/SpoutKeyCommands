package com.yurijware.bukkit.SpoutKeyCommands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.keyboard.Keyboard;
import org.getspout.spoutapi.player.SpoutPlayer;

import com.yurijware.bukkit.SpoutKeyCommands.SpoutKeyCommands.ChooseMode;

public class Commands implements CommandExecutor {
	private final SpoutKeyCommands plugin;
	
	public Commands(SpoutKeyCommands plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		SpoutPlayer player = null;
		if (sender instanceof Player) {
			player = (SpoutPlayer) sender;
		}
		
		if (player != null && !player.isSpoutCraftEnabled()) {
			player.sendMessage(ChatColor.RED + "You need to use the spoutcraft client.");
			return true;
		}
		
		if (args.length > 1 && args[0].equalsIgnoreCase("set")) {
			if (player == null) { return true; }
			if (!this.plugin.checkPermissions(sender, "SpoutKeyCommands.use")) {
				sender.sendMessage(ChatColor.RED + "You need permission.");
				return true;
			}
			String arguments = args[1];
			for (int i = 2; i < args.length; i++) {
				arguments += " " + args[i];
			}
			if (arguments.charAt(0) == '/') {
				arguments = arguments.replace("/", "");
			}
			this.plugin.listPlayerMode.put(player.getName(), ChooseMode.SET);
			this.plugin.listPlayerCmdTemp.put(player.getName(), arguments);
			sender.sendMessage(ChatColor.DARK_AQUA + "Press a key to set the command to that key.");
			return true;
			
		} else if (args.length > 0){
			if(args[0].equalsIgnoreCase("check")) {
				if (player == null) { return true; }
				if (!this.plugin.checkPermissions(sender, "SpoutKeyCommands.use")) {
					sender.sendMessage(ChatColor.RED + "You need permission.");
					return true;
				}
				player.sendMessage(ChatColor.DARK_AQUA + "=== List of key commands ===");
				int count = 0;
				if (this.plugin.listPlayerConfig.containsKey(player.getName())) {
					Config conf = this.plugin.listPlayerConfig.get(player.getName());
					for (Keyboard key: Keyboard.values()) {
						String k = key.toString().replaceAll("KEY_", "");
						String msg = "";
						String cmd = conf.getCommand(key);
						String cmd_global = "";
						if (this.plugin.listGlobalCmds.containsKey(key)) {
							cmd_global = this.plugin.listGlobalCmds.get(key);
						}
						if (cmd.equals("") && cmd_global.equals("")) { continue; }
						if (!cmd_global.equals("")) {
							String plugin = this.plugin.listGlobalPlugin.get(key);
							if (plugin.equals("")) { plugin = "Unkown"; }
							msg = ChatColor.AQUA + k + ": /" + cmd_global + " " +
									ChatColor.GRAY + "by " + plugin;
						} else if (!cmd.equals("")) {
							msg = ChatColor.AQUA + k + ": /" + cmd;
						} else {
							continue;
						}
						sender.sendMessage(msg);
						count++;
					}
				}
				if (count == 0) {
					sender.sendMessage(ChatColor.RED + "You dont have any commands set.");
				}
				return true;
				
			} else if (args[0].equalsIgnoreCase("clear")) {
				if (player == null) { return true; }
				if (!this.plugin.checkPermissions(sender, "SpoutKeyCommands.use")) {
					sender.sendMessage(ChatColor.RED + "You need permission.");
					return true;
				}
				if (this.plugin.listPlayerConfig.containsKey(player.getName())) {
					this.plugin.listPlayerConfig.get(player.getName()).clearCommands();
					sender.sendMessage(ChatColor.DARK_AQUA + "All commands have been removed.");
				}
				return true;
				
			} else if (args[0].equalsIgnoreCase("unset")) {
				if (player == null) { return true; }
				if (!this.plugin.checkPermissions(sender, "SpoutKeyCommands.use")) {
					sender.sendMessage(ChatColor.RED + "You need permission.");
					return true;
				}
				this.plugin.listPlayerMode.put(player.getName(), ChooseMode.UNSET);
				sender.sendMessage(ChatColor.DARK_AQUA + "Press a key to remove that command.");
				return true;
			} else if (args[0].equalsIgnoreCase("save")) {
				if (!this.plugin.checkPermissions(sender, "SpoutKeyCommands.reload")) {
					sender.sendMessage(ChatColor.RED + "You need permission.");
					return true;
				}
				this.plugin.saveToDB();
				sender.sendMessage(ChatColor.DARK_AQUA + "Key commands saved");
				return true;
			} else if (args[0].equalsIgnoreCase("load")) {
				if (!this.plugin.checkPermissions(sender, "SpoutKeyCommands.reload")) {
					sender.sendMessage(ChatColor.RED + "You need permission.");
					return true;
				}
				this.plugin.loadAllPlayerKeys();
				this.plugin.loadAllGlobalKeys();
				sender.sendMessage(ChatColor.DARK_AQUA + "Key commands loaded");
				return true;
			}
			
		}
		return false;
	}
	
}
