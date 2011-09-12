package com.yurijware.bukkit.SpoutKeyCommands;

import java.util.HashMap;
import java.util.LinkedHashSet;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.bukkit.entity.Player;
import org.getspout.spoutapi.keyboard.Keyboard;

import com.avaje.ebean.validation.NotEmpty;
import com.yurijware.bukkit.SpoutKeyCommands.SpoutKeyCommands.ChooseMode;

@Entity()
@Table(name = "SpoutKeyCommands_PlayerConf")
public class PlayerOptions {
	
	private static HashMap<Player,ChooseMode> mode = new HashMap<Player,ChooseMode>();
	private static HashMap<Player,String> cmd = new HashMap<Player,String>();
	private static HashMap<String,LinkedHashSet<Keyboard>> pressed =
			new HashMap<String,LinkedHashSet<Keyboard>>();
	
	@Id
	private int id;
	@NotEmpty
	private String player;
	@NotEmpty
	private String preferred;
	
	
	protected static PlayerOptions getPlayer(Player player) {
		return getPlayer(player.getName());
	}
	
	protected static PlayerOptions getPlayer(String player) {
		PlayerOptions po = SpoutKeyCommands.getInstance().getDatabase().find(PlayerOptions.class)
				.where().ieq("player", player).findUnique();
		if (po == null) {
			po = new PlayerOptions(player);
			SpoutKeyCommands.getInstance().getDatabase().save(po);
		}
		return po;
	}
	
	
	protected static ChooseMode getMode(Player player) {
		ChooseMode m = ChooseMode.NONE;
		if (mode.containsKey(player)) {
			m = mode.get(player);
			mode.remove(player);
		}
		return m;
	}
	
	protected static void setMode(Player player, ChooseMode m){
		mode.put(player, m);
	}
	
	
	protected static String getCmd(Player player) {
		String c = "";
		if (cmd.containsKey(player)) {
			c = cmd.get(player);
			cmd.remove(player);
		}
		return c;
	}
	
	protected static void setCmd(Player player, String c) {
		cmd.put(player, c);
	}
	
	
	public PlayerOptions() {
		
	}
	
	public PlayerOptions(String player) {
		this.player = player;
		this.preferred = GlobalOptions.getConf("default preferred");
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getPlayer() {
		return this.player;
	}
	
	public void setPlayer(String player) {
		this.player = player;
	}
	
	public String getPreferred() {
		return this.preferred;
	}
	
	public void setPreferred(String preferred) {
		this.preferred = preferred;
	}
	
	
	public static LinkedHashSet<Keyboard> getPressed(String player) {
		if (pressed.containsKey(player)) {
			return pressed.get(player);
		}
		return new LinkedHashSet<Keyboard>();
	}
	
	public static void setPressed(String player, LinkedHashSet<Keyboard> set) {
		pressed.put(player, set);
	}
	
	public static void addPressed(String player, Keyboard key) {
		if (!Utils.isValidModifier(key)) { return; }
		
		if (pressed.containsKey(player)) {
			pressed.get(player).add(key);
			return;
		}
		
		LinkedHashSet<Keyboard> set = new LinkedHashSet<Keyboard>();
		set.add(key);
		pressed.put(player, set);
	}
	
	public static void removePressed(String player, Keyboard key) {
		if (!pressed.containsKey(player)) { return; }
		pressed.get(player).remove(key);
	}
	
}
