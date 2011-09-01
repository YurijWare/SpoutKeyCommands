package com.yurijware.bukkit.SpoutKeyCommands;

import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.getspout.spoutapi.event.input.InputListener;
import org.getspout.spoutapi.event.input.KeyPressedEvent;
import org.getspout.spoutapi.gui.ScreenType;
import org.getspout.spoutapi.player.SpoutPlayer;

import com.yurijware.bukkit.SpoutKeyCommands.SpoutKeyCommands.ChooseMode;

public class SpoutListener extends InputListener {
	private final SpoutKeyCommands plugin;
	
	public SpoutListener(SpoutKeyCommands plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void onKeyPressedEvent(KeyPressedEvent event) {
		if (event.getScreenType() != ScreenType.GAME_SCREEN) { return; }
		SpoutPlayer player = event.getPlayer();
		
		boolean valid = this.plugin.isValidKey(player, event.getKey());
		ChooseMode mode = null;
		String msg = "";
		if (this.plugin.listPlayerMode.containsKey(player.getName())) {
			mode = this.plugin.listPlayerMode.get(player.getName());
			this.plugin.listPlayerMode.remove(player.getName());
		}
		
		if (mode == null) {
			if (!valid) { return; }
			String cmd = this.plugin.getKeyCommand(player.getName(), event.getKey());
			String cmd_global = this.plugin.getGlobalKeyCommand(event.getKey());
			if (!cmd_global.equals("") && this.plugin.checkPermissions(player, "SpoutKeyCommands.use.global")) {
				player.performCommand(cmd_global);
			} else if (!cmd.equals("") && this.plugin.checkPermissions(player, "SpoutKeyCommands.use.personal")){
				player.performCommand(cmd);
			}
			return;
		} else if (mode == ChooseMode.SET && !valid) {
			player.sendMessage(ChatColor.RED + "That key is not valid.");
			return;
		}
		
		String thisPluginName = this.plugin.getDescription().getName();
		String cmd = "";
		if (this.plugin.listPlayerCmdTemp.containsKey(player.getName())) {
			cmd = this.plugin.listPlayerCmdTemp.get(player.getName());
		}
		
		switch(mode) {
		case SET:
			msg = ChatColor.DARK_AQUA + "Command set to key: " +
					event.getKey().toString().replaceAll("KEY_", "");
			if (cmd.equals("")) { return; }
			
			if (this.plugin.listGlobalPlugin.containsKey(event.getKey())) {
				String p = this.plugin.listGlobalPlugin.get(event.getKey());
				PluginManager pm = this.plugin.getServer().getPluginManager();
				if (pm.getPlugin(p) != null && pm.getPlugin(p).isEnabled()) {
					player.sendMessage(ChatColor.RED + "That key is already set by " + p);
					return;
				}
			}
			
			if (this.plugin.listPlayerConfig.containsKey(player.getName())) {
				String plugin = this.plugin.listPlayerConfig.get(player.getName()).getPlugin(event.getKey());
				if (!thisPluginName.equals(plugin)) {
					PluginManager pm = this.plugin.getServer().getPluginManager();
					if (pm.getPlugin(plugin) != null && pm.getPlugin(plugin).isEnabled()) {
						player.sendMessage(ChatColor.RED + "Another plugin has set a command to this key.");
						return;
					}
				}
				this.plugin.listPlayerConfig.get(player.getName())
					.addCommand(event.getKey(), cmd, thisPluginName);
			} else {
				Config c = new Config(player.getName());
				c.addCommand(event.getKey(), cmd, thisPluginName);
				this.plugin.listPlayerConfig.put(player.getName(), c);
			}
			break;
			
		case GSET:
			msg = ChatColor.DARK_AQUA + "Global command set to key: " +
					event.getKey().toString().replaceAll("KEY_", "");
			if (cmd.equals("")) { return; }
			
			if (this.plugin.listGlobalPlugin.containsKey(event.getKey())) {
				String p = this.plugin.listGlobalPlugin.get(event.getKey());
				PluginManager pm = this.plugin.getServer().getPluginManager();
				if (thisPluginName.equals(p)) {
				} else if (pm.getPlugin(p) != null && pm.getPlugin(p).isEnabled()) {
					player.sendMessage(ChatColor.RED + "That key is already set by " + p);
					return;
				}
			}
			
			this.plugin.listGlobalCmds.put(event.getKey(), cmd);
			this.plugin.listGlobalPlugin.put(event.getKey(), thisPluginName);
			break;
			
		case UNSET:
			if (this.plugin.listPlayerConfig.containsKey(player.getName())) {
				msg = ChatColor.DARK_AQUA + "Command unset from key: " +
						event.getKey().toString().replaceAll("KEY_", "");
				
				Config conf = this.plugin.listPlayerConfig.get(player.getName());
				PluginManager pm = this.plugin.getServer().getPluginManager();
				String p = conf.getPlugin(event.getKey());
				if (p.equals("SpoutKeyCommands")) {
					
				} else if (pm.getPlugin(p) != null && pm.getPlugin(p).isEnabled()) {
					player.sendMessage(ChatColor.RED + "Key set by " + p);
					return;
				}
				this.plugin.listPlayerConfig.get(player.getName()).removeCommand(event.getKey());
			}
			msg = ChatColor.RED + "No command on this key";
			break;
		case GUNSET:
			if (this.plugin.listGlobalPlugin.containsKey(event.getKey())) {
				msg = ChatColor.DARK_AQUA + "Global command unset from key: " +
						event.getKey().toString().replaceAll("KEY_", "");
				
				String p = this.plugin.listGlobalPlugin.get(event.getKey());
				PluginManager pm = this.plugin.getServer().getPluginManager();
				if (thisPluginName.equals(p)) {
				} else if (pm.getPlugin(p) != null && pm.getPlugin(p).isEnabled()) {
					player.sendMessage(ChatColor.RED + "That key set by " + p);
					return;
				}
				this.plugin.listGlobalCmds.remove(event.getKey());
				this.plugin.listGlobalPlugin.remove(event.getKey());
			}
			msg = ChatColor.RED + "No global command on this key";
			
			break;
		}
		
		player.sendMessage(msg);
	}
	
}
