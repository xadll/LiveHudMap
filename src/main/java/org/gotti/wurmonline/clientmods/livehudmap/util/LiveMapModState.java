package org.gotti.wurmonline.clientmods.livehudmap.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import org.gotti.wurmonline.clientmods.livehudmap.LiveHudMapMod;

public class LiveMapModState {
    
    private static final Logger LOG = LiveHudMapMod.LOG;
    
    private List<ServerItem> servers;
    private boolean hiResMap = false;
    private boolean showHiddenOre = false;
    private boolean showDeeds = true;
    
    private ServerItem selectedServer;

    public List<ServerItem> getServers() {
        return servers;
    }

    public boolean isHiResMap() {
        return hiResMap;
    }

    public boolean isShowHiddenOre() {
        return showHiddenOre;
    }

    public boolean isShowDeeds() {
        return showDeeds;
    }

    public void loadFromProperties(Properties p) {
        loadProps(p);
        loadServerItems(p);
    }
    
    public ServerItem getSelectedServer() {
        return selectedServer;
    }
    
    public void changeServer() {
        if (servers == null || servers.isEmpty() || selectedServer == null) {
            return;
        }
        int lastIndex = selectedServer.index;
        if (lastIndex+1 >= servers.size()) {
            lastIndex = 0;
            selectedServer = servers.get(0);
        } else {
            selectedServer = servers.get(++lastIndex);
        }
    }
    
    private void loadProps(Properties p) {
        hiResMap = Boolean.parseBoolean(p.getProperty("hiResMap", String.valueOf(hiResMap)));
        LOG.info(String.format("hiResMap: %s", hiResMap));
        
        showHiddenOre = Boolean.parseBoolean(p.getProperty("showHiddenOre", String.valueOf(showHiddenOre)));
        LOG.info(String.format("showHiddenOre: %s", showHiddenOre));
        
        showDeeds = Boolean.parseBoolean(p.getProperty("showDeeds", String.valueOf(showDeeds ) ) );
        LOG.info(String.format("showDeeds: %s", showDeeds));
    }
    
    private void loadServerItems(Properties p) {
        servers = new ArrayList<>(1);
        int i = 0;

        while (true) {
            String propertyServerName = String.format("server.%d.name", i);
            String propertyServerSize = String.format("server.%d.size", i);
            String propertyDeedsJsonUrl = String.format("server.%d.deeds_json_url", i);
            String propertyServerDefault = String.format("server.%d.default", i);

            String name = p.getProperty(propertyServerName);
            int size = Integer.parseInt(p.getProperty(propertyServerSize, "0"));
            String url = p.getProperty(propertyDeedsJsonUrl);
            boolean isDefault = Boolean.parseBoolean(p.getProperty(propertyServerDefault));

            if (name == null || size == 0 || url == null) {
                break;
            }
            
            ServerItem server = new ServerItem(i, name, size, url, isDefault);
            
            if (server.isDefault) {
                selectedServer = server;
            }
            
            servers.add(i, server);
            
            LOG.info(server.toString());
            
            i++;
        }
    }
}