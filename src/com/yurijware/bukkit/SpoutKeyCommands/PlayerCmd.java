package com.yurijware.bukkit.SpoutKeyCommands;

import java.util.LinkedHashSet;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.getspout.spoutapi.keyboard.Keyboard;

import com.avaje.ebean.validation.NotEmpty;

@Entity()
@Table(name = "SpoutKeyCommands_Personal")
public class PlayerCmd {
	
	@Id
	private int id;
	@NotEmpty
	private String player;
	@NotEmpty
	private String keys;
	@NotEmpty
	private String command;
	@NotEmpty
	private String plugin;
	
	
	protected static PlayerCmd get(Player player, LinkedHashSet<Keyboard> keys) {
		return get(player.getName(), keys);
	}
	
	protected static PlayerCmd get(String player, LinkedHashSet<Keyboard> keys) {
		PlayerCmd pc = SpoutKeyCommands.getInstance().getDatabase().find(PlayerCmd.class)
				.where().ieq("player", player).eq("keys", Utils.getKeyString(keys)).findUnique();
		return pc;
	}
	
	protected static List<PlayerCmd> getList(Player player) {
		return getList(player.getName());
	}
	
	protected static List<PlayerCmd> getList(String player) {
		return SpoutKeyCommands.getInstance().getDatabase().find(PlayerCmd.class)
				.where().ieq("player", player).findList();
	}
	
	protected static void clear(Player player) {
		clear(player.getName());
	}
	
	protected static void clear(String player) {
		List<PlayerCmd> list = SpoutKeyCommands.getInstance().getDatabase().find(PlayerCmd.class)
				.where().ieq("player", player).findList();
		SpoutKeyCommands.getInstance().getDatabase().delete(list);
	}
	
	
	public PlayerCmd() {
		
	}
	
	public PlayerCmd(LinkedHashSet<Keyboard> list, String command, Player player, Plugin plugin) {
		this.keys = Utils.getKeyString(list);
		this.command = command;
		this.player = player.getName();
		this.plugin = plugin.getDescription().getName();
	}
	
	public PlayerCmd(LinkedHashSet<Keyboard> list, String command, String player, Plugin plugin) {
		this.keys = Utils.getKeyString(list);
		this.command = command;
		this.player = player;
		this.plugin = plugin.getDescription().getName();
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getKeys() {
		return keys;
	}
	
	public void setKeys(String keys) {
		this.keys = keys;
	}
	
	public String getCommand() {
		return this.command;
	}
	
	public void setCommand(String command) {
		this.command = command;
	}
	
	public String getPlayer() {
		return this.player;
	}
	
	public void setPlayer(String player) {
		this.player = player;
	}
	
	public String getPlugin() {
		return this.plugin;
	}
	
	public void setPlugin(String plugin) {
		this.plugin = plugin;
	}
	
}
