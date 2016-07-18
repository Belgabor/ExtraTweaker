package mods.belgabor.extratweaker.mods.vanilla.commands;

import com.google.common.base.Joiner;
import mods.belgabor.extratweaker.ExtraTweaker;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by Belgabor on 03.06.2016.
 */
public class VanillaCommandLoadorder extends CommandBase {

    @Override
    public String getCommandName() {
        return "loadorder";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/loadorder - Dump mod load order";
    }


    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] arguments) throws CommandException {
        
        OutputStreamWriter writer = null;
        boolean errors = false;
        int total = 0;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(new File(ExtraTweaker.logsDir, "loadorder.csv")), "utf-8");
            writer.write("Order;ModID;Name;Version;Requirements;Dependencies\n");
        } catch (IOException e) {
            sender.addChatMessage(new TextComponentString("Warning: Unable to open log file."));
        }
        for (ModContainer mod : Loader.instance().getActiveModList()) {
            sender.addChatMessage(new TextComponentString(String.format("[%s] %s", mod.getModId(), mod.getName())));
            total++;
            if (writer != null) {
                try {
                    writer.write(String.format("%d;%s;\"%s\";\"%s\";\"%s\";\"%s\"\n",
                            total, mod.getModId(), mod.getName(), mod.getDisplayVersion(), Joiner.on(",").join(mod.getRequirements()), Joiner.on(",").join(mod.getDependencies())));
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
