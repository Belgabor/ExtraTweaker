package mods.belgabor.extratweaker.mods.vanilla.handlers;

import minetweaker.IUndoableAction;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IItemStack;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.HashMap;
import java.util.Map;

import static mods.belgabor.extratweaker.helpers.InputHelper.isABlock;
import static mods.belgabor.extratweaker.helpers.InputHelper.toStack;

/**
 * Created by Belgabor on 03.06.2016.
 */

@ZenClass("mods.vanilla.HarvestLevel")
public class HarvestLevel {
 
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Change harvest level
    
    @ZenMethod
    public static void set(IItemStack item, String tool, int level) {
        if (item == null) {
            MineTweakerAPI.getLogger().logError("Harvest level: Block/Item must not be null!");
            return;
        }
        if (isABlock(toStack(item))) {
            MineTweakerAPI.apply(new HarvestLevelChangeBlock(item, tool, level));
        } else {
            if (tool == null) {
                MineTweakerAPI.getLogger().logError("Harvest level: For items tool must not be null!");
                return;
            }
            MineTweakerAPI.apply(new HarvestLevelChangeItem(item, tool, level));
        }
    }


    private static class HarvestLevelChangeBlock implements IUndoableAction {
        private final ItemStack item;
        private final Block block;
        private final String tool;
        private final Integer level;
        //private final int meta;
        private final boolean changeAll;
        private final IBlockState state;
        //private final String[] tools = new String[16];
        //private final Integer[] levels = new Integer[16];
        private final Map<IBlockState, String> tools = new HashMap<>();
        private final Map<IBlockState, Integer> levels = new HashMap<>();
        private boolean applied = false;
        
        public HarvestLevelChangeBlock(IItemStack block, String tool, Integer level) {
            this.item = toStack(block, true);
            this.block = Block.getBlockFromItem(this.item.getItem());
            this.tool = tool;
            this.level = level;
            //this.meta = this.item.getItemDamage();
            this.state = this.block.getStateFromMeta(this.item.getItemDamage());
            this.changeAll = this.item.getItemDamage() == OreDictionary.WILDCARD_VALUE;
            
            for (IBlockState state : this.block.getBlockState().getValidStates()) {
                tools.put(state, this.block.getHarvestTool(state));
                levels.put(state, this.block.getHarvestLevel(state));
            }
            /*
            for(int i=0; i<16; i++) {
                tools[i] = this.block.getHarvestTool(this.block.getStateFromMeta(i));
                levels[i] = this.block.getHarvestLevel(this.block.getStateFromMeta(i));
            }
            */
        }
        
        @Override
        public void apply() {
            if (!applied) {
                if (changeAll) {
                    this.block.setHarvestLevel(tool, level);
                } else {
                    this.block.setHarvestLevel(tool, level, state);
                }
                applied = true;
            }
        }

        @Override
        public boolean canUndo() {
            return true;
        }

        @Override
        public void undo() {
            if (applied) {
                if (changeAll) {
                    for (IBlockState s : this.block.getBlockState().getValidStates()) {
                        this.block.setHarvestLevel(tools.get(s), levels.get(s), s);
                    }
                    /*
                    for(int i=0; i<16; i++) {
                        this.block.setHarvestLevel(tools[i], levels[i], this.block.getStateFromMeta(i));
                    }
                    */
                } else {
                    this.block.setHarvestLevel(tools.get(state), levels.get(state), state);
                    //this.block.setHarvestLevel(tools[meta], levels[meta], this.block.getStateFromMeta(meta));
                }
                applied = false;
            }
        }
        
        private String getBlockDescription() {
            return this.block.getUnlocalizedName() + ":" + (changeAll?"*":Integer.toString(item.getItemDamage()));
        }

        @Override
        public String describe() {
            return String.format("Setting harvest level for block %s: %s, %d", getBlockDescription(), tool==null?"null":tool, level);
        }

        @Override
        public String describeUndo() {
            return "Restoring harvest level of block " + getBlockDescription();
        }

        @Override
        public Object getOverrideKey() {
            return null;
        }
    }

    private static class HarvestLevelChangeItem implements IUndoableAction {
        private final ItemStack item;
        private final Item theItem;
        private final String tool;
        private final Integer level;
        private final Integer currentLevel;
        private boolean applied = false;

        public HarvestLevelChangeItem(IItemStack item, String tool, Integer level) {
            this.item = toStack(item, true);
            this.theItem = this.item.getItem();
            this.tool = tool;
            this.level = level;
            
            currentLevel = theItem.getHarvestLevel(this.item, tool, null, null);
        }

        @Override
        public void apply() {
            if (!applied) {
                theItem.setHarvestLevel(tool, level);
                applied = true;
            }
        }

        @Override
        public boolean canUndo() {
            return true;
        }

        @Override
        public void undo() {
            if (applied) {
                theItem.setHarvestLevel(tool, currentLevel);
                applied = false;
            }
        }

        @Override
        public String describe() {
            return String.format("Setting harvest level for item %s: %s, %d", item.getDisplayName(), tool, level);
        }

        @Override
        public String describeUndo() {
            return String.format("Restoring harvest level for item %s: %s, %d", item.getDisplayName(), tool, currentLevel);
        }

        @Override
        public Object getOverrideKey() {
            return null;
        }
    }
}

