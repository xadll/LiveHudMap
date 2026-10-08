package org.gotti.wurmonline.clientmods.livehudmap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.Iterator;
import org.json.simple.JSONArray;

public class DeedData {
    
    private static final Logger LOG = LiveHudMapMod.LOG;
    
    private static String jsonPath = "";
    public static byte[][] map;
    public static boolean showDeeds;

    /**
     * 
     * @param size Should be maximum world size of all servers in config
     */
    public void initMap(int size) {
        map = new byte[size+1][size+1];
    }
    
    private static void parseMapData() {
        JSONParser parser = new JSONParser();
        String lJsonString = ""; 
        try 
        {
            URL lURL = new URL(jsonPath);
            URLConnection lConnection = lURL.openConnection();
            BufferedReader lBufferedReader = new BufferedReader(new InputStreamReader(lConnection.getInputStream()));
            String lInputString;
            while ((lInputString = lBufferedReader.readLine()) != null) {
                lJsonString += lInputString;
            }
            lBufferedReader.close();

            String jsonX = lJsonString.substring(18, (lJsonString.length() - 1));

            JSONArray deeds = (JSONArray) parser.parse(jsonX);
            Iterator it = deeds.iterator();

            while (it.hasNext()) {
                JSONObject lDeed = (JSONObject) it.next();

                Long xStart = (Long) lDeed.get("x") - (Long) lDeed.get("tilesWest");
                Long xEnd = (Long) lDeed.get("x") + (Long) lDeed.get("tilesEast");
                Long yStart = (Long) lDeed.get("y") - (Long) lDeed.get("tilesNorth");
                Long yEnd = (Long) lDeed.get("y") + (Long) lDeed.get("tilesSouth");

                Long xPerimeterStart = xStart - (Long) lDeed.get("tilesPerimeter");
                Long xPerimeterEnd = xEnd + (Long) lDeed.get("tilesPerimeter");
                Long yPerimeterStart = yStart - (Long) lDeed.get("tilesPerimeter");
                Long yPerimeterEnd = yEnd + (Long) lDeed.get("tilesPerimeter");

                for ( int i = xPerimeterStart.intValue(); i <= xPerimeterEnd; i ++)
                {
                    if ( ( i >= 0 ) && ( i <= 4096 ) )
                    {
                        for ( int j = yPerimeterStart.intValue(); j <= yPerimeterEnd; j ++)
                        {
                            if ( ( j >= 0 ) && ( j <= 4096 ) )
                            {
                                if ( ( i > xStart ) && ( i <= xEnd ) && ( j > yStart  ) && ( j <= yEnd ) )
                                {
                                    map[i][j] = 1;	            			
                                }
                                else
                                {
                                    map[i][j] = 2;	
                                }		            			
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
	
    public void refreshMap()
    {
        LOG.info("Deed data refreshing...");
        LOG.info(String.format("JsonPath: %s", jsonPath));
        LOG.info(String.format("ServerSize: %s", LiveHudMapMod.serverSize));
        resetMapData();
        if (jsonPath.length() > 10)
        {
            parseMapData();
        }
        LOG.info("Deed data for Livemap refreshed");
    }

    public byte[][] getMapData()
    {
        return map;
    }

    private void resetMapData()
    {
        for (int i = 1; i <= 4096; i ++)
        {
            for (int j = 1; j <= 4096; j++)
            {
                map[i][j] = 0;
            }
        }
        LOG.info("Deed data for Livemap reseted");
    }

    public void setJsonPath(String jsonPath)
    {
        this.jsonPath = jsonPath;
    }

    public String getJsonPath()
    {
        return jsonPath;
    }

    public void setShowDeeds(boolean showDeeds)
    {
        this.showDeeds = showDeeds;
    }

    public boolean getShowDeed()
    {
        return showDeeds;
    }
}