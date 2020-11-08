package com.feed_the_beast.mods.ftbranks;

import com.feed_the_beast.mods.ftbranks.api.FTBRanksAPI;
import com.feed_the_beast.mods.ftbranks.impl.FTBRanksAPIImpl;
import com.feed_the_beast.mods.ftbranks.impl.PermissionAPIWrapper;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import net.minecraftforge.server.permission.PermissionAPI;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author LatvianModder
 */
@Mod("ftbranks")
public class FTBRanks
{
	public static final Logger LOGGER = LogManager.getLogger("FTB Ranks");

	public FTBRanks()
	{
		FTBRanksAPI.INSTANCE = new FTBRanksAPIImpl();
		PermissionAPI.setPermissionHandler(new PermissionAPIWrapper(PermissionAPI.getPermissionHandler()));
		ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
	}
}