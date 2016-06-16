package mods.belgabor.extratweaker.mods.vanilla.loggers;

import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IItemStack;
import minetweaker.api.player.IPlayer;
import minetweaker.api.server.ICommandFunction;
import mods.belgabor.extratweaker.util.CommandLoggerBase;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.*;

import java.util.Set;

import static mods.belgabor.extratweaker.helpers.InputHelper.isABlock;
import static mods.belgabor.extratweaker.helpers.InputHelper.toStack;

/**
 * Created by Belgabor on 03.06.2016.
 */
public class VanillaCommandLoggerItem extends CommandLoggerBase implements ICommandFunction {
    public static void register() {
        MineTweakerAPI.server.addMineTweakerCommand("handextra", new String[] {
                "/minetweaker handextra",
                "    dump extra information about the held item"
        }, new VanillaCommandLoggerItem());
    }
    
    @Override
    public void execute(String[] arguments, IPlayer player) {
        IItemStack item = player.getCurrentItem();
        if (item != null) {
            ItemStack itemStack = toStack(item); 
            Item theItem = itemStack.getItem();
            logBoth(player, "Item: " + item.toString());
            String s;
            if (theItem.getHasSubtypes()) {
                s = "None (subtypes)";
            } else if (!theItem.isDamageable()) {
                s = "None (not damagable)";
            } else {
                s = Integer.toString(theItem.getMaxDamage());
            }
            logBoth(player, "Durability (max damage): " + s);
            logBoth(player, "Tool harvest levels:");
            if (isABlock(itemStack)) {
                Block block = ((ItemBlock) theItem).getBlock();
                for (IBlockState state : block.getBlockState().getValidStates()) {
                    String meta = "?";
                    try {
                        meta = Integer.toString(block.getMetaFromState(state));
                    } catch (IllegalArgumentException e) {}
                    logBoth(player, String.format("  %s: %s - %d", meta, block.getHarvestTool(state), block.getHarvestLevel(state)));
                }
            } else {
                Set<String> classes = theItem.getToolClasses(itemStack);
                if (classes.size() > 0) {
                    for (String cl : classes) {
                        logBoth(player, String.format("  %s: %d", cl, theItem.getHarvestLevel(itemStack, cl)));
                    }
                } else {
                    logBoth(player, "  None");
                }
            }
            if (theItem instanceof ItemPickaxe || theItem instanceof ItemSpade || theItem instanceof ItemAxe) {
                logBoth(player, "The item class extends one of the primary vanilla tool classes. It is therefore likely locked to its primary function.");
            }
        }
    }
    
}
