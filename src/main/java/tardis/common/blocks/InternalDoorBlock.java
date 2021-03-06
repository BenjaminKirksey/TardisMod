package tardis.common.blocks;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import io.darkcraft.darkcore.mod.abstracts.AbstractBlock;
import io.darkcraft.darkcore.mod.abstracts.AbstractItemBlock;
import io.darkcraft.darkcore.mod.datastore.SimpleCoordStore;
import io.darkcraft.darkcore.mod.helpers.ServerHelper;

import tardis.Configs;
import tardis.TardisMod;
import tardis.api.ILinkable;
import tardis.api.ScrewdriverMode;
import tardis.api.TardisPermission;
import tardis.common.TMRegistry;
import tardis.common.core.TardisOutput;
import tardis.common.core.helpers.Helper;
import tardis.common.core.helpers.ScrewdriverHelper;
import tardis.common.core.helpers.ScrewdriverHelperFactory;
import tardis.common.core.schema.CoordStore;
import tardis.common.core.schema.PartBlueprint;
import tardis.common.dimension.TardisDataStore;
import tardis.common.tileents.CoreTileEntity;

public class InternalDoorBlock extends AbstractBlock implements ILinkable
{
	public HashMap<SimpleCoordStore,SimpleCoordStore> linkMap = new HashMap<SimpleCoordStore,SimpleCoordStore>();

	public InternalDoorBlock()
	{
		super(TardisMod.modName);
	}

	@Override
	public Class<? extends AbstractItemBlock> getIB()
	{
		return InternalDoorItemBlock.class;
	}

	@Override
	public void initData()
	{
		setBlockName("InternalDoor");
		setSubNames("InternalDoor","InternalDoorPrimary");
		setLightLevel(Configs.lightBlocks ? 1 : 0);
	}

	@Override
	public String getSubName(int num)
	{
		if((num % 8) < 4)
			return super.getSubName(0);
		else
			return super.getSubName(1);
	}

	@Override
	public void getSubBlocks(Item itemID,CreativeTabs tab,List itemList)
	{
		itemList.add(new ItemStack(itemID,1,0));
		itemList.add(new ItemStack(itemID,1,4));
	}

	@Override
	public void initRecipes()
	{
		// TODO Auto-generated method stub

	}

	public static int opposingFace(int myFace)
	{
		return ((myFace + 2) % 4);
	}

	public static int dx(int myFace)
	{
		if((myFace % 4) == 0)
			return -1;
		if((myFace % 4) == 2)
			return 1;
		return 0;
	}

	public static int dz(int myFace)
	{
		if((myFace % 4) == 1)
			return -1;
		if((myFace % 4) == 3)
			return 1;
		return 0;
	}

	@Override
	public boolean onBlockActivated(World w, int x, int y, int z, EntityPlayer player, int side, float i, float j, float k)
	{
		if(player != null)
		{
			ItemStack held =player.getHeldItem();
			if(held != null)
			{
				Item base = held.getItem();
				if(base != TMRegistry.screwItem) return false;
				ScrewdriverHelper help = ScrewdriverHelperFactory.get(held);
				if((help != null))
				{
					if(help.getMode() == ScrewdriverMode.Schematic)
					{
						if(ServerHelper.isServer())
						{
							TardisDataStore ds = Helper.getDataStore(w);
							if((ds == null) || (ds.hasPermission(player,TardisPermission.ROOMS)))
							{
								if(help.getSchemaName() != null)
								{
									String category = help.getSchemaCat();
									String name = help.getSchemaName();
									PartBlueprint pb = TardisMod.schemaHandler.getSchema(category, name);
									if(pb == null) return true; //If the schema isn't available anymore
									int facing = w.getBlockMetadata(x, y, z) % 4;
									CoordStore door = pb.getPrimaryDoorPos(opposingFace(facing));
									int nX = (x - door.x) + dx(facing);
									int nY = y - door.y;
									int nZ = (z - door.z) + dz(facing);
									TardisOutput.print("TIDB","OBA"+door.x+","+door.y+","+door.z);
									if(pb.roomFor(w, nX, nY, nZ, opposingFace(facing)))
									{
										CoreTileEntity te = Helper.getTardisCore(w);
										if((te == null) || te.addRoom(false, null)) //pass null as arg for schemacore since it adds itself
										{
											pb.reconstitute(w, nX, nY, nZ, opposingFace(facing));
											ServerHelper.sendString(player, "Room counter after creation: " + (te.getNumRooms() + 1) + "/" + te.getMaxNumRooms());
										}
										else
											player.addChatMessage(new ChatComponentText("Too many rooms in this TARDIS"));
									}
									else
									{
										TardisOutput.print("TIDB", "NoRoom:"+nX+","+nY+","+nZ,TardisOutput.Priority.DEBUG);
										player.addChatMessage(new ChatComponentText("Not enough room for schematic"));
									}
								}
								else
									player.addChatMessage(new ChatComponentText("No schematic loaded"));
							}
							else
								player.addChatMessage(CoreTileEntity.cannotModifyMessage);
						}
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World w, int x, int y, int z)
	{
		return super.getSelectedBoundingBoxFromPool(w, x, y, z);
		//return getCollisionBoundingBoxFromPool(w,x,y,z);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World w, int x, int y, int z)
	{
		if(w.getBlockMetadata(x, y, z) >= 8)
		{
			return AxisAlignedBB.getBoundingBox(0,0,0,0,0,0);
		}
		else
			return super.getCollisionBoundingBoxFromPool(w, x, y, z);
	}

	@Override
	public boolean isNormalCube(IBlockAccess w, int x, int y, int z)
	{
		return w.getBlockMetadata(x,y,z) < 8;
	}

	@Override
	public boolean isBlockSolid(IBlockAccess w, int x, int y, int z, int s)
	{
		return isNormalCube(w,x,y,z);
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess w, int x, int y, int z, int s)
	{
		switch(s)
		{
			case 0: y++;break;
			case 1: y--;break;
			case 2: z++;break;
			case 3: z--;break;
			case 4: x++;break;
			case 5: x--;break;
		}
		if(w.getBlockMetadata(x, y, z) >= 8)
			return false;
		return true;
	}

	@Override
	public void onNeighborBlockChange(World w, int x, int y, int z, Block bID)
	{
		//super.onNeighborBlockChange(w, x, y, z, bID);
		//if(bID != TMRegistry.schemaComponentBlock)
		//	manageConnected(w,x,y,z,w.getBlockMetadata(x, y, z)%4);
	}

	/**
	 * @param w
	 * @param x
	 * @param y
	 * @param z
	 * @param newState
	 * @return true if nothing needs to change
	 */
	private static boolean isValid(World w, int x, int y, int z, boolean newState)
	{
		Block b = w.getBlock(x, y, z);
		boolean isDoorBit = ((b == TMRegistry.internalDoorBlock) || SchemaComponentBlock.isDoorConnector(w,x,y,z));
		return isDoorBit == newState;
	}

	public static void manageConnected(World w, int x, int y, int z, int facing, boolean state)
	{

	}

	public static boolean hasConnector(World w, int x, int y, int z)
	{
		if(w.getBlock(x, y, z) == TMRegistry.internalDoorBlock)
		{
			int facing = w.getBlockMetadata(x, y, z) % 4;
			int connectedDoorX = x + dx(facing);
			int connectedDoorZ = z + dz(facing);
			if(w.getBlock(connectedDoorX, y, connectedDoorZ) == TMRegistry.internalDoorBlock)
				if((w.getBlockMetadata(connectedDoorX, y, connectedDoorZ) % 4) == opposingFace(facing))
					return true;
		}
		return false;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess w, int x, int y, int z)
	{
		if(w.getBlockMetadata(x, y, z) >= 8)
			setBlockBounds(0, 0, 0, 0, 0, 0);
		else
			setBlockBounds(0, 0, 0, 1, 1, 1);
	}

	@Override
	public void setBlockBoundsForItemRender()
	{
		setBlockBounds(0,0,0,1,1,1);
	}

	@Override
	public IIcon getIcon(int s, int d)
	{
		int iconMeta = (d % 8) >= 4 ? 1 : 0;
		//TardisOutput.print("TIDB", "Meta"+d+"->"+iconMeta);
		return super.getIcon(s, iconMeta);
	}

	@Override
	public int getDamageValue(World par1World, int par2, int par3, int par4)
    {
        return super.getDamageValue(par1World, par2, par3, par4) & 7;
    }

	@Override
	public void addCollisionBoxesToList(World w, int x, int y, int z, AxisAlignedBB par5AxisAlignedBB, List par6List, Entity par7Entity)
    {
        //this.setBlockBoundsBasedOnState(w, x, y, z);
		if(isNormalCube(w,x,y,z))
			super.addCollisionBoxesToList(w, x, y, z, par5AxisAlignedBB, par6List, par7Entity);
    }

	@Override
	public boolean link(EntityPlayer pl, SimpleCoordStore link, SimpleCoordStore other)
	{
		if((link == null) || (other == null)) return false;
		if(link.world != other.world) return false;
		TardisDataStore ds = Helper.getDataStore(link.world);
		if((ds == null) || ds.hasPermission(pl, TardisPermission.ROOMS))
		{
			if(((other.getBlock() == this) || (linkMap.get(other) == link)) && (link.getBlock() == this))
			{
				linkMap.put(link, other);
				link.setBlock(TMRegistry.magicDoorBlock, link.getMetadata(), 3);
			}
			if(linkMap.get(other) == link)
			{
				CoreTileEntity c = Helper.getTardisCore(link.world);
				if(c != null)
					c.refreshDoors(false);
			}
			return true;
		}
		return false;
	}


	@Override
	public boolean unlink(EntityPlayer pl, SimpleCoordStore link)
	{
		return false;
	}

	@Override
	public Set<SimpleCoordStore> getLinked(SimpleCoordStore link)
	{
		return null;
	}

	@Override
	public boolean isLinkable(SimpleCoordStore link)
	{
		return true;
	}

}
