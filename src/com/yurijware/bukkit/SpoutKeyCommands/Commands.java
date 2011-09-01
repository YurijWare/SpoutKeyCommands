package com.yurijware.bukkit.SpoutKeyCommands;

import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
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
			if (!this.plugin.checkPermissions(sender, "SpoutKeyCommands.set.personal")) {
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
			
		} else if (args.length > 1 && args[0].equalsIgnoreCase("gset")) {
			if (player == null) { return true; }
			if (!this.plugin.checkPermissions(sender, "SpoutKeyCommands.set.global")) {
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
			this.plugin.listPlayerMode.put(player.getName(), ChooseMode.GSET);
			this.plugin.listPlayerCmdTemp.put(player.getName(), arguments.toLowerCase());
			sender.sendMessage(ChatColor.DARK_AQUA + "Press a key to set the global command to that key.");
			return true;
			
		} else if (args.length > 0){
			if(args[0].equalsIgnoreCase("check")) {
				if (args.length == 1 && !this.plugin.checkPermissions(sender, "SpoutKeyCommands.check.self")) {
					sender.sendMessage("Checking self");
					sender.sendMessage(ChatColor.RED + "You need permission.");
					return true;
				} else if (args.length > 1 && !this.plugin.checkPermissions(sender, "SpoutKeyCommands.check.other")) {
					sender.sendMessage("Checking other: " + args[1]);
					sender.sendMessage(ChatColor.RED + "You need permission.");
					return true;
				}
				
				String name = "";
				String title = "";
				if (args.length > 1) {
					List<Player> list = this.plugin.getServer().matchPlayer(args[1]);
					if (list.size() == 1) {
						name = list.get(0).getName();
					} else if (list.size() > 1) {
						sender.sendMessage(ChatColor.RED + "Too many matches");
						return true;
					} else {
						sender.sendMessage(ChatColor.RED + "Player not found");
						return true;
					}
					title = ChatColor.DARK_AQUA + "=== List of key commands for " + name + " ===";
				} else if (player != null) {
					name = player.getName();
					title = ChatColor.DARK_AQUA + "=== List of key commands ===";
				} else {
					sender.sendMessage("You must specify a player");
					return true;
				}
				
				sender.sendMessage(title);
				
				int count = 0;
				Config conf;
				if (this.plugin.listPlayerConfig.containsKey(name)) {
					conf = this.plugin.listPlayerConfig.get(name);
				} else {
					conf = new Config(name);
					this.plugin.listPlayerConfig.put(name, conf);
				}
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
				if (count == 0 && args.length == 1) {
					sender.sendMessage(ChatColor.RED + "You dont have any commands set.");
				} else if (count == 0) {
					sender.sendMessage(ChatColor.RED + "That player dont have any commands set.");
				}
				return true;
				
			} else if (args[0].equalsIgnoreCase("clear")) {
				if (player == null) { return true; }
				if (!this.plugin.checkPermissions(sender, "SpoutKeyCommands.set.personal")) {
					sender.sendMessage(ChatColor.RED + "You need permission.");
					return true;
				}
				if (this.plugin.listPlayerConfig.containsKey(player.getName())) {
					this.plugin.listPlayerConfig.get(player.getName()).clearCommands();
					sender.sendMessage(ChatColor.DARK_AQUA + "Personal commands have been cleared.");
				}
				return true;
				
			} else if (args[0].equalsIgnoreCase("gclear")) {
				if (!this.plugin.checkPermissions(sender, "SpoutKeyCommands.set.global")) {
					sender.sendMessage(ChatColor.RED + "You need permission.");
					return true;
				}
				
				Iterator<Keyboard> itr = this.plugin.listGlobalCmds.keySet().iterator();
				while(itr.hasNext()) {
					Keyboard key = itr.next();
					String p = this.plugin.listGlobalPlugin.get(key);
					PluginManager pm = this.plugin.getServer().getPluginManager();
					if (!p.equals("SpoutKeyCommands") &&
							pm.getPlugin(p) != null &&
							pm.getPlugin(p).isEnabled()) {
						continue;
					}
					this.plugin.listGlobalCmds.remove(key);
					this.plugin.listGlobalPlugin.remove(key);
				}
				sender.sendMessage(ChatColor.DARK_AQUA + "Global commands have been cleared.");
				return true;
				
			}  else if (args[0].equalsIgnoreCase("unset")) {
				if (player == null) { return true; }
				if (!this.plugin.checkPermissions(sender, "SpoutKeyCommands.set.personal")) {
					sender.sendMessage(ChatColor.RED + "You need permission.");
					return true;
				}
				this.plugin.listPlayerMode.put(player.getName(), ChooseMode.UNSET);
				sender.sendMessage(ChatColor.DARK_AQUA + "Press a key to remove that command.");
				return true;
				
			} else if (args[0].equalsIgnoreCase("gunset")) {
				if (player == null) { return true; }
				if (!this.plugin.checkPermissions(sender, "SpoutKeyCommands.set.global")) {
					sender.sendMessage(ChatColor.RED + "You need permission.");
					return true;
				}
				this.plugin.listPlayerMode.put(player.getName(), ChooseMode.GUNSET);
				sender.sendMessage(ChatColor.DARK_AQUA + "Press a key to remove that global command.");
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
