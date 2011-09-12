package com.yurijware.bukkit.SpoutKeyCommands;

import java.util.Iterator;
import java.util.LinkedHashSet;

import org.getspout.spoutapi.keyboard.Keyboard;
import org.getspout.spoutapi.player.SpoutPlayer;

public class Utils {
	
	static String getKeyString(LinkedHashSet<Keyboard> list) {
		StringBuffer s = new StringBuffer();
		Iterator<Keyboard> itr = list.iterator();
		while (itr.hasNext()) {
			s.append(itr.next().toString().replaceAll("KEY_", ""));
			if (itr.hasNext()) {
				s.append(" + ");
			}
		}
		return s.toString();
	}
	
	static boolean isValidKey(SpoutPlayer p, Keyboard k) {
		if (k == p.getBackwardKey() || k == p.getChatKey() ||
				k == p.getDropItemKey() || k == p.getForwardKey() ||
				k == p.getInventoryKey() || k == p.getJumpKey() ||
				k == p.getLeftKey() || k == p.getRightKey() ||
				k == p.getSneakKey() || k == p.getToggleFogKey()) { return false; }
		switch(k) {
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
		return true;
	}
	
	static boolean isValidModifier(Keyboard key) {
		switch (key) {
		case KEY_LCONTROL:
		case KEY_LSHIFT:
		case KEY_RCONTROL:
		case KEY_RSHIFT:
			return true;
		}
		return false;
	}
	
}
