/*
 * Slime in a Bucket Spigot Plugin for Minecraft 1.13.1
 * Version 1.0
 * @Author: Jamdoggy
 * @Description: A recreation of the Slime in a Bucket found in the Quark Forge Mod
 */

package jamSlimeBucket;

import java.util.ArrayList;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

public class SlimeBucket implements Listener {

	public static Item slime_bucket;
	boolean debug=true;

	enum EnumHand {
		   MAIN_HAND, OFF_HAND, NO_HAND
		}

	/*
	 * Constructor
	 */
	public SlimeBucket() {
    	System.out.println("[SlimeBucket] Initialising plugin.");

	}
	
	/*
	 * Handler: Catch a slime in a bucket
	 */
	@EventHandler
	public void entityInteract(PlayerInteractEntityEvent event) {
		
    	if (debug == true) System.out.println("[SlimeBucket] Entity Interact detected.");
		// Did the player right-click on a slime?
		if (!event.isCancelled() && event.getRightClicked() != null && event.getRightClicked() instanceof Slime) {
			if (debug == true) System.out.println("[SlimeBucket] Detected right-click on slime.");
			// Is it a small slime?
			Slime slimeEntity = (Slime) event.getRightClicked();
			if (slimeEntity.getSize() == 1) {
				if (debug == true) System.out.println("[SlimeBucket] Small slime: OK.");
				// Does the player hold an empty bucket?
				EnumHand hand = EnumHand.MAIN_HAND;
				Player p = event.getPlayer();
				ItemStack stack = p.getInventory().getItemInMainHand();
				if(stack == null || stack.getType() != Material.BUCKET) {
					stack = p.getInventory().getItemInOffHand();
					hand = EnumHand.OFF_HAND;
				}
				
				// Player is holding a bucket!
				if(stack != null && stack.getType() == Material.BUCKET) {
					
					if (debug == true) System.out.println("[SlimeBucket] Holding bucket in ." + ((hand == EnumHand.MAIN_HAND) ? "Main " : "Off-") + "Hand.");
					// Finally, time to swap the bucket for the Slime in a Bucket
					ItemStack bucketSlime = createSlimeBucket(); 

					// How many buckets are in the hand?
					if (stack.getAmount() == 1) {  // One bucket - straight swap
						if (hand == EnumHand.MAIN_HAND)
							p.getInventory().setItemInMainHand(bucketSlime);
						else if (hand == EnumHand.OFF_HAND)
							p.getInventory().setItemInOffHand(bucketSlime);
					} else {  // More than one bucket, reduce stack and try to drop in inventory
						// Reduce the stack of buckets by 1
						int b = stack.getAmount();
						stack.setAmount(b-1);
						
						// Throw the slimebucket into the players' inventory
						if (p.getInventory().addItem(bucketSlime) == null) {
							// If it failed, drop one on the floor
							p.getWorld().dropItemNaturally(p.getLocation(), bucketSlime);
						}
					}

					// Remove the slime from the game
					slimeEntity.remove();
					
					// We did stuff, cancel the right-click event
					event.setCancelled(true);

					// TODO: Add arm swing animation
				}
				
			}
		}
	}

	/*
	 * Handler: Release the slime from the bucket
	 */
	@EventHandler
	public void playerInteract(PlayerInteractEvent event) {
		
		// Did the player right-click on a block?
		if (!event.isCancelled() && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			// With a slime in a bucket...?
			Player p = event.getPlayer();
			Block b = event.getClickedBlock();
			EnumHand h = hasSlimeBucket(p);
			
			if (h != EnumHand.NO_HAND) {
				if (debug == true) System.out.println("[SlimeBucket] Player Interact detected.");
				// Player has right-clicked a block with a slime-in-a-bucket
				// Remove the Slime-in-a-bucket
				if (h == EnumHand.MAIN_HAND) {
					ItemStack stack = p.getInventory().getItemInMainHand();
					stack.setAmount(stack.getAmount()-1);
				} else if (h == EnumHand.OFF_HAND) {
					ItemStack stack = p.getInventory().getItemInOffHand();
					stack.setAmount(stack.getAmount()-1);
				}
				
				//  and give player a bucket
				ItemStack bucketItem = new ItemStack(Material.BUCKET);
				if (p.getInventory().addItem(bucketItem) == null) {
					// If it failed, drop one on the floor
					p.getWorld().dropItemNaturally(p.getLocation(), bucketItem);
				}
				
				// and put a slime on the floor
				Location spawnLoc = b.getLocation();
				Slime s = (Slime) p.getWorld().spawnEntity(spawnLoc, EntityType.SLIME);
				s.setSize(1);
				
				// We did stuff, cancel the right-click event
				event.setCancelled(true);
			}
		}
	}

	/*
	 * Has the player moved into a new chunk.  If so, check if the bucket needs updating
	 */
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
	    if ((event != null) && !event.isCancelled()) {
	        Chunk chunkFrom = event.getFrom().getChunk(); // initial chunk
	        Chunk chunkTo = event.getTo().getChunk();     // current chunk

	        if (chunkFrom.getX() != chunkTo.getX() || chunkFrom.getZ() != chunkTo.getZ()) {
	            // if chunk has changed
	            updateSlimeBuckets(event.getPlayer()); // update any "buckets" in the players inventory
	        }
	    }
	}
	
	/*
	 * Go through player inventory, and if they have a slime-in-a-bucket then check if it needs updating
	 */
	public void updateSlimeBuckets(Player p) {
		if (p != null) {
			PlayerInventory inv = p.getInventory();
			for (ItemStack item : inv.getContents()) {
				if ((item != null) && (item.getType() == Material.GOLDEN_HOE)) {
					//  Check that this gold hoe is a slime-in-a-bucket
					if (((item.getDurability() == (short) 1) || (item.getDurability() == (short) 2)) && (item.getItemMeta().isUnbreakable() == true)) {
						boolean slime = p.getLocation().getChunk().isSlimeChunk();
						short meta = item.getDurability();
						short newMeta = (short) (slime ? 2 : 1);
						

						if (debug == true) {
							System.out.println("[SlimeBucket] Player " + (p.getName()) + " moved to a new chunk with a slime bucket.");
							System.out.println("[SlimeBucket] Slime chunk: " + (slime ? "True" : "False"));
							System.out.println("[SlimeBucket] Old Value: " + meta);
							System.out.println("[SlimeBucket] New Value: " + newMeta);
						}
						if(meta != newMeta) {
							item.setDurability((short) newMeta);
							if (debug == true) System.out.println("[SlimeBucket] Damage value updated!");
						}
					}
				}
			}
		}
	}
	
	/*
	 * Does the player hold a slime bucket in their main hand (or off hand) - return hand or NO_HAND
	 */
	private EnumHand hasSlimeBucket(Player p) {
		// Does the player hold an empty bucket?
		EnumHand hand = EnumHand.MAIN_HAND;
		ItemStack stack = p.getInventory().getItemInMainHand();
		
		if(stack == null || stack.getType() != Material.GOLDEN_HOE) {
			stack = p.getInventory().getItemInOffHand();
			hand = EnumHand.OFF_HAND;
		}

		if(stack != null && stack.getType() == Material.GOLDEN_HOE) {
			// Player has a golden hoe, but it is the right type...?
			ItemMeta stackMeta = stack.getItemMeta();
			
			if (((stack.getDurability() != (short) 1) && (stack.getDurability() != (short) 2)) || (stackMeta.isUnbreakable() != true)) {
				hand = EnumHand.NO_HAND;
			}
		} else {
			hand = EnumHand.NO_HAND;
		}
		return hand;
	}
	
	/*
	 * Create a new slime-in-a-bucket
	 */
	private ItemStack createSlimeBucket() {
		// Create an Unbreakable Golden Hoe, with damage 1 and Hide Flags 
		ItemStack bucketSlime = new ItemStack(Material.GOLDEN_HOE);
		ItemMeta sibMeta = bucketSlime.getItemMeta();

		sibMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a&lSlime in a Bucket"));
		
		ArrayList<String> lore = new ArrayList<String>();
        lore.add("This slime gets excited when in a slime chunk.");
        sibMeta.setLore(lore);
        
        sibMeta.setUnbreakable(true);
        sibMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        sibMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        
        bucketSlime.setItemMeta(sibMeta);
		bucketSlime.setDurability((short) 1);
		
		return bucketSlime;
	}
}