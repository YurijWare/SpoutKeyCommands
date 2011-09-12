package com.yurijware.bukkit.SpoutKeyCommands;

import java.util.LinkedHashSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.getspout.spoutapi.event.input.InputListener;
import org.getspout.spoutapi.event.input.KeyPressedEvent;
import org.getspout.spoutapi.event.input.KeyReleasedEvent;
import org.getspout.spoutapi.gui.ScreenType;
import org.getspout.spoutapi.keyboard.Keyboard;
import org.getspout.spoutapi.player.SpoutPlayer;

import com.yurijware.bukkit.SpoutKeyCommands.SpoutKeyCommands.ChooseMode;

public class SpoutListener extends InputListener {
	
	@Override
	public void onKeyPressedEvent(KeyPressedEvent event) {
		if (event.getScreenType() != ScreenType.GAME_SCREEN) { return; }
		SpoutPlayer player = event.getPlayer();
		
		PlayerOptions po = PlayerOptions.getPlayer(player);
		String msg = "";
		
		if (Utils.isValidModifier(event.getKey())) {
			PlayerOptions.addPressed(player.getName(), event.getKey());
			return;
		}
		
		ChooseMode mode = PlayerOptions.getMode(player);
		
		boolean valid = Utils.isValidKey(player, event.getKey());
		if (!valid) {
			switch(mode) {
			case SET:
			case GSET:
				player.sendMessage(ChatColor.RED + "That key combination is not valid.");
			}
			return;
		}
		
		LinkedHashSet<Keyboard> allPressed = PlayerOptions.getPressed(player.getName());
		allPressed.add(event.getKey());
		
		if (mode == ChooseMode.NONE) {
			if (!Permission.check(player, "SpoutKeyCommands.use")) {
				return;
			}
			
			PlayerCmd pc = PlayerCmd.get(player, allPressed);
			GlobalCmd gc = GlobalCmd.get(allPressed);
			if (pc == null && gc == null) { return; }
			
			String perf = po.getPreferred();
			
			if (perf.equalsIgnoreCase("global") && gc != null || pc == null) {
				player.performCommand(gc.getCommand());
			} else if(perf.equalsIgnoreCase("personal") && pc != null || pc != null) {
				player.performCommand(pc.getCommand());
			}
			
			return;
		}
		
		String cmd = PlayerOptions.getCmd(player);
		
		PluginManager pm = Bukkit.getServer().getPluginManager();
		String pluginName = SpoutKeyCommands.getInstance().getDescription().getName();
		
		PlayerCmd pc = null;
		GlobalCmd gc = null;
		
		switch(mode) {
		case SET:
			if (cmd.equals("")) { return; }
			msg = ChatColor.DARK_AQUA + "Command set to: " +
					ChatColor.GREEN + Utils.getKeyString(allPressed);
			
			pc = PlayerCmd.get(player, allPressed);
			if (pc != null) {
				if (!pluginName.equals(pc.getPlugin())) {
					Plugin p = pm.getPlugin(pc.getPlugin());
					if (p != null && p.isEnabled()) {
						msg = ChatColor.RED + "That key is already set by " + pc.getPlugin();
						break;
					}
				}
				
				pc.setCommand(cmd);
				pc.setPlugin(pluginName);
				SpoutKeyCommands.getInstance().getDatabase().update(pc);
				break;
			}
			
			pc = new PlayerCmd(allPressed, cmd, player, SpoutKeyCommands.getInstance());
			SpoutKeyCommands.getInstance().getDatabase().save(pc);
			break;
			
		case GSET:
			if (cmd.equals("")) { return; }
			msg = ChatColor.DARK_AQUA + "Global command set to: " +
					ChatColor.GREEN + Utils.getKeyString(allPressed);
			
			gc = GlobalCmd.get(allPressed);
			if (gc != null) {
				if (!pluginName.equals(gc.getPlugin())) {
					Plugin p = pm.getPlugin(gc.getPlugin());
					if (p != null && p.isEnabled()) {
						msg = ChatColor.RED + "That key is already set by " + gc.getPlugin();
						break;
					}
				}
				
				gc.setCommand(cmd);
				gc.setPlugin(pluginName);
				SpoutKeyCommands.getInstance().getDatabase().update(gc);
				break;
			}
			
			gc = new GlobalCmd(allPressed, cmd, SpoutKeyCommands.getInstance());
			SpoutKeyCommands.getInstance().getDatabase().save(gc);
			break;
			
		case UNSET:
			pc = PlayerCmd.get(player, allPressed);
			if (pc == null) {
				msg = ChatColor.RED + "No command on this combination";
				break;
			}
			
			if (!pluginName.equals(pc.getPlugin())) {
				Plugin p = pm.getPlugin(pc.getPlugin());
				if (p != null && p.isEnabled()) {
					msg = ChatColor.RED + "That key is set by " + pc.getPlugin();
					break;
				}
			}
			
			SpoutKeyCommands.getInstance().getDatabase().delete(pc);
			msg = ChatColor.DARK_AQUA + "Command unset from: " +
					ChatColor.GREEN + Utils.getKeyString(allPressed);
			break;
			
		case GUNSET:
			gc = GlobalCmd.get(allPressed);
			if (gc == null) {
				msg = ChatColor.RED + "No global command on this combination";
				break;
			}
			
			if (!pluginName.equals(gc.getPlugin())) {
				Plugin p = pm.getPlugin(gc.getPlugin());
				if (p != null && p.isEnabled()) {
					msg = ChatColor.RED + "That key is already set by " + gc.getPlugin();
					break;
				}
			}
			
			SpoutKeyCommands.getInstance().getDatabase().delete(gc);
			msg = ChatColor.DARK_AQUA + "Global command unset from: " +
					ChatColor.GREEN + Utils.getKeyString(allPressed);
			break;
		}
		
		player.sendMessage(msg);
	}
	
	@Override
	public void onKeyReleasedEvent(KeyReleasedEvent event) {
		SpoutPlayer player = event.getPlayer();
		PlayerOptions.removePressed(player.getName(), event.getKey());
	}
	
}
