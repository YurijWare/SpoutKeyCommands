package com.yurijware.bukkit.SpoutKeyCommands;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.getspout.spoutapi.keyboard.Keyboard;

public class SpoutKeyCommandsHandler {
	private final SpoutKeyCommands plugin;
	public enum SkcKeyState { BLOCKED, NOT_SET, PLAYER_SET, SET, SET_NOT_ACTIVE  }
	
	//SpoutKeyCommandsHandler handler = ((SpoutKeyCommands) pm.getPlugin("SpoutKeyCommandsHandler")).getHandle();
	
	protected SpoutKeyCommandsHandler(SpoutKeyCommands plugin) {
		this.plugin = plugin;
	}
	
	public boolean addCommand(String player, Keyboard key, String command, Plugin plugin) {
		String pluginName = plugin.getDescription().getName();
		if (this.plugin.listPlayerConfig.containsKey(player)) {
			Config conf = this.plugin.listPlayerConfig.get(player);
			if (!conf.getPlugin(key).equals("SpoutKeyCommands")) {
				PluginManager pm = this.plugin.getServer().getPluginManager();
				Plugin p = pm.getPlugin(conf.getPlugin(key));
				if (p != null && p.isEnabled()) {
					return false;
				}
			}
			conf.addCommand(key, command, pluginName);
			this.plugin.listPlayerConfig.put(player, conf);
			return true;
		} else {
			Config conf = new Config(player);
			conf.addCommand(key, command, pluginName);
			this.plugin.listPlayerConfig.put(player, conf);
			return true;
		}
	}
	
	public boolean removeCommand(String player, Keyboard key, Plugin plugin) {
		String pluginName = plugin.getDescription().getName();
		if (this.plugin.listPlayerConfig.containsKey(player)) {
			Config conf = this.plugin.listPlayerConfig.get(player);
			PluginManager pm = this.plugin.getServer().getPluginManager();
			Plugin p = pm.getPlugin(pluginName);
			if (pluginName.equals(conf.getPlugin(key))) {
				
			} else if (p != null && p.isEnabled()) {
				return false;
			}
			conf.removeCommand(key);
			this.plugin.listPlayerConfig.put(player, conf);
		}
		return true;
	}
	
	public SkcKeyState checkCommand(String player, Keyboard key, Plugin plugin) {
		String pluginName = plugin.getDescription().getName();
		if (this.plugin.listPlayerConfig.containsKey(player)) {
			Config conf = this.plugin.listPlayerConfig.get(player);
			if (conf.getCommand(key).equals("")) {
				return SkcKeyState.NOT_SET;
			}
			PluginManager pm = this.plugin.getServer().getPluginManager();
			Plugin p = pm.getPlugin(conf.getPlugin(key));
			if (conf.getPlugin(key).equals(pluginName)) {
				return SkcKeyState.SET;
			} else if (conf.getPlugin(key).equals("SpoutKeyCommands")) {
				return SkcKeyState.PLAYER_SET;
			} else if (p != null && p.isEnabled()) {
				return SkcKeyState.BLOCKED;
			} else {
				return SkcKeyState.SET_NOT_ACTIVE;
			}
		}
		return SkcKeyState.NOT_SET;
	}
	
	public boolean addGlobalCommand(Keyboard key, String command, Plugin plugin) {
		String pluginName = plugin.getDescription().getName();
		if (this.plugin.listGlobalCmds.containsKey(key)) {
			String cmdPlugin = this.plugin.listGlobalPlugin.get(key);
			PluginManager pm = this.plugin.getServer().getPluginManager();
			Plugin p = pm.getPlugin(pluginName);
			if (pluginName.equals(cmdPlugin)) {
				
			} else if (p != null && p.isEnabled()) {
				return false;
			}
		}
		this.plugin.listGlobalCmds.put(key, command);
		this.plugin.listGlobalPlugin.put(key, pluginName);
		return true;
	}
	
	public boolean removeGlobalCommand(Keyboard key, Plugin plugin) {
		String pluginName = plugin.getDescription().getName();
		if (this.plugin.listGlobalCmds.containsKey(key)) {
			String cmdPlugin = this.plugin.listGlobalPlugin.get(key);
			PluginManager pm = this.plugin.getServer().getPluginManager();
			Plugin p = pm.getPlugin(pluginName);
			if (pluginName.equals(cmdPlugin)) {
				
			} else if (p != null && p.isEnabled()) {
				return false;
			}
			this.plugin.listGlobalCmds.remove(key);
			this.plugin.listGlobalPlugin.remove(key);
		}
		return true;
	}
	
	public SkcKeyState checkGlobalCommand(Keyboard key, Plugin plugin) {
		String pluginName = plugin.getDescription().getName();
		if (this.plugin.listGlobalCmds.containsKey(key)) {
			String cmdPlugin = this.plugin.listGlobalPlugin.get(key);
			PluginManager pm = this.plugin.getServer().getPluginManager();
			Plugin p = pm.getPlugin(pluginName);
			if (pluginName.equals(cmdPlugin)) {
				return SkcKeyState.SET;
			} if (p != null && p.isEnabled()) {
				return SkcKeyState.BLOCKED;
			} else {
				return SkcKeyState.SET_NOT_ACTIVE;
			}
		}
		return SkcKeyState.NOT_SET;
	}
	
}
