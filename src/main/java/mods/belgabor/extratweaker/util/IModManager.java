package mods.belgabor.extratweaker.util;

import net.minecraft.command.ServerCommandManager;

/**
 * Created by Belgabor on 15.06.2016.
 */
public interface IModManager {
    public void initTweaks();
    public void initAccessors();
    public boolean accessorsAvailable();
    public void registerTweakLoggers();
    public void registerCommands(ServerCommandManager manager);
}
