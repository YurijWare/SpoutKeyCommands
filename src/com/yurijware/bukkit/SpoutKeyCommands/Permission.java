package com.yurijware.bukkit.SpoutKeyCommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class Permission {
	
	private static PermissionManager permissionsExHandler;
	private static PermissionHandler permissionsHandler;
	
	protected static void checkPermSupport() {
		PluginManager pm = Bukkit.getServer().getPluginManager();
		
		Plugin permissionsEx = pm.getPlugin("PermissionsEx");
		Plugin permissions = pm.getPlugin("Permissions");
		if (permissionsEx != null && permissionsEx.isEnabled()) {
			permissionsExHandler = PermissionsEx.getPermissionManager();
			String v = permissionsEx.getDescription().getVersion();
			SpoutKeyCommands.LogInfo("PermissionsEx detected! Using version " + v);
			
		} else if (permissions != null && permissions.isEnabled()) {
			permissionsHandler = ((Permissions) permissions).getHandler();
			String v = permissions.getDescription().getVersion();
			SpoutKeyCommands.LogInfo("Permissions detected! Using version " + v);
		}
	}
	
	protected static boolean check(CommandSender sender, String node) {
		if (permissionsExHandler != null && sender instanceof Player) {
			return permissionsExHandler.has((Player) sender, node);
		} else if (permissionsHandler != null && sender instanceof Player) {
			return permissionsHandler.has((Player) sender, node);
		} else {
			return sender.hasPermission(node);
		}
	}
	
}
