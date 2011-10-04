package com.yurijware.bukkit.SpoutKeyCommands;

import java.util.LinkedHashSet;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.bukkit.plugin.Plugin;
import org.getspout.spoutapi.keyboard.Keyboard;

import com.avaje.ebean.validation.NotEmpty;

@Entity()
@Table(name = "SpoutKeyCommands_Global")
public class GlobalCmd {
	
	@Id
	private int id;
	@NotEmpty
	private String combination;
	@NotEmpty
	private String command;
	@NotEmpty
	private String plugin;
	
	
	protected static GlobalCmd get(LinkedHashSet<Keyboard> keys) {
		GlobalCmd gk = SpoutKeyCommands.getInstance().getDatabase().find(GlobalCmd.class)
				.where().eq("combination", Utils.getKeyString(keys)).findUnique();
		return gk;
	}
	
	protected static List<GlobalCmd> getList() {
		return SpoutKeyCommands.getInstance().getDatabase().find(GlobalCmd.class).findList();
	}
	
	protected static void clear() {
		List<GlobalCmd> list = SpoutKeyCommands.getInstance().getDatabase()
				.find(GlobalCmd.class).findList();
		SpoutKeyCommands.getInstance().getDatabase().delete(list);
	}
	
	
	public GlobalCmd() {
		
	}	
	
	public GlobalCmd(LinkedHashSet<Keyboard> list, String command, Plugin plugin) {
		this.combination = Utils.getKeyString(list);
		this.command = command;
		this.plugin = plugin.getDescription().getName();
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getCombination() {
		return combination;
	}
	
	public void setCombination(String combination) {
		this.combination = combination;
	}
	
	public String getCommand() {
		return this.command;
	}
	
	public void setCommand(String command) {
		this.command = command;
	}
	
	public String getPlugin() {
		return this.plugin;
	}
	
	public void setPlugin(String plugin) {
		this.plugin = plugin;
	}
	
}
