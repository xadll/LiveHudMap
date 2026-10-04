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
		if (yo < 0)
			yo = 0;
		if (xo < 0)
			xo = 0;

		final BufferedImage bi2 = new BufferedImage(lWidth, lWidth, BufferedImage.TYPE_INT_RGB);
		final float[] data = new float[lWidth * lWidth * 3];

		for (int x = 0; x < lWidth; x++) {
			for (int y = lWidth - 1; y >= 0; y--) {
                            
				final int tx = x + xo;
				final int ty = y + yo;
                                
				final short height = getSurfaceHeight(x + xo, y + yo);
				final short nearHeightNX = x == 0 ? height : getSurfaceHeight(x + xo - 1, y + yo);
				final short nearHeightNY = y == 0 ? height : getSurfaceHeight(x + xo, y + yo - 1);
				final short nearHeightX = x == lWidth - 1 ? height : getSurfaceHeight(x + xo + 1, y + yo);
				final short nearHeightY = y == lWidth - 1 ? height : getSurfaceHeight(x + xo, y + yo + 1);
				boolean isControur = checkContourLine(height, nearHeightNX, interval)
						|| checkContourLine(height, nearHeightNY, interval)
						|| checkContourLine(height, nearHeightX, interval)
						|| checkContourLine(height, nearHeightY, interval);

				final Tile tile = getTileType(x + xo, y + yo);
				final Color color;
				if (tile != null) {
					color = tile.getColor();
				}
				else {
					color = Tile.TILE_DIRT.getColor();
				}
				int r = color.getRed();
				int g = color.getGreen();
				int b = color.getBlue();
				if (isControur) {
					r = 0;
					g = 0;
					b = 0;
				}
				else if (height < 0) {
					r = (int) (r * 0.2f + 0.4f * 0.4f * 256f);
					g = (int) (g * 0.2f + 0.5f * 0.4f * 256f);
					b = (int) (b * 0.2f + 1.0f * 0.4f * 256f);
				}

				if (px == x + xo && py == y + yo) {
					r = Color.RED.getRed();
					g = 0;
					b = 0;
				}
                                
				if ( DeedData.showDeeds )
				{
					if ( DeedData.map[tx][ty] == (byte)1 )
					{
						if ( g <= 215 )
						{
							g = g + 40;
						}
						else
						{
							g = 255;
						}
					}
					else if ( DeedData.map[tx][ty] == (byte)2 )
					{
						if ( r <= 215 )
						{
							r = r + 40;
						}
						else
						{
							r = 255;
						}
					}
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
