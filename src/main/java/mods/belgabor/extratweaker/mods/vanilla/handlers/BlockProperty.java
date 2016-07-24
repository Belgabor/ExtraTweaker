package mods.belgabor.extratweaker.mods.vanilla.handlers;

import minetweaker.IUndoableAction;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IItemStack;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.HashMap;
import java.util.Map;

import static mods.belgabor.extratweaker.helpers.InputHelper.isABlock;
import static mods.belgabor.extratweaker.helpers.InputHelper.toStack;

/**
 * Created by Belgabor on 24.07.2016.
 */
@ZenClass("mods.vanilla.BlockProperty")
public class BlockProperty {

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Change harvest level

    @ZenMethod
    public static void set(IItemStack item, Float hardness, @Optional Float resistance, @Optional Integer lightLevel, @Optional Integer lightOpacity) {
        apply(item, hardness, resistance, lightLevel, lightOpacity);
    }

    @ZenMethod
    public static void resistance(IItemStack item, Float resistance) {
        apply(item, null, resistance, null, null);
    }

    @ZenMethod
    public static void lightLevel(IItemStack item, Integer lightLevel, @Optional Integer lightOpacity) {
        apply(item, null, null, lightLevel, lightOpacity);
    }

    @ZenMethod
    public static void lightOpacity(IItemStack item, Integer lightOpacity) {
        apply(item, null, null, null, lightOpacity);
    }

    protected static void apply(IItemStack item, Float hardness, Float resistance, Integer lightLevel, Integer lightOpacity) {
        if (!validate(item))
            return;
        if (lightLevel != null && ((lightLevel <0) || (lightLevel>15))) {
            MineTweakerAPI.getLogger().logError("Block property: Light level must be between 0.0 and 15.0");
            return;
        }
        if (lightOpacity != null && ((lightOpacity <0) || (lightOpacity>255))) {
            MineTweakerAPI.getLogger().logError("Block property: Light level must be between 0.0 and 255.0");
            return;
        }
        MineTweakerAPI.apply(new BlockPropertyChange(item, hardness, resistance, lightLevel, lightOpacity));
    } 
    
    protected static boolean validate(IItemStack item) {
        if (item == null) {
            MineTweakerAPI.getLogger().logError("Block property: Block must not be null!");
            return false;
        }
        if (!isABlock(item)) {
            MineTweakerAPI.getLogger().logError("Block property: Only blocks allowed!");
            return false;
        }
        return true;
    }


    private static class BlockPropertyChange implements IUndoableAction {
        private final ItemStack item;
        private final Block block;
        private final Float hardness;
        private final Float resistance;
        private final Integer lightLevel;
        private final Integer lightOpacity;
        private final boolean changeAll;
        private final IBlockState state;
        private final Map<IBlockState, Float> hardnesses = new HashMap<>();
        private float backResistance;
        private final Map<IBlockState, Integer> lightLevels = new HashMap<>();
        private final Map<IBlockState, Integer> lightOpacities = new HashMap<>();
        private boolean applied = false;
        private boolean undoable = true;

        public BlockPropertyChange(IItemStack block, Float hardness, Float resistance, Integer lightLevel, Integer lightOpacity) {
            this.item = toStack(block, true);
            this.block = Block.getBlockFromItem(this.item.getItem());
            this.hardness = hardness;
            this.resistance = resistance;
            this.lightLevel = lightLevel;
            this.lightOpacity = lightOpacity;
            this.state = this.block.getStateFromMeta(this.item.getItemDamage());
            this.changeAll = this.item.getItemDamage() == OreDictionary.WILDCARD_VALUE;

            for (IBlockState state : this.block.getBlockState().getValidStates()) {
                if (hardness != null) {
                    try {
                        hardnesses.put(state, this.block.getBlockHardness(state, null, null));
                    } catch (Throwable e) {
                        if (undoable)
                            MineTweakerAPI.getLogger().logError("Block property: Failed to query hardness, action cannot be undone!");
                        undoable = false;
                    }
                }
                if (lightLevel != null) {
                    lightLevels.put(state, this.block.getLightValue(state));
                }
                if (lightOpacity != null) {
                    lightOpacities.put(state, this.block.getLightOpacity(state));
                }
            }
            if (hardness != null || resistance != null) {
                try {
                    this.backResistance = this.block.getExplosionResistance(null) * 5.0f;
                } catch (Throwable e) {
                    if (undoable)
                        MineTweakerAPI.getLogger().logError("Block property: Failed to query resistance, action cannot be undone!");
                    undoable = false;
                }
            }
        }

        @Override
        public void apply() {
            if (!applied) {
                if (hardness != null)
                    this.block.setHardness(hardness);
                if (resistance != null)
                    this.block.setResistance(resistance / 3.0f);
                if (lightLevel != null)
                    this.block.setLightLevel(((float)lightLevel) / 15.0f);
                if (lightOpacity != null)
                    this.block.setLightOpacity(lightOpacity);
                /*
                if (changeAll) {
                } else {
                    this.block.setHarvestLevel(tool, level, state);
                }
                */
                applied = true;
            }
        }

        @Override
        public boolean canUndo() {
            return undoable;
        }

        @Override
        public void undo() {
            if (applied && undoable) {
                if (hardness != null)
                    this.block.setHardness(hardnesses.get(state));
                if (hardness != null || resistance != null)
                    this.block.setResistance(backResistance / 3.0f);
                if (lightLevel != null)
                    this.block.setLightLevel(((float) lightLevels.get(this.block.getDefaultState())) / 15.0f);
                if (lightOpacity != null)
                    this.block.setLightOpacity(lightOpacities.get(this.block.getDefaultState()));
                /*
                if (changeAll) {
                    for (IBlockState s : this.block.getBlockState().getValidStates()) {
                        this.block.setHarvestLevel(tools.get(s), levels.get(s), s);
                    }
                } else {
                    this.block.setHarvestLevel(tools.get(state), levels.get(state), state);
                }
                */
                applied = false;
            }
        }

        private String getBlockDescription() {
            return this.block.getUnlocalizedName() + ":" + (changeAll?"*":Integer.toString(item.getItemDamage()));
        }

        @Override
        public String describe() {
            return String.format("Setting properties for block %s", getBlockDescription());
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

}
