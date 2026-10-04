package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.gotti.wurmonline.clientmods.livehudmap.DeedData;

import com.wurmonline.client.game.NearTerrainDataBuffer;
import com.wurmonline.client.renderer.PickData;
import com.wurmonline.mesh.FieldData;
import com.wurmonline.mesh.FoliageAge;
import com.wurmonline.mesh.Tiles.Tile;

public class MapRendererFlat extends AbstractSurfaceRenderer {
    
	private final NearTerrainDataBuffer mBuffer;
	
	public MapRendererFlat(NearTerrainDataBuffer buffer) {
		super(buffer);
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
				final short height = getSurfaceHeight(x + xo, y + yo);
				
				final int tx = x + xo;
				final int ty = y + yo;
				
				final Tile tile = getTileType(tx, ty);

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
					
				if (height < 0) {
					r = (int) (r * 0.2f + 0.4f * 0.4f * 256f);
					g = (int) (g * 0.2f + 0.5f * 0.4f * 256f);
					b = (int) (b * 0.2f + 1.0f * 0.4f * 256f);
				}
				
				if (px == x + xo && py == y + yo) {
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