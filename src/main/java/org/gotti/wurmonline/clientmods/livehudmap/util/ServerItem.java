package org.gotti.wurmonline.clientmods.livehudmap.util;

public class ServerItem {

    public final int index;
    public final String serverName;
    public final int serverSize;
    public final String deedsJsonUrl;
    public final boolean isDefault;
 
    public ServerItem(
            int index,
            String serverName,
            int serverSize,
            String deedsJsonUrl,
            boolean isDefault)
    {
        this.index = index;
        this.serverName = serverName;
        this.serverSize = serverSize;
        this.deedsJsonUrl = deedsJsonUrl;
        this.isDefault = isDefault;
    }

    @Override
    public String toString() {
        String def = (isDefault) ? " (default)" : "";
        return String.format("Server-%d: %s, %d, %s%s",
                    index, serverName, serverSize, deedsJsonUrl, def);
    }
    
}
