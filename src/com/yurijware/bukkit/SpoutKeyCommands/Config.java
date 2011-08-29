package com.yurijware.bukkit.SpoutKeyCommands;

import java.util.LinkedHashMap;

import org.getspout.spoutapi.keyboard.Keyboard;

public class Config {
	
	private String player;
	private LinkedHashMap<Keyboard, String> commandList = new LinkedHashMap<Keyboard, String>();
	private LinkedHashMap<Keyboard, String> pluginList = new LinkedHashMap<Keyboard, String>();
	
	public Config(String player) {
		this.player = player;
	}
	
	public String getPlayer() {
		return player;
	}
	
	public void addCommand(Keyboard key, String command, String plugin) {
		commandList.put(key, command);
		pluginList.put(key, plugin);
	}
	
	public void removeCommand(Keyboard key) {
		commandList.remove(key);
		pluginList.remove(key);
	}
	
	public LinkedHashMap<Keyboard, String> getCommands() {
		return commandList;
	}
	
	public LinkedHashMap<Keyboard, String> getPlugins() {
		return pluginList;
	}
	
	public String getCommand(Keyboard key) {
		if (commandList.containsKey(key)){
			return commandList.get(key);
		}
		return "";
	}
	
	public String getPlugin(Keyboard key) {
		if (pluginList.containsKey(key)){
			return pluginList.get(key);
		}
		return "";
	}
	
	public void clearCommands() {
		commandList.clear();
		pluginList.clear();
	}
	
}
