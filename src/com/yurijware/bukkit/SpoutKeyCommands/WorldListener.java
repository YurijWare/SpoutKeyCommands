package com.yurijware.bukkit.SpoutKeyCommands;

import org.bukkit.event.world.WorldSaveEvent;

public class WorldListener extends org.bukkit.event.world.WorldListener {
	private final SpoutKeyCommands plugin;
	
	public WorldListener(SpoutKeyCommands plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void onWorldSave(WorldSaveEvent event) {
		this.plugin.saveToDB();
		this.plugin.log.info(SpoutKeyCommands.logPrefix + "Key commands saved");
	}
	
}
