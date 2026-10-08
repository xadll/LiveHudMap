package org.gotti.wurmonline.clientmods.livehudmap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.gotti.wurmonline.clientmods.livehudmap.renderer.MapRendererCave;
import org.gotti.wurmonline.clientmods.livehudmap.renderer.RenderType;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.classhooks.InvocationHandlerFactory;
import org.gotti.wurmunlimited.modloader.interfaces.Configurable;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.WurmClientMod;

import com.wurmonline.client.renderer.gui.HeadsUpDisplay;
import com.wurmonline.client.renderer.gui.LiveMapWindow;
import com.wurmonline.client.renderer.gui.MainMenu;
import com.wurmonline.client.renderer.gui.WurmComponent;
import com.wurmonline.client.settings.SavePosManager;
import org.gotti.wurmonline.clientmods.livehudmap.util.LiveMapModState;
import org.gotti.wurmunlimited.modloader.interfaces.PreInitable;
import org.gotti.wurmunlimited.modsupport.console.ConsoleListener;
import org.gotti.wurmunlimited.modsupport.console.ModConsole;
import com.wurmonline.client.game.World;

public class LiveHudMapMod implements WurmClientMod, PreInitable, Initable, Configurable, ConsoleListener {

    public static final Logger LOG = Logger.getLogger(LiveHudMapMod.class.getName());

    public final LiveMapModState state = new LiveMapModState();

    private LiveMapWindow liveMapWindow;

    public static int serverSize = 0;

    @Override
    public void configure(Properties properties) {
        state.loadFromProperties(properties);

        RenderType.highRes = state.isHiResMap();
        MapRendererCave.showHiddenOre = state.isShowHiddenOre();
        serverSize = state.getSelectedServer().serverSize;
    }
    
    @Override
    public boolean handleInput(String string, Boolean silent) {
        if (string != null && string.startsWith("toggle livemap") && liveMapWindow != null) {
            liveMapWindow.toggle();
            return true;
        }
        return false;
    }

    @Override
    public void preInit() {
    }
    
    @Override
    public void init() {
        // com.wurmonline.client.renderer.gui.HeadsUpDisplay.init(int, int)
        HookManager.getInstance().registerHook(
            "com.wurmonline.client.renderer.gui.HeadsUpDisplay",
            "init",
            "(II)V",
            new InvocationHandlerFactory() {
                @Override
                public InvocationHandler createInvocationHandler() {
                    return new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            method.invoke(proxy, args);
                            initLiveMap((HeadsUpDisplay) proxy);
                            return null;
                        }
                    };
                }
            }
        );
        ModConsole.addConsoleListener(this);
    }

    private void initLiveMap(HeadsUpDisplay hud) {
        if (state.getSelectedServer() == null) {
            return;
        }
        new Runnable() {
            @Override
            public void run() {
                try {
                    World world = ReflectionUtil.getPrivateField(hud, ReflectionUtil.getField(hud.getClass(), "world"));
                    liveMapWindow = new LiveMapWindow(world, state);
                    
                    MainMenu mainMenu = ReflectionUtil.getPrivateField(hud, ReflectionUtil.getField(hud.getClass(), "mainMenu"));
                    mainMenu.registerComponent("Live map", liveMapWindow);
                    
                    List<WurmComponent> components = ReflectionUtil.getPrivateField(hud, ReflectionUtil.getField(hud.getClass(), "components"));
                    components.add(liveMapWindow);

                    SavePosManager savePosManager = ReflectionUtil.getPrivateField(hud, ReflectionUtil.getField(hud.getClass(), "savePosManager"));
                    savePosManager.registerAndRefresh(liveMapWindow, "livemapwindow");
                }
                catch (IllegalArgumentException | IllegalAccessException | ClassCastException | NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            }

        }.run();
    }
}