package com.wurmonline.client.renderer.gui;

import java.util.Locale;
import org.gotti.wurmonline.clientmods.livehudmap.DeedData;
import org.gotti.wurmonline.clientmods.livehudmap.LiveMap;
import org.gotti.wurmonline.clientmods.livehudmap.MapLayer;
import org.gotti.wurmonline.clientmods.livehudmap.renderer.RenderType;

import com.wurmonline.client.game.World;
import com.wurmonline.client.options.Options;
import com.wurmonline.client.renderer.PickData;
import com.wurmonline.client.resources.textures.ImageTexture;
import com.wurmonline.client.resources.textures.ImageTextureLoader;
import com.wurmonline.client.resources.textures.ResourceTexture;
import com.wurmonline.client.resources.textures.ResourceTextureLoader;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.gotti.wurmonline.clientmods.livehudmap.LiveHudMapMod;
import org.gotti.wurmonline.clientmods.livehudmap.util.LiveMapModState;
import org.gotti.wurmonline.clientmods.livehudmap.util.ServerItem;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

public class LiveMapWindow extends WWindow {

    private static final Logger LOG = LiveHudMapMod.LOG;
    
    private final WurmBorderPanel mainPanel;
    private final LiveMap liveMap;
    private final BufferedImage iconImage;
    private final LiveMapView liveMapView;
    private final DeedData deedData = new DeedData();
    private final LiveMapModState state;

    public LiveMapWindow(World world, LiveMapModState state) {
        super("Live map", true);
        this.state = state;

        setTitle(String.format("Live map: %s", state.getSelectedServer().serverName));
        
        deedData.initMap(state.getMaxServerSize());
        deedData.setShowDeeds(state.isShowDeeds());
        deedData.setJsonPath(state.getSelectedServer().deedsJsonUrl);
        deedData.refreshMap();
        
        mainPanel = new WurmBorderPanel("Live map");

        this.liveMap = new LiveMap(world, 256);
        resizable = false;

        iconImage = loadIconImage();

        WurmArrayPanel<WButton> buttons = new WurmArrayPanel<>("Live map buttons", WurmArrayPanel.DIR_VERTICAL);
        buttons.setInitialSize(32, 256, false);

        buttons.addComponent(createButton("+", "Zoom in", 0, new ButtonClickListener() {
                @Override
                public void buttonClicked(WButton p0) {
                    liveMap.zoomIn();
                }
        }));

        buttons.addComponent(createButton("-", "Zoom out", 1, new ButtonClickListener() {
                @Override
                public void buttonClicked(WButton p0) {
                    liveMap.zoomOut();
                }
        }));

        buttons.addComponent(createButton("Flat", "Flat view", 2, new ButtonClickListener() {
                @Override
                public void buttonClicked(WButton p0) {
                    liveMap.setRenderer(MapLayer.SURFACE, RenderType.FLAT);
                }
        }));

        buttons.addComponent(createButton("3D", "Pseudo 3D view", 3, new ButtonClickListener() {
                @Override
                public void buttonClicked(WButton p0) {
                    liveMap.setRenderer(MapLayer.SURFACE, RenderType.ISOMETRIC);
                }
        }));

        buttons.addComponent(createButton("Topo", "Topographic view", 4, new ButtonClickListener() {
                @Override
                public void buttonClicked(WButton p0) {
                    liveMap.setRenderer(MapLayer.SURFACE, RenderType.TOPOGRAPHIC);
                }
        }));

        buttons.addComponent(createButton("Deed", "Toggle Deeds", 5, new ButtonClickListener() {
                @Override
                public void buttonClicked(WButton p0) {
                    deedData.setShowDeeds(!deedData.getShowDeed());
                    liveMap.deedChanged();
                    liveMap.update(x, y);
                }
        }));

        buttons.addComponent(createButton("SER", "Change Server", 6, new ButtonClickListener() {
                @Override
                public void buttonClicked(WButton p0) {
                    new Runnable() {
                        @Override
                        public void run() {
                            state.changeServer();
                            ServerItem server = state.getSelectedServer();
                            setTitle(String.format("Live map: %s", server.serverName));
                            LiveHudMapMod.serverSize = server.serverSize;
                            deedData.setJsonPath(server.deedsJsonUrl);
                            deedData.refreshMap();
                            liveMap.deedChanged();
                            liveMap.update(x, y);
                        }
                    }.run();
                }
        }));

        buttons.addComponent(createButton( "", "", 7, new ButtonClickListener() {
                @Override
                public void buttonClicked(WButton p0) {
                }
        }));

        liveMapView = new LiveMapView("Live map", liveMap, 256, 256); //TODO 256, 256

        mainPanel.setComponent(liveMapView, WurmBorderPanel.WEST);
        mainPanel.setComponent(buttons, WurmBorderPanel.EAST);

        setComponent(mainPanel);
        setInitialSize(256 + 6 + 32, 256 + 25, false);
        layout();
        sizeFlags = FlexComponent.FIXED_WIDTH | FlexComponent.FIXED_HEIGHT;
    }

    public void closePressed() {
        hud.toggleComponent(this);
    }

    public void toggle() {
        hud.toggleComponent(this);
    }

    public void pick(final PickData pickData, final int xMouse, final int yMouse) {
        if (this.liveMapView.contains(xMouse, yMouse)) {
            this.liveMap.pick(pickData, 1.0f * (xMouse - this.liveMapView.x) / this.liveMapView.width, 1.0f * (yMouse - this.liveMapView.y) / this.liveMapView.width);
        }
    }

    public HeadsUpDisplay getHud() {
        return hud;
    }

    private BufferedImage loadIconImage() {
        try {
            URL url = this.getClass().getClassLoader().getResource("livemapicons.png");
            if (url == null && this.getClass().getClassLoader() == HookManager.getInstance().getLoader()) {
                url = HookManager.getInstance().getClassPool().find(LiveMapWindow.class.getName());
                if (url != null) {
                    String path = url.toString();
                    int pos = path.lastIndexOf('!');
                    if (pos != -1) {
                        path = path.substring(0, pos) + "!/livemapicons.png";
                    }
                    url = new URL(path);
                }
            }
            if (url != null) {
                return ImageIO.read(url);
            } else {
                return null;
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
            return null;
        }
    }

    private WButton createButton(String label, String tooltip, int textureIndex, ButtonListener listener) {	
        if (iconImage != null) {
            BufferedImage image = iconImage.getSubimage(textureIndex * 32, 0, 32, 32);
            ImageTexture texture = ImageTextureLoader.loadNowrapNearestTexture(image, false);
            return new LiveMapButton("", tooltip, 32, 32, texture, listener);
        } else {
            String themeName = Options.guiSkins.options[Options.guiSkins.value()].toLowerCase(Locale.ENGLISH).replace(" ", "");
            ResourceTexture backgroundTexture = ResourceTextureLoader.getTexture("img.gui.button.mainmenu." + themeName);
            return new WTextureButton(label, tooltip, backgroundTexture, listener);
        }
    }
}