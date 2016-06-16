package mods.belgabor.extratweaker.mods.vanilla;

import minetweaker.MineTweakerAPI;
import mods.belgabor.extratweaker.mods.vanilla.commands.VanillaCommandBlockstats;
import mods.belgabor.extratweaker.mods.vanilla.handlers.Durability;
import mods.belgabor.extratweaker.mods.vanilla.handlers.HarvestLevel;
import mods.belgabor.extratweaker.mods.vanilla.loggers.VanillaCommandLoggerItem;
import mods.belgabor.extratweaker.util.IModManager;
import net.minecraft.command.ServerCommandManager;

/**
 * Created by Belgabor on 15.06.2016.
 */
public class Vanilla implements IModManager {
    @Override
    public void initTweaks() {
        MineTweakerAPI.registerClass(Durability.class);
        MineTweakerAPI.registerClass(HarvestLevel.class);
    }

    @Override
    public void initAccessors() {
        
    }

    @Override
    public boolean accessorsAvailable() {
        return false;
    }

    @Override
    public void registerTweakLoggers() {
        VanillaCommandLoggerItem.register();
    }

    @Override
    public void registerCommands(ServerCommandManager manager) {
        manager.registerCommand(new VanillaCommandBlockstats());
    }
}
