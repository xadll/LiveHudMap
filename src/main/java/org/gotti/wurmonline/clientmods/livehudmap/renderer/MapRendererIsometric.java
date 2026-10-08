package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import java.awt.Color;
import java.awt.image.BufferedImage;

import com.wurmonline.client.game.NearTerrainDataBuffer;
import com.wurmonline.client.renderer.PickData;
import com.wurmonline.mesh.FieldData;
import com.wurmonline.mesh.FoliageAge;
import com.wurmonline.mesh.Tiles.Tile;
import org.gotti.wurmonline.clientmods.livehudmap.DeedData;

public class MapRendererIsometric extends AbstractSurfaceRenderer {
	
    private final NearTerrainDataBuffer mBuffer;
    
    public MapRendererIsometric(NearTerrainDataBuffer buffer) {
        super(buffer);
        mBuffer = buffer;
    }
	
        @Override
        public BufferedImage createMapDump(int xo, int yo, int lWidth, int lHeight, int px, int py) {

            final BufferedImage bi2 = new BufferedImage(lWidth, lHeight, BufferedImage.TYPE_INT_RGB);
            final float[] data = new float[lWidth * lHeight * 3];

            int y0 = lHeight + lHeight / 2;

            for (int x = 0; x < lWidth; x++) {
                int alt = y0 - 1;
                for (int y = y0 - 1; y >= -lHeight / 2 && alt >= 0; y--) {

                    final int tx = x + xo;
                    final int ty = y + yo;

                    float r = 0;
                    float g = 0;
                    float b = 0;

                    short height = 0;

                    // Check if coordinates are within world bounds
                    boolean isValidCoord = false;
                    if (DeedData.map != null && DeedData.map.length > 0) {
                        isValidCoord = (tx >= 0 && tx < DeedData.map.length && ty >= 0 && ty < DeedData.map[0].length);
                    }

                    if (isValidCoord) {
                        height = getSurfaceHeight(tx, ty);
                        float node = (float) (height / (Short.MAX_VALUE / 3.3f));

                        // Calculate slope relative to neighboring tile with map boundary handling
                        float node2 = node;
                        if (y != y0 - 1) {
                            boolean isValidNext = (tx + 1 < DeedData.map.length && ty + 1 < DeedData.map[0].length);
                            if (isValidNext) {
                                node2 = (float) (getSurfaceHeight(tx + 1, ty + 1) / (Short.MAX_VALUE / 3.3f));
                            }
                        }

                        final float hh = node;

                        float h = ((node2 - node) * 1500) / 256.0f * 0x1000 / 128 + hh / 2 + 1.0f;
                        h *= 0.4f;

                        r = h;
                        g = h;
                        b = h;

                        final Tile tile = getTileType(tx, ty);
                        final Color color = (tile != null) ? tile.getColor() : Tile.TILE_DIRT.getColor();

                        r *= (color.getRed() / 255.0f) * 2;
                        g *= (color.getGreen() / 255.0f) * 2;
                        b *= (color.getBlue() / 255.0f) * 2;

                        if (r < 0) r = 0; if (r > 1) r = 1;
                        if (g < 0) g = 0; if (g > 1) g = 1;
                        if (b < 0) b = 0; if (b > 1) b = 1;

                        // Render deeds
                        if (DeedData.showDeeds) {
                            byte deedType = DeedData.map[tx][ty];
                            if (deedType == (byte) 1) {
                                g = Math.min(1.0f, g + (40.0f / 255.0f));
                            } else if (deedType == (byte) 2) {
                                r = Math.min(1.0f, r + (40.0f / 255.0f));
                            }
                        }

                        // Adjust color for water / underwater terrain
                        if (node < 0) {
                            r = r * 0.2f + 0.4f * 0.4f;
                            g = g * 0.2f + 0.5f * 0.4f;
                            b = b * 0.2f + 1.0f * 0.4f;
                        }
                    }

                    // Render player marker
                    if (px == tx && py == ty) {
                        r = 1.0f;
                        g = 0;
                        b = 0;
                    }

                    final int altTarget = y - (int) (height * MAP_HEIGHT / 4 / (Short.MAX_VALUE / 2.5f));
                    while (alt > altTarget && alt >= 0) {
                        if (alt < lHeight) {
                            data[(x + alt * lWidth) * 3 + 0] = r * 255;
                            data[(x + alt * lWidth) * 3 + 1] = g * 255;
                            data[(x + alt * lWidth) * 3 + 2] = b * 255;
                        }
                        alt--;
                    }
                }
            }

            bi2.getRaster().setPixels(0, 0, lWidth, lHeight, data);
            return bi2;
        }
    
    
        private Tile getEffectiveTileType(int x, int y) {
            return getTileType(x, y);
        }

        private boolean isTreeorBush(Tile tileType) {
            if (tileType == null) return false;
            return tileType.isBush() || tileType.isTree();
        }

        private boolean isField(Tile tileType) {
            if (tileType == null) return false;
            return tileType == Tile.TILE_FIELD || tileType == Tile.TILE_FIELD2;
        }

        @Override
        public void pick(PickData pickData, float xMouse, float yMouse, int width, int height, int px, int py) {
            int xScreen = (int) (xMouse * width);
            int yScreen = (int) (yMouse * height);

            int xo = px - width / 2;
            if (xo < 0) xo = 0;

            int yo = py - height / 2;
            if (yo < 0) yo = 0;

            final int ox = xScreen + xo;
            int oy = yScreen + yo;

            int y0 = height + height / 2;
            int alt = y0 - 1;

            for (int y = y0 - 1; y >= -height / 2 && alt >= 0; y--) {
                int tx = ox;
                int ty = y + yo;

                int altTarget = y - (int) (getSurfaceHeight(tx, ty) * MAP_HEIGHT / 4 / (Short.MAX_VALUE / 2.5f));

                // OPRAVA: Použita ostrá nerovnost '>' u altTarget, protože createMapDump kreslí dokud je alt > altTarget
                if (yScreen <= alt && yScreen > altTarget) {
                    oy = ty;
                    break;
                }

                alt = altTarget;
            }

            final Tile tile = getEffectiveTileType(ox, oy);
            if (tile == null) {
                return;
            }

            byte lData = mBuffer.getData(ox, oy);
            String lSuffix = " ";

            if (isTreeorBush(tile)) {
                FoliageAge lFoliAge = FoliageAge.getFoliageAge(lData);
                lSuffix += lFoliAge.getAgeName();
                if ((lFoliAge.getAgeId() > FoliageAge.YOUNG_FOUR.getAgeId()) 
                        && (lFoliAge.getAgeId() < FoliageAge.OVERAGED.getAgeId()) 
                        && tile.usesNewData() && tile.isNormal()) {
                    lSuffix += " harvestable";
                }
                pickData.addText(tile.getName() + lSuffix);
            } 
            else if (isField(tile)) {
                lSuffix += FieldData.getTypeName(tile, lData) + ", " + FieldData.getAgeName(lData);
                if (!FieldData.isTended(lData)) {
                    lSuffix += ", untended";
                }
                pickData.addText(tile.getName() + lSuffix);
            }
        }

}
