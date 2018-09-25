/*
 * Slime in a Bucket Spigot Plugin for Minecraft 1.13.1
 * Version 1.0
 * @Author: Jamdoggy
 * @Description: A recreation of the Slime in a Bucket found in the Quark Forge Mod
 *               Written as a server-side mod for Spigot
 *               NOTE: Requires server-enforced resource pack
 */

package jamSlimeBucket;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import jamSlimeBucket.SlimeBucket;

public class Main extends JavaPlugin {
	public static Main instance;

	@Override
	public void onEnable() {
		
		instance = this;
		
		registerConfig();
		
		this.getServer().getPluginManager().registerEvents(new SlimeBucket(), this);

		BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
            	
            }
        }, 0L, 20l);
	}

	@Override
	public void onDisable() {
		
	}

	public void registerConfig() {
		saveDefaultConfig();
	}
}