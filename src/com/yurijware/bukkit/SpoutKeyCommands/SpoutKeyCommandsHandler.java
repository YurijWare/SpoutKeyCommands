package com.yurijware.bukkit.SpoutKeyCommands;

import java.util.Iterator;
import java.util.LinkedHashSet;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.getspout.spoutapi.keyboard.Keyboard;

public class SpoutKeyCommandsHandler {
	public enum SkcKeyState { BLOCKED, NOT_SET, SET, SET_PLUGIN_DISABLED, INVALID_KEY  }
	
	private static PluginManager pm = Bukkit.getServer().getPluginManager();
	
	//SpoutKeyCommandsHandler handler = ((SpoutKeyCommands) pm.getPlugin("SpoutKeyCommandsHandler")).getHandle();
	
	private static boolean validKeys(LinkedHashSet<Keyboard> keys) {
		Iterator<Keyboard> itr = keys.iterator();
		while (itr.hasNext()) {
			switch(itr.next()) {
			case KEY_ESCAPE:
			case KEY_1:
			case KEY_2:
			case KEY_3:
			case KEY_4:
			case KEY_5:
			case KEY_6:
			case KEY_7:
			case KEY_8:
			case KEY_9:
			case KEY_LMENU:
			case KEY_LWIN:
			case KEY_RWIN:
				return false;
			}
		}
		return true;
	}
	
	public boolean addGlobalCommand(LinkedHashSet<Keyboard> keys, String command, Plugin plugin) {
		if (!validKeys(keys)) { return false; }
		String pluginName = plugin.getDescription().getName();
		GlobalCmd gc = GlobalCmd.get(keys);
		
		if (gc != null) {
			if (!pluginName.equals(gc.getPlugin())) {
				Plugin p = pm.getPlugin(gc.getPlugin());
				if (p != null && p.isEnabled()) {
					return false;
				}
			}
			
			gc.setCommand(command);
			gc.setPlugin(pluginName);
			SpoutKeyCommands.getInstance().getDatabase().update(gc);
		}
		
		gc = new GlobalCmd(keys, command, plugin);
		SpoutKeyCommands.getInstance().getDatabase().update(gc);
		return true;
	}
	
	public boolean removeGlobalCommand(LinkedHashSet<Keyboard> keys, Plugin plugin) {
		String pluginName = plugin.getDescription().getName();
		GlobalCmd gc = GlobalCmd.get(keys);
		
		if (gc != null) {
			if (!pluginName.equals(gc.getPlugin())) {
				Plugin p = pm.getPlugin(gc.getPlugin());
				if (p != null && p.isEnabled()) {
					return false;
				}
			}
			
			SpoutKeyCommands.getInstance().getDatabase().delete(gc);
		}
		
		return true;
	}
	
	public SkcKeyState checkGlobalCommand(LinkedHashSet<Keyboard> keys, Plugin plugin) {
		if (!validKeys(keys)) { return SkcKeyState.INVALID_KEY; }
		String pluginName = plugin.getDescription().getName();
		GlobalCmd gc = GlobalCmd.get(keys);
		
		if (gc != null) {
			if (!pluginName.equals(gc.getPlugin())) {
				Plugin p = pm.getPlugin(gc.getPlugin());
				if (p != null && p.isEnabled()) {
					return SkcKeyState.BLOCKED;
				}
				return SkcKeyState.SET_PLUGIN_DISABLED;
			}
			
			return SkcKeyState.SET;
		}
		
		return SkcKeyState.NOT_SET;
	}
	
}
