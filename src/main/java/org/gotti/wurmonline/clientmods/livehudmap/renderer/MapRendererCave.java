package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Random;

import com.wurmonline.client.game.CaveDataBuffer;
import com.wurmonline.client.renderer.PickData;
import com.wurmonline.mesh.Tiles.Tile;

import org.gotti.wurmonline.clientmods.livehudmap.DeedData;
import org.gotti.wurmonline.clientmods.livehudmap.LiveHudMapMod;

public class MapRendererCave extends AbstractCaveRenderer {

	public static boolean showHiddenOre;

	public MapRendererCave(CaveDataBuffer buffer) {
		super(buffer);
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

				final short height = getHeight(x + xo, y + yo);
				final int tx = x + xo;
				final int ty = y + yo;
				Tile tile = getEffectiveTileType(x + xo, y + yo);

				final Color color;
				if (tile != null) {
					color = CaveColors.getColorFor(tile);
				}
				else {
					color = CaveColors.getColorFor(Tile.TILE_CAVE);
				}
				int r = color.getRed();
				int g = color.getGreen();
				int b = color.getBlue();
				
				
				if ( ( height < 0 && tile == Tile.TILE_CAVE ) || (  height < 0 && tile == Tile.TILE_CAVE_FLOOR_REINFORCED ) ) {
					r = (int) (r * 0.2f + 0.4f * 0.4f * 256f);
					g = (int) (g * 0.2f + 0.5f * 0.4f * 256f);
					b = (int) (b * 0.2f + 1.0f * 0.4f * 256f);
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
						if ( r == 255 )
						{
							if ( b <= 225 )
							{
								b = b + 30;
							}
							else
							{
								b = 255;
							}
						}
					}
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
		Tile tile = getTileType(x, y);

		if (!showHiddenOre && tile != Tile.TILE_CAVE_WALL && !isTunnel(tile) && isSurroundedByRock(x, y)) {
			return Tile.TILE_CAVE_WALL;
		}

		return tile;
	}

	private boolean isTunnel(int x, int y) {
		Tile tileType = getTileType(x, y);
		return isTunnel(tileType);
	}

	private boolean isTunnel(Tile tileType) {
		if (tileType == null) {
			return false;
		}
		return tileType == Tile.TILE_CAVE || tileType == Tile.TILE_CAVE_EXIT || tileType.isReinforcedFloor() || tileType.isRoad();
	}

	private boolean isSurroundedByRock(int x, int y) {
		return !isTunnel(x + 1, y) && !isTunnel(x - 1, y) && !isTunnel(x, y + 1) && !isTunnel(x, y - 1);
	}

	private int getQl(int x ,int y ,int sz){
		Random r = new Random();
		r.setSeed((x + y * sz) * 789221L);
		return 20 + r.nextInt(80);
	}

	@Override
	public void pick(PickData pickData, float xMouse, float yMouse, int width, int height, int px, int py) {
		final int ox = px + (int)(xMouse * width) - width / 2;
		final int oy = py + (int)(yMouse * height) - height / 2;
		final Tile tile = getEffectiveTileType(ox, oy);
		if (!isTunnel(tile)) {
			pickData.addText(tile.getName().replace(" wall", "").replace(" vein", "") +
					(LiveHudMapMod.serverSize>0?(String.format(" (%d QL)", getQl(ox,oy,LiveHudMapMod.serverSize))):""));
		}
	}
}