package com.yurijware.bukkit.SpoutKeyCommands;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.avaje.ebean.validation.NotEmpty;

@Entity()
@Table(name = "SpoutKeyCommands_GlobalConf")
public class GlobalOptions {
	
	protected static String getConf(String config) {
		GlobalOptions opt = SpoutKeyCommands.getInstance().getDatabase().find(GlobalOptions.class)
				.where().ieq("config", config).findUnique();
		if (opt != null) {
			return opt.getValue();
		}
		return null;
	}
	
	protected static String getConf(String config, String def) {
		GlobalOptions opt = SpoutKeyCommands.getInstance().getDatabase().find(GlobalOptions.class)
				.where().ieq("config", config).findUnique();
		if (opt != null) {
			return opt.getValue();
		}
		opt = new GlobalOptions(config, def);
		SpoutKeyCommands.getInstance().getDatabase().save(opt);
		return def;
	}
	
	protected static void setConf(String config, String value) {
		GlobalOptions opt = SpoutKeyCommands.getInstance().getDatabase().find(GlobalOptions.class)
				.where().ieq("config", config).findUnique();
		if (opt != null) {
			opt.setValue(value);
			SpoutKeyCommands.getInstance().getDatabase().update(opt);
			return;
		}
		opt = new GlobalOptions(config, value);
		SpoutKeyCommands.getInstance().getDatabase().save(opt);
	}
	
	@Id
	private int id;
	@NotEmpty
	private String config;
	@NotEmpty
	private String value;
	
	public GlobalOptions() {
		
	}
	
	public GlobalOptions(String config, String value) {
		this.config = config;
		this.value = value;
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getConfig() {
		return this.config;
	}
	
	public void setConfig (String config) {
		this.config = config;
	}
	
	public String getValue() {
		return this.value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
}
