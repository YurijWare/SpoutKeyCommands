package com.yurijware.bukkit.SpoutKeyCommands;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import org.getspout.spoutapi.keyboard.Keyboard;
import org.getspout.spoutapi.player.SpoutPlayer;

import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import com.alta189.sqlLibrary.MySQL.mysqlCore;
import com.alta189.sqlLibrary.SQLite.sqlCore;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class SpoutKeyCommands extends JavaPlugin {
    protected final Logger log = Logger.getLogger("Minecraft");
	protected static PluginDescriptionFile pdfFile = null;
	protected static String logPrefix = null;
	
	public static enum ChooseMode { SET, UNSET }
	
	private static String path = "plugins" + File.separator + "SpoutKeyCommands";
	private static final File configFolder = new File(path);
	private static final File configFile = new File(configFolder, "config.yml");
	private static Configuration conf = new Configuration(configFile);
	
	protected static PermissionManager permissionsExHandler;
	protected static PermissionHandler permissionsHandler;
	
	protected mysqlCore manageMySQL;
	protected sqlCore manageSQLite;
	protected Boolean MySQL = false;
	protected String dbHost = null;
	protected String dbUser = null;
	protected String dbPass = null;
	protected String dbPrefix = null;
	protected String dbDatabase = null;
	protected String dbPath = null;
	
	protected LinkedHashMap<String,Config> listPlayerConfig = new LinkedHashMap<String,Config>();
	protected LinkedHashMap<String,ChooseMode> listPlayerMode = new LinkedHashMap<String,ChooseMode>();
	protected LinkedHashMap<String,String> listPlayerCmdTemp = new LinkedHashMap<String,String>();
	
	protected LinkedHashMap<Keyboard,String> listGlobalCmds = new LinkedHashMap<Keyboard,String>();
	protected LinkedHashMap<Keyboard,String> listGlobalPlugin = new LinkedHashMap<Keyboard,String>();
	
	protected SpoutKeyCommandsHandler handle = new SpoutKeyCommandsHandler(this);
	
	private SpoutListener SpoutListener = new SpoutListener(this);
	private WorldListener WorldListener = new WorldListener(this);
	
	@Override
	public void onDisable() {
		log.info(logPrefix + "Plugin disabled!");
	}
	
	@Override
	public void onEnable() {
		pdfFile = this.getDescription();
		logPrefix = "[" + pdfFile.getName() + "] ";
		
		PluginManager pm = this.getServer().getPluginManager();
		
		Plugin spout = pm.getPlugin("Spout");
		if (spout != null && spout.isEnabled()) {
			String v = spout.getDescription().getVersion();
			log.info(logPrefix + "Spout detected! Using version " + v);
		} else {
			log.severe(logPrefix + "Spout not found. Please install it for this plugin to work.");
			pm.disablePlugin(this);
		}
		
		Plugin permissionsEx = pm.getPlugin("PermissionsEx");
		Plugin permissions = pm.getPlugin("Permissions");
		if (permissionsEx != null && permissionsEx.isEnabled()) {
			permissionsExHandler = PermissionsEx.getPermissionManager();
			String v = permissionsEx.getDescription().getVersion();
			log.info(logPrefix + "PermissionsEx detected! Using version " + v);
			
		} else if (permissions != null && permissions.isEnabled()) {
			permissionsHandler = ((Permissions) permissions).getHandler();
			String v = permissions.getDescription().getVersion();
			log.info(logPrefix + "Permissions detected! Using version " + v);
			
		}

		loadConfig();
		
		this.getCommand("SpoutKeyCommands").setExecutor(new Commands(this));
		
		pm.registerEvent(Event.Type.CUSTOM_EVENT, SpoutListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.WORLD_SAVE, WorldListener, Priority.Normal, this);
		
		log.info(logPrefix + "Version " + pdfFile.getVersion()
				+ " is enabled!");
	}
	
	private void loadConfig() {
		createDefaultConfiguration("config.yml", "config.yml");
		conf.load();
		initDB();
		loadAllPlayerKeys();
		loadAllGlobalKeys();
	}
	
	private void createDefaultConfiguration(String name, String filename) {
		File file = new File(configFolder, filename);
		
		if (configFile.exists()) { return; }
		if (!configFolder.exists()) {
			configFolder.mkdir();
		}
		
		InputStream input = SpoutKeyCommands.class
				.getResourceAsStream("/resources/" + name);
		if (input != null) {
			FileOutputStream output = null;
			try {
				output = new FileOutputStream(file);
				byte[] buf = new byte[8192];
				int length = 0;
				while ((length = input.read(buf)) > 0) {
					output.write(buf, 0, length);
				}
				
				this.log.info(getDescription().getName() + ": Created configuration file: " + name);
			} catch (IOException e) {
				e.printStackTrace();
				try {
					if (input != null)
						input.close();
				} catch (IOException localIOException) { }
				try {
					if (output != null)
						output.close();
				} catch (IOException localIOException) { }
			} finally {
				try {
					if (input != null)
						input.close();
				} catch (IOException localIOException) { }
				try {
					if (output != null)
						output.close();
				} catch (IOException localIOException) { }
			}
		}
	}
	
	private void initDB(){
		String query = "CREATE TABLE SpoutKeyCommands " +
				"(player VARCHAR(255), key INT, command VARCHAR(255), plugin VARCHAR(255));";
		String query_global = "CREATE TABLE SpoutKeyCommandsGlobal " +
				"(key INT, command VARCHAR(255), plugin VARCHAR(255));";
		
		String db = conf.getString("config.datatype", "sqlite");
		if (db.equalsIgnoreCase("mysql")) {
			this.MySQL = true;
			this.dbHost = conf.getString("config.mysql.host");
			this.dbUser = conf.getString("config.mysql.username");
			this.dbPass = conf.getString("config.mysql.password");
			this.dbPrefix = conf.getString("config.mysql.prefix");
			this.dbDatabase = conf.getString("config.mysql.database");
		} else if (db.equalsIgnoreCase("sqlite")) {
			this.MySQL = false;
		} else {
			this.log.warning(logPrefix + "Could not recoginize wich database type to use.");
			this.log.warning(logPrefix + "Defaulting to SqLite");
			this.MySQL = false;
		}
		
		if (this.MySQL) {
			if (this.dbHost.equals(null)) { this.MySQL = false; this.log.severe(logPrefix + "MySQL is on, but host is not defined, defaulting to SQLite"); }
			if (this.dbUser.equals(null)) { this.MySQL = false; this.log.severe(logPrefix + "MySQL is on, but username is not defined, defaulting to SQLite"); }
			if (this.dbPass.equals(null)) { this.MySQL = false; this.log.severe(logPrefix + "MySQL is on, but password is not defined, defaulting to SQLite"); }
			if (this.dbDatabase.equals(null)) { this.MySQL = false; this.log.severe(logPrefix + "MySQL is on, but database is not defined, defaulting to SQLite"); }
		} else {
			this.dbPath = conf.getString("config.sqlite.database-file",
					configFolder.getPath() + File.separator + "database.db");
		}
		
		if (this.MySQL) {
			this.manageMySQL = new mysqlCore(this.log, logPrefix, this.dbHost,
					this.dbDatabase, this.dbUser, this.dbPass);
			
			this.log.info(logPrefix + "MySQL Initializing");
			
			this.manageMySQL.initialize();
			try {
				if (this.manageMySQL.checkConnection()) { // Check if the Connection was successful
					this.log.info(logPrefix + "MySQL connection successful");
					if (!this.manageMySQL.checkTable("SpoutKeyCommands")) { // Check if the table exists in the database if not create it
						this.log.info(logPrefix + "Creating table SpoutKeyCommands");
						this.manageMySQL.createTable(query); // Use mysqlCore.createTable(query) to create tables
					}
					if (!this.manageMySQL.checkTable("SpoutKeyCommandsGlobal")) { // Check if the table exists in the database if not create it
						this.log.info(logPrefix + "Creating table SpoutKeyCommandsGlobal");
						this.manageMySQL.createTable(query_global); // Use mysqlCore.createTable(query) to create tables
					}
				} else {
					this.log.severe(logPrefix + "MySQL connection failed");
					this.MySQL = false;
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} else {
			this.log.info(logPrefix + "SQLite Initializing");
			this.log.info(logPrefix + "SqLite database path: " + this.dbPath);
			
			this.manageSQLite = new sqlCore(this.log, logPrefix, this.dbPath);
			
			this.manageSQLite.initialize();
			
			if (!this.manageSQLite.checkTable("SpoutKeyCommands")) {
				this.log.info(logPrefix + "Creating table SpoutKeyCommands");
				this.manageSQLite.createTable(query);
			}
			
			if (!this.manageSQLite.checkTable("SpoutKeyCommandsGlobal")) {
				this.log.info(logPrefix + "Creating table SpoutKeyCommandsGlobal");
				this.manageSQLite.createTable(query_global);
			}
		}
	}
	
	protected void loadAllPlayerKeys() {
		String query = "SELECT * FROM SpoutKeyCommands;";
		ResultSet result = null;
		
		if (this.MySQL.booleanValue()) {
			try {
				result = this.manageMySQL.sqlQuery(query);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} else {
			result = this.manageSQLite.sqlQuery(query);
		}
		
		this.listPlayerConfig.clear();
		
		try {
			while(result != null && result.next()) {
				String player = result.getString("player");
				int key = result.getInt("key");
				String cmd = result.getString("command");
				String p = result.getString("plugin");
//				log.info(logPrefix + player + ": Adding cmd: '" + cmd + "' to: " +
//						Keyboard.getKey(key).toString());
				if(listPlayerConfig.containsKey(player)){
					listPlayerConfig.get(player) .addCommand(Keyboard.getKey(key), cmd, p);
				} else {
					Config c = new Config(player);
					c.addCommand(Keyboard.getKey(key), cmd, p);
					listPlayerConfig.put(player, c);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
	}
	
	protected void loadAllGlobalKeys() {
		String query = "SELECT * FROM SpoutKeyCommandsGlobal;";
		ResultSet result = null;
		
		if (this.MySQL.booleanValue()) {
			try {
				result = this.manageMySQL.sqlQuery(query);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} else {
			result = this.manageSQLite.sqlQuery(query);
		}
		
		listGlobalCmds.clear();
		listGlobalPlugin.clear();
		
		try {
			while(result != null && result.next()) {
				Keyboard key = Keyboard.getKey(result.getInt("key"));
				String cmd = result.getString("command");
				String p = result.getString("plugin");
//				log.info(logPrefix + player + ": Adding cmd: '" + cmd + "' to: " +
//						Keyboard.getKey(key).toString());
				listGlobalCmds.put(key, cmd);
				listGlobalPlugin.put(key, p);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	protected void saveToDB() {
		String query = "DELETE FROM SpoutKeyCommands;";
		String query_global = "DELETE FROM SpoutKeyCommandsGlobal;";
		
		if (this.MySQL) {
			try {
				this.manageMySQL.deleteQuery(query);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} else {
			this.manageSQLite.deleteQuery(query);
		}
		
		if (this.MySQL) {
			try {
				this.manageMySQL.deleteQuery(query_global);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} else {
			this.manageSQLite.deleteQuery(query_global);
		}
		
		Iterator<Config> configs = this.listPlayerConfig.values().iterator();
		
		for(Iterator<Config> i = configs; i.hasNext();){
			Config c = i.next();
			String player = c.getPlayer();
			LinkedHashMap<Keyboard, String> cmds = c.getCommands();
			for (Keyboard key : cmds.keySet())
			{
				insertDbPlayerRow(player, key, cmds.get(key), c.getPlugin(key));
			}
			
		}
		
		for(Iterator<Keyboard> i = this.listGlobalCmds.keySet().iterator(); i.hasNext();){
			Keyboard key = i.next();
			String cmd = this.listGlobalCmds.get(key);
			String plugin = this.listGlobalPlugin.get(key);
			insertDbGlobalRow(key, cmd, plugin);
		}
	}
	
	private void insertDbPlayerRow(String player, Keyboard key, String cmd, String plugin) {
		String query = "INSERT INTO SpoutKeyCommands (player, key, command, plugin) VALUES ('"
				+ player + "', " + key.getKeyCode() + ", '" + cmd + "', '" + plugin +  "');";
		
		if (this.MySQL) {
			try {
				this.manageMySQL.insertQuery(query);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} else {
			this.manageSQLite.insertQuery(query);
		}
	}
	
	private void insertDbGlobalRow(Keyboard key, String cmd, String plugin) {
		String query = "INSERT INTO SpoutKeyCommandsGlobal (key, command, plugin) VALUES ("
				+ key.getKeyCode() + ", '" + cmd + "', '" + plugin +  "');";
		
		if (this.MySQL) {
			try {
				this.manageMySQL.insertQuery(query);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} else {
			this.manageSQLite.insertQuery(query);
		}
	}
	
	protected String getKeyCommand(String player, Keyboard key) {
		if (this.listPlayerConfig.containsKey(player)) {
			return this.listPlayerConfig.get(player).getCommand(key);
		}
		return "";
	}

	protected String getGlobalKeyCommand(Keyboard key) {
		if (this.listGlobalCmds.containsKey(key)) {
			return this.listGlobalCmds.get(key);
		}
		return "";
	}
	
	protected boolean isValidKey(SpoutPlayer p, Keyboard k) {
		if (k == p.getBackwardKey() || k == p.getChatKey() ||
				k == p.getDropItemKey() || k == p.getForwardKey() ||
				k == p.getInventoryKey() || k == p.getJumpKey() ||
				k == p.getLeftKey() || k == p.getRightKey() ||
				k == p.getSneakKey() || k == p.getToggleFogKey() ||
				k == Keyboard.KEY_ESCAPE || k == Keyboard.KEY_1 ||
				k == Keyboard.KEY_2 || k == Keyboard.KEY_3 ||
				k == Keyboard.KEY_4 || k == Keyboard.KEY_5 ||
				k == Keyboard.KEY_6 || k == Keyboard.KEY_7 ||
				k == Keyboard.KEY_8 || k == Keyboard.KEY_9) { return false; }
		return true;
	}
	
	public SpoutKeyCommandsHandler getHandle() {
		return handle;
	}
	
	protected boolean checkPermissions(CommandSender sender, String node) {
		if (permissionsExHandler != null && sender instanceof Player) {
			return permissionsExHandler.has((Player) sender, node);
		} else if (permissionsHandler != null && sender instanceof Player) {
			return permissionsHandler.has((Player) sender, node);
		} else {
			return sender.hasPermission(node);
		}
	}
	
}
