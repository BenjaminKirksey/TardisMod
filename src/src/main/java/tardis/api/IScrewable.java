package tardis.api;

import net.minecraft.entity.player.EntityPlayer;

public interface IScrewable
{
	public boolean screw(ScrewdriverMode mode, EntityPlayer player);
}
