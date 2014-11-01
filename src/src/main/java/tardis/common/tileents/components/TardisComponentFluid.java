package tardis.common.tileents.components;

import tardis.TardisMod;
import tardis.common.core.Helper;
import tardis.common.tileents.TardisComponentTileEntity;
import tardis.common.tileents.TardisCoreTileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TardisComponentFluid extends TardisAbstractComponent implements IFluidHandler
{
	protected TardisComponentFluid() { }
	
	public TardisComponentFluid(TardisComponentTileEntity parent)
	{
		parentObj = parent;
	}
	
	@Override
	public ITardisComponent create(TardisComponentTileEntity parent)
	{
		return new TardisComponentFluid(parent);
	}
	
	private FluidStack[] getTanks()
	{
		if(parentObj != null && parentObj.worldObj != null)
		{
			TardisCoreTileEntity core = Helper.getTardisCore(parentObj.worldObj);
			if(core != null)
				return core.getTanks();
		}
		return null;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		FluidStack[] tanks = getTanks();
		if(tanks != null)
		{
			for(int i = 0;i<tanks.length;i++)
			{
				if(tanks[i] == null)
				{
					if(doFill)
						tanks[i] = new FluidStack(resource.fluidID,Math.min(TardisMod.maxFlu, resource.amount));
					return Math.min(TardisMod.maxFlu, resource.amount);
				}
				else if(tanks[i].equals(resource))
				{
					int maxFill = Math.min(TardisMod.maxFlu - tanks[i].amount,resource.amount);
					if(doFill)
						tanks[i].amount += maxFill;
					return maxFill;
				}
			}
		}
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		FluidStack[] tanks = getTanks();
		if(tanks != null)
		{
			for(int i = 0;i<tanks.length;i++)
			{
				if(tanks[i] != null && tanks[i].equals(resource))
				{
					int toDrain = Math.min(tanks[i].amount,resource.amount);
					if(doDrain)
					{
						if(toDrain == tanks[i].amount)
							tanks[i] = null;
						else
							tanks[i].amount -= toDrain;
					}
					return new FluidStack(resource.fluidID,toDrain);
				}
			}
		}
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		FluidStack[] tanks = getTanks();
		if(tanks != null)
		{
			for(int i = 0;i<tanks.length;i++)
			{
				if(tanks[i] != null)
				{
					int toDrain = Math.min(tanks[i].amount,maxDrain);
					int fluidID = tanks[i].fluidID;
					if(doDrain)
					{
						if(toDrain == tanks[i].amount)
							tanks[i] = null;
						else
							tanks[i].amount -= toDrain;
					}
					return new FluidStack(fluidID,toDrain);
				}
			}
		}
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		FluidStack[] tanks = getTanks();
		if(tanks != null)
		{
			for(int i = 0;i<tanks.length;i++)
			{
				if(tanks[i] == null)
				{
					return true;
				}
				else if(tanks[i].fluidID == fluid.getID())
				{
					return tanks[i].amount < TardisMod.maxFlu;
				}
			}
		}
		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		FluidStack[] tanks = getTanks();
		if(tanks != null)
		{
			for(int i = 0;i<tanks.length;i++)
			{
				if(tanks[i].fluidID == fluid.getID())
				{
					return tanks[i].amount > 0;
				}
			}
		}
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		FluidStack[] tanks = getTanks();
		if(tanks != null)
		{
			FluidTankInfo[] retVal = new FluidTankInfo[tanks.length];
			for(int i = 0;i<tanks.length;i++)
			{
				FluidTankInfo info = new FluidTankInfo(tanks[i],TardisMod.maxFlu);
				retVal[i] = info;
			}
			return retVal;
		}
		return null;
	}

}