/*
 * Slime in a Bucket Spigot Plugin for Minecraft 1.13.1
 * Version 1.0
 * @Author: Jamdoggy
 * @Description: A recreation of the Slime in a Bucket found in the Quark Forge Mod
 *               Written as a server-side mod for Spigot
 *               NOTE: Requires server-enforced resource pack
 */

package jamSlimeBucket;

import java.util.ArrayList;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Consumer;

import net.md_5.bungee.api.ChatColor;

public class SlimeBucket implements Listener {

	public static Item slime_bucket;     // Slime Bucket Item
	boolean debug=true;                  // Flag to show/hide debug information in logs  (Default: False)
	boolean logs=true;                   // Flag to show/hide normal log information     (Default: True)

	enum EnumHand {
		   MAIN_HAND, OFF_HAND, NO_HAND
		}

	/*
	 * Constructor
	 */
	public SlimeBucket() {
    	System.out.println("[SlimeBucket] Initialising plugin.");
    	if (Main.instance.getConfig().contains("DebugMessages")) debug = Main.instance.getConfig().getBoolean("DebugMessages");
    	if (Main.instance.getConfig().contains("LogMessages"))   logs  = Main.instance.getConfig().getBoolean("LogMessages");

	}
	
	/*
	 * Handler: Catch a slime in a bucket
	 * Variables:
	 *    @event : PlayerInteractEntityEvent - When player clicks on an Entity 
	 * Returns:
	 *    Nothing
	 */
	@EventHandler
	public void entityInteract(PlayerInteractEntityEvent event) {
		
		// Did the player right-click on a slime?
		if ((event != null) &&!event.isCancelled() && event.getRightClicked() != null && event.getRightClicked() instanceof Slime) {
	    	if (debug == true) System.out.println("[SlimeBucket] Entity Interact (right-click on slime) detected.");
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

					// Grab the name of the slime, if it's been renamed
					String slimeCName = null;

					if (slimeEntity.getCustomName() != null) {
						slimeCName = slimeEntity.getCustomName();
					}

					// Finally, time to swap the bucket for the Slime in a Bucket
					ItemStack bucketSlime = createSlimeBucket(slimeCName); 

					// How many buckets are in the hand?
					if (stack.getAmount() == 1) {          // One bucket - straight swap
						if (hand == EnumHand.MAIN_HAND)
							p.getInventory().setItemInMainHand(bucketSlime);
						else if (hand == EnumHand.OFF_HAND)
							p.getInventory().setItemInOffHand(bucketSlime);
					} else {                               // More than one bucket, reduce stack and try to drop in inventory
						// Reduce the stack of buckets by 1
						int b = stack.getAmount();
						stack.setAmount(b-1);
						
						// Throw the slimebucket into the players' inventory
						if (p.getInventory().addItem(bucketSlime) == null) {
							// If it failed, drop one on the floor
							p.getWorld().dropItemNaturally(p.getLocation(), bucketSlime);
						}
					}
					if (logs == true) System.out.println("[SlimeBucket] " + p.getDisplayName() + " caught a baby slime in a bucket.");

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
	 * Variables:
	 *    @event : PlayerInteractEvent - When player clicks on a block 
	 * Returns:
	 *    Nothing
	 */
	@EventHandler
	public void playerInteract(PlayerInteractEvent event) {
		
		// Did the player right-click on a block?
		if ((event != null) &&!event.isCancelled() && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			// With a slime in a bucket...?
			Player p = event.getPlayer();
			if (p != null) {
				EnumHand h = hasSlimeBucket(p);
			
				if (h != EnumHand.NO_HAND) {
					Block b = event.getClickedBlock();
					BlockFace f = event.getBlockFace();
					String customSName = null;
					ItemStack stack;

					if (debug == true) System.out.println("[SlimeBucket] Player Interact detected.");
					// Player has right-clicked a block with a slime-in-a-bucket
					// Remove the Slime-in-a-bucket
					stack = p.getInventory().getItemInMainHand();    // Assume Main Hand
					if (h == EnumHand.OFF_HAND) {
						stack = p.getInventory().getItemInOffHand();
					}
				
					if (!stack.getItemMeta().getDisplayName().equals("&a&lSlime in a Bucket")) {
						int nLen = stack.getItemMeta().getDisplayName().length();
						int nEnd = nLen - 12;
						if (debug == true) System.out.println("[SlimeBucket] Custom Name: '" + stack.getItemMeta().getDisplayName() + "' (length: " + (nLen) + ")");
						customSName = stack.getItemMeta().getDisplayName().substring(4, nEnd);
					}
					stack.setAmount(stack.getAmount()-1);

					//  and give player a bucket
					ItemStack bucketItem = new ItemStack(Material.BUCKET);
					if (p.getInventory().addItem(bucketItem) == null) {
						// If it failed, drop one on the floor
						p.getWorld().dropItemNaturally(p.getLocation(), bucketItem);
					}
				
					// and put a slime on the floor
					double blockOffset = 0.5;
					Location spawnLoc = b.getLocation();

					// Add block face direction, then half a block for block centre
					spawnLoc.add((double)f.getModX()+blockOffset, (double)f.getModY(), ((double)f.getModZ())+blockOffset); // Move to the side on which the block was clicked

					if (customSName != null) {
						// Spawn a size 1 slime with a custom name
						final String sn = customSName;
						p.getWorld().spawn(spawnLoc, Slime.class, new Consumer<Slime>() {
							public void accept(Slime slime) {
								slime.setSize(1); // make smallest slime before appearing
								slime.setCustomName(sn);
							}
						});
					} else {
						// Spawn a normal size 1 slime
						p.getWorld().spawn(spawnLoc, Slime.class, new Consumer<Slime>() {
							public void accept(Slime slime) {
								slime.setSize(1); // make smallest slime before appearing
							}
						});
					}
					if (logs == true) System.out.println("[SlimeBucket] " + p.getDisplayName() + " released a baby slime.");

				
					// We did stuff, cancel the right-click event
					event.setCancelled(true);
				}
			}
		}
	}

	/*
	 * Has the player moved into a new chunk.  If so, check if the bucket needs updating
	 * Variables:
	 *    @event : PlayerMoveEvent - When player moves 
	 * Returns:
	 *    Nothing
	 */
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
	    if ((event != null) && !event.isCancelled()) {
	        Chunk chunkFrom = event.getFrom().getChunk(); // initial chunk
	        Chunk chunkTo = event.getTo().getChunk();     // current chunk

	        if (chunkFrom.getX() != chunkTo.getX() || chunkFrom.getZ() != chunkTo.getZ()) {
	            // if chunk has changed
	            updateSlimeBuckets(event); // update any "buckets" in the players inventory
	        }
	    }
	}
	
	/*
	 * Called when player moves to a new chunk, to check if the slime bucket animation needs to change
	 * Variables:
	 *    @event : PlayerMoveEvent - When player moves to a new chunk 
	 * Returns:
	 *    Nothing
	 */
	public void updateSlimeBuckets(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		if (p != null) {
			// Does the player have a golden hoe in their inventory?
			PlayerInventory inv = p.getInventory();
			for (ItemStack item : inv.getContents()) {
				if (item != null) {
					//  Is this a slime-in-a-bucket?
					if (isSlimeBucket(item)) {
						boolean slime = e.getTo().getChunk().isSlimeChunk();                  // Has player moved into a slime chunk?
						boolean gareth = isGareth(item);
				    	int meta = getDamageValue(item);                                      // get damage value
						int newMeta = (gareth ? (slime ? 4 : 3) : (slime ? 2 : 1));           // New durability value based on whether in a slime chunk

						if (debug == true) {
							System.out.println("[SlimeBucket] Player " + (p.getName()) + " moved to a new chunk with a slime bucket.");
							System.out.println("[SlimeBucket] Slime chunk: " + (slime ? "True" : "False"));
						}
						if(meta != newMeta) {
							ItemMeta itMeta = item.getItemMeta();
							((Damageable) itMeta).setDamage(newMeta);             // cast to Damageable, then set damage
							item.setItemMeta(itMeta);
							
							if (debug == true) System.out.println("[SlimeBucket] Damage value updated!");
							if ((logs == true) && (slime == true)) System.out.println("[SlimeBucket] " + p.getDisplayName() + " found a Slime Chunk!");
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
		// Assume Main hand, and get item there
		EnumHand hand = EnumHand.MAIN_HAND;
		ItemStack stack = p.getInventory().getItemInMainHand();
		
		// If main hand is empty, assume off-hand and grab that item instead
		if(stack == null || stack.getType() != Material.GOLDEN_HOE) {
			stack = p.getInventory().getItemInOffHand();
			hand = EnumHand.OFF_HAND;
		}

		if(stack != null && stack.getType() == Material.GOLDEN_HOE) {
			// Player has a golden hoe, but it is the right type...?
			ItemMeta stackMeta = stack.getItemMeta();
	    	int d = getDamageValue(stack); // get damage value
			
			if (((d != 1) && (d != 2) && (d != 3) && (d != 4)) || (stackMeta.isUnbreakable() != true)) {
				hand = EnumHand.NO_HAND;  // This item isn't a valid slime-in-a-bucket
			}
		} else {
			hand = EnumHand.NO_HAND;      // Nothing in main hand or off-hand
		}
		return hand;
	}

    /*
     * Is this a Slime in a Bucket? (of any type)
     */
    private boolean isSlimeBucket(ItemStack item) {
    	boolean bRet = false;
    	int d = getDamageValue(item); // get damage value
    	if ( (item.getType() == Material.GOLDEN_HOE) && 
    		 ((d == 1) || (d == 2) || (d == 3) || (d == 4)) && 
			 (item.getItemMeta().isUnbreakable() == true)) {
			bRet = true;
		}
		return bRet;
    }

    /*
     * Is this a Gareth-type Slime in a Bucket?
     */
	private boolean isGareth(ItemStack item) {
    	boolean bRet = false;
    	int d = getDamageValue(item); // get damage value
    	if ( (item.getType() == Material.GOLDEN_HOE) && 
    		 ((d == 3) || (d == 4)) && 
			 (item.getItemMeta().isUnbreakable() == true)) {
			bRet = true;
		}
		return bRet;	
	}
	
	/*
	 * Create a new slime-in-a-bucket
	 */
	private ItemStack createSlimeBucket(String sName) {
		// Create an Unbreakable Golden Hoe, with damage 1 and Hide Flags 
		ItemStack bucketSlime = new ItemStack(Material.GOLDEN_HOE);
		ItemMeta sibMeta = bucketSlime.getItemMeta();
		ArrayList<String> lore = new ArrayList<String>();
		int bucketType = 1;  // Assume a standard slime-in-a-bucket

		if (sName == null) {
			sibMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a&lSlime in a Bucket"));
	        lore.add("This slime gets excited when in a slime chunk.");
		} else {
			sibMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a&l" + sName + " in a Bucket"));
			if (sName.equalsIgnoreCase("gareth") || sName.equalsIgnoreCase("garethpw")) {
				bucketType = 3;
		        lore.add("Gareth gets excited when in a slime chunk.");
			} else {
		        lore.add("This slime gets excited when in a slime chunk.");				
			}
		}
		
        sibMeta.setLore(lore);
        
        sibMeta.setUnbreakable(true);
        sibMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        sibMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        
		((Damageable) sibMeta).setDamage(bucketType);
        
        bucketSlime.setItemMeta(sibMeta);
		
		return bucketSlime;
	}
	
	private int getDamageValue(ItemStack itStack)
	{
		return ((Damageable) itStack.getItemMeta()).getDamage();
	}

	private void setDamageValue(ItemStack itStack, int iVal)
	{
		ItemMeta itMeta = itStack.getItemMeta();
		((Damageable) itMeta).setDamage(iVal);
		itStack.setItemMeta(itMeta);
	}
}
