package mods.belgabor.extratweaker;

import minetweaker.MineTweakerImplementationAPI;
import minetweaker.util.IEventHandler;
import mods.belgabor.extratweaker.util.IModManager;
import mods.belgabor.extratweaker.mods.vanilla.Vanilla;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mod(modid = ExtraTweaker.MOD_ID, version = ExtraTweaker.MOD_VERSION, name = ExtraTweaker.MOD_NAME)
@Optional.InterfaceList({ @Optional.Interface(iface = "minetweaker.util.IEventHandler", modid = "MineTweaker3"), })
public class ExtraTweaker implements IEventHandler<MineTweakerImplementationAPI.ReloadEvent> {
    public static final String MOD_ID = "extratweaker";
    public static final String MOD_VERSION = "0.1";
    public static final String MOD_NAME = "ExtraTweaker";

    public static Logger logger;
    public static File logsDir;
    public static boolean mtAvailable = false;
    
    private static final List<IModManager> managers = new ArrayList<>();
    
    public static void log(Level level, String message, Object ... args) {
        logger.log(level, String.format(message, args));
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        logsDir = new File("logs/");
        mtAvailable = Loader.isModLoaded("MineTweaker3");
        
        managers.add(new Vanilla());
    }


    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        if (mtAvailable)
            managers.stream().forEachOrdered(m -> m.initTweaks());
    }
    
    @EventHandler
    public void onServerStart(FMLServerStartingEvent event) {
        registerCommands(event.getServer());
        if (mtAvailable) {
            registerLoggers();
            MineTweakerImplementationAPI.onReloadEvent(this);
        }
    }

    private void registerLoggers() {
        managers.stream().forEachOrdered(m -> m.registerTweakLoggers());
    }

    private void registerCommands(MinecraftServer server) {
        ServerCommandManager manager = (ServerCommandManager) server.getCommandManager();
        managers.stream().forEachOrdered(m -> m.registerCommands(manager));
    }

    @Override
    public void handle(MineTweakerImplementationAPI.ReloadEvent reloadEvent) {
        registerLoggers();
    }
}
