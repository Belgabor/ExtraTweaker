package mods.belgabor.extratweaker.mods.mt;

import minetweaker.MineTweakerImplementationAPI;
import minetweaker.util.IEventHandler;
import mods.belgabor.extratweaker.ExtraTweaker;
import mods.belgabor.extratweaker.util.IModManager;
import net.minecraft.command.ServerCommandManager;

/**
 * Created by Belgabor on 16.06.2016.
 */
public class MT implements IModManager, IEventHandler<MineTweakerImplementationAPI.ReloadEvent> {
    
    public MT() {
        MineTweakerImplementationAPI.onReloadEvent(this);
    }
    
    @Override
    public void initTweaks() {
        
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

    }

    @Override
    public void registerCommands(ServerCommandManager manager) {

    }

    @Override
    public void handle(MineTweakerImplementationAPI.ReloadEvent reloadEvent) {
        ExtraTweaker.registerLoggers();
    }
}
