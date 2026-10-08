package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import java.awt.Color;
import java.awt.image.BufferedImage;

import com.wurmonline.client.game.NearTerrainDataBuffer;
import com.wurmonline.client.renderer.PickData;
import com.wurmonline.mesh.FieldData;
import com.wurmonline.mesh.FoliageAge;
import com.wurmonline.mesh.Tiles.Tile;
import org.gotti.wurmonline.clientmods.livehudmap.DeedData;

public class MapRendererTopographic extends AbstractSurfaceRenderer {
	
        private final short interval;

        private final NearTerrainDataBuffer mBuffer;
        
	public MapRendererTopographic(NearTerrainDataBuffer buffer) {
		super(buffer);
		this.interval = 250;
                mBuffer = buffer;
	}

        @Override
        public BufferedImage createMapDump(int xo, int yo, int lWidth, int lHeight, int px, int py) {

            final BufferedImage bi2 = new BufferedImage(lWidth, lWidth, BufferedImage.TYPE_INT_RGB);
            final float[] data = new float[lWidth * lWidth * 3];

            for (int x = 0; x < lWidth; x++) {
                for (int y = lWidth - 1; y >= 0; y--) {

                    final int tx = x + xo;
                    final int ty = y + yo;

                    int r = 0;
                    int g = 0;
                    int b = 0;

                    // Check if coordinates are within world and DeedData map bounds
                    boolean isValidCoord = false;
                    if (DeedData.map != null && DeedData.map.length > 0) {
                        isValidCoord = (tx >= 0 && tx < DeedData.map.length && ty >= 0 && ty < DeedData.map[0].length);
                    }

                    if (isValidCoord) {
                        final short height = getSurfaceHeight(tx, ty);

                        // Safely calculate neighboring heights for contour lines
                        final short nearHeightNX = (tx - 1 >= 0) ? getSurfaceHeight(tx - 1, ty) : height;
                        final short nearHeightNY = (ty - 1 >= 0) ? getSurfaceHeight(tx, ty - 1) : height;
                        final short nearHeightX  = (tx + 1 < DeedData.map.length) ? getSurfaceHeight(tx + 1, ty) : height;
                        final short nearHeightY  = (ty + 1 < DeedData.map[0].length) ? getSurfaceHeight(tx, ty + 1) : height;

                        boolean isContour = checkContourLine(height, nearHeightNX, interval)
                                || checkContourLine(height, nearHeightNY, interval)
                                || checkContourLine(height, nearHeightX, interval)
                                || checkContourLine(height, nearHeightY, interval);

                        final Tile tile = getTileType(tx, ty);
                        final Color color = (tile != null) ? tile.getColor() : Tile.TILE_DIRT.getColor();

                        r = color.getRed();
                        g = color.getGreen();
                        b = color.getBlue();

                        if (isContour) {
                            r = 0;
                            g = 0;
                            b = 0;
                        } else if (height < 0) {
                            r = (int) (r * 0.2f + 0.4f * 0.4f * 256f);
                            g = (int) (g * 0.2f + 0.5f * 0.4f * 256f);
                            b = (int) (b * 0.2f + 1.0f * 0.4f * 256f);
                        }

                        // Render deeds
                        if (DeedData.showDeeds) {
                            byte deedType = DeedData.map[tx][ty];
                            if (deedType == (byte) 1) {
                                g = (g <= 215) ? g + 40 : 255;
                            } else if (deedType == (byte) 2) {
                                r = (r <= 215) ? r + 40 : 255;
                            }
                        }
                    }
                    // If !isValidCoord, r, g, b remain 0 (black void outside map)

                    // Render player marker
                    if (px == tx && py == ty) {
                        r = Color.RED.getRed();
                        g = 0;
                        b = 0;
                    }

                    data[(x + y * lWidth) * 3 + 0] = r;
                    data[(x + y * lWidth) * 3 + 1] = g;
                    data[(x + y * lWidth) * 3 + 2] = b;
                }
            }

            bi2.getRaster().setPixels(0, 0, lWidth, lWidth, data);
            return bi2;
        }

	private boolean checkContourLine(short h0, short h1, short interval) {
		if (h0 == h1) {
			return false;
		}
		for (int i = h0; i <= h1; i++) {
			if (i % interval == 0) {
				return true;
			}
		}
		return false;
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
	public void pick(PickData pickData, float xMouse, float yMouse, int width, int height, int px, int py) 
	{
		final int ox = px + (int)( xMouse * width ) - width / 2;
		final int oy = py + (int)( yMouse * height ) - height / 2;
		final Tile tile = getEffectiveTileType( ox, oy );
		
		byte lData = mBuffer.getData( ox, oy );
		String lSuffix = " ";
		
		if ( isTreeorBush( tile ) ) 
		{
			FoliageAge lFoliAge = FoliageAge.getFoliageAge( lData );
			lSuffix += lFoliAge.getAgeName();
			if ( ( lFoliAge.getAgeId() > FoliageAge.YOUNG_FOUR.getAgeId() ) && ( lFoliAge.getAgeId() < FoliageAge.OVERAGED.getAgeId() ) && tile.usesNewData() && tile.isNormal() )
			{
				lSuffix += " harvestable";
			}
			pickData.addText( tile.getName() + lSuffix );
		}
		else if ( isField( tile ) )
		{
			lSuffix += FieldData.getTypeName( tile, lData ) + ", " + FieldData.getAgeName( lData );
			if ( !FieldData.isTended( lData) )
			{
				lSuffix += ", untended";
			}
			pickData.addText( tile.getName() + lSuffix );
		}
	}

}
