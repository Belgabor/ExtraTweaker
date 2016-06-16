package mods.belgabor.extratweaker.mods.vanilla.commands;

import com.google.common.base.Joiner;
import mods.belgabor.extratweaker.ExtraTweaker;
import mods.belgabor.extratweaker.util.CommandLoggerBase;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.oredict.OreDictionary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

/**
 * Created by Belgabor on 03.06.2016.
 */
public class VanillaCommandBlockstats extends CommandBase {
    @Override
    public String getCommandName() {
        return "blockstats";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/blockstats [range] - Dump block statistics around player in a certain range";
    }
    
    private static class BlockStats {
        public final Block block;
        public final int meta;
        public int count = 0;
        public int minLevel = Integer.MAX_VALUE;
        public int maxLevel = 0;

        private BlockStats(Block block, int meta) {
            this.block = block;
            this.meta = meta;
        }
        
        protected void minmax(int y) {
            minLevel = Math.min(minLevel, y);
            maxLevel = Math.max(maxLevel, y);
        }
        
        protected String getTag() {
            try {
                ItemStack stack = new ItemStack(block, 1, meta);
                if (ExtraTweaker.mtAvailable)
                    return CommandLoggerBase.getObjectDeclaration(new ItemStack(block, 1, meta));
                else 
                    return stack.getUnlocalizedName() + "#" + meta;
            } catch (NullPointerException e) {
                return block.getUnlocalizedName() + "#" + meta;
            }
        }
        
        protected String getName() {
            try {
                return (new ItemStack(block, 1, meta)).getDisplayName();
            } catch (NullPointerException e) {
                return block.getLocalizedName();
            }
        }
        
        protected String getOres() {
            try {
                int[] ids = OreDictionary.getOreIDs(new ItemStack(block, 1, meta));
                if (ids.length ==0)
                    return "";
                ArrayList<String> ores = new ArrayList<>();
                for (int id : ids) {
                    ores.add(OreDictionary.getOreName(id));
                }
                return Joiner.on(", ").join(ores);
            } catch (NullPointerException|IllegalArgumentException e) {
                return "";
            }
        }
    }
    
    private static class BlockStatsComp implements Comparator<BlockStats> {

        @Override
        public int compare(BlockStats o1, BlockStats o2) {
            return o1.count - o2.count;
        }
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] arguments) throws CommandException {
        if (arguments.length != 1) {
            sender.addChatMessage(new TextComponentString("Wrong number of parameters."));
            return;
        }
        int range;
        try {
            range = Integer.parseInt(arguments[0]);
        } catch (NumberFormatException e) {
            range = -1;
        }
        if (range <= 0) {
            sender.addChatMessage(new TextComponentString("Range must be a positive number."));
            return;
        }
        sender.addChatMessage(new TextComponentString("Starting to collect block statistics, radius " + range));
        World world = sender.getEntityWorld();
        BlockPos center = sender.getPosition();
        BlockPos from = new BlockPos(center.getX() - range, 0, center.getZ() - range);
        BlockPos to = new BlockPos(center.getX() + range, world.getActualHeight() - 1, center.getZ() + range);
        
        Map<String, BlockStats> stats = new HashMap<>();
        int total = 0;
        
        for (int x = from.getX(); x <= to.getX(); x++) {
            for (int z = from.getZ(); z <= to.getZ(); z++) {
                Chunk chunk = world.getChunkFromBlockCoords(new BlockPos(x, 0, z));
                int chunkX = x & 15;
                int chunkZ = z & 15;
                for (int y = from.getY(); y <= to.getY(); y++) {
                    IBlockState state = chunk.getBlockState(chunkX, y, chunkZ); 
                    Block block = state.getBlock();
                    int meta = block.getMetaFromState(state);
                    String tag = block.getUnlocalizedName() + "#" + meta;
                    
                    BlockStats s = stats.get(tag);
                    if (s == null) {
                        s = new BlockStats(block, meta);
                        stats.put(tag, s);
                    }
                    
                    s.count++;
                    total++;
                    s.minmax(y);
                }
            }
        }

        TreeSet<BlockStats> sorted = new TreeSet<>(new BlockStatsComp());
        sorted.addAll(stats.values());
        OutputStreamWriter writer = null;
        boolean errors = false;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(new File(ExtraTweaker.logsDir, String.format("blockstats_%d_%d_%d_%d.csv", world.provider.getDimension(), center.getX(), center.getZ(), range))), "utf-8");
            writer.write("Block;Unlocalized Name;Name;Meta;OreDict;count;total;Min Level;Max Level;Dimension ID;Dimension Name\n");
        } catch (IOException e) {
            sender.addChatMessage(new TextComponentString("Warning: Unable to open log file."));
        }
        for (BlockStats bl : sorted) {
            sender.addChatMessage(new TextComponentString(String.format("%s  %d  %d-%d", bl.getTag(), bl.count, bl.minLevel, bl.maxLevel)));
            if (writer != null) {
                try {
                    writer.write(String.format("%s;%s;\"%s\";%d;%s;%d;%d;%d;%d;%d;\"%s\"\n", 
                            bl.getTag(), bl.block.getUnlocalizedName(), bl.getName(), bl.meta, bl.getOres(), bl.count, total, bl.minLevel, bl.maxLevel, world.provider.getDimension(), world.provider.getDimensionType().getName()));
                    writer.flush();
                } catch (IOException e) {
                    errors = true;
                }
            }
        }
        sender.addChatMessage(new TextComponentString("Total: " + total));
        if (errors)
            sender.addChatMessage(new TextComponentString("There were errors writing the log file, it may be incomplete."));
    }
}
