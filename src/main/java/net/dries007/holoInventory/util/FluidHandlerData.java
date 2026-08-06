package net.dries007.holoInventory.util;

import static net.dries007.holoInventory.util.NBTKeys.*;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import net.dries007.holoInventory.Config;
import net.dries007.holoInventory.HoloInventory;
import net.dries007.holoInventory.network.BlockFluidHandlerMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class FluidHandlerData {

    public final Coord coord;
    public WeakReference<IFluidHandler> te;
    public final WeakHashMap<EntityPlayer, NBTTagCompound> playerSet = new WeakHashMap<>();
    private final WeakHashMap<EntityPlayer, Long> lastSentTicks = new WeakHashMap<>();
    private long snapshotTick = Long.MIN_VALUE;
    private NBTTagCompound snapshot;

    public FluidHandlerData(IFluidHandler fluidHandler, Coord coord) {
        this.coord = coord;
        this.te = new WeakReference<>(fluidHandler);
    }

    public void sendIfOld(EntityPlayerMP player) {
        IFluidHandler fluidHandler = te.get();
        if (fluidHandler == null) {
            return;
        }
        final long tick = player.worldObj.getTotalWorldTime();
        if (snapshot == null || snapshotTick != tick) {
            snapshotTick = tick;
            snapshot = new NBTTagCompound();
            coord.writeToNBT(snapshot);
            snapshot.setTag(NBT_KEY_TANK, encodeFluidTankInfo(fluidHandler));
        }

        final Long lastSentTick = lastSentTicks.get(player);
        if (!snapshot.equals(playerSet.get(player)) || lastSentTick == null
                || tick - lastSentTick >= 20L * Math.max(1, Config.syncFreq)) {
            playerSet.put(player, snapshot);
            lastSentTicks.put(player, tick);
            HoloInventory.getSnw().sendTo(new BlockFluidHandlerMessage(snapshot), player);
        }
    }

    private NBTTagList encodeFluidTankInfo(IFluidHandler fluidHandler) {
        NBTTagList tagList = new NBTTagList();
        FluidTankInfo[] tankInfos = fluidHandler.getTankInfo(ForgeDirection.UNKNOWN);
        if (tankInfos == null) return tagList;

        for (FluidTankInfo tankInfo : tankInfos) {
            if (tankInfo == null) continue;
            if (tankInfo.fluid == null) continue;

            NBTTagCompound fluidTag = new NBTTagCompound();
            tankInfo.fluid.writeToNBT(fluidTag);
            fluidTag.setInteger(NBT_KEY_CAPACITY, tankInfo.capacity);
            tagList.appendTag(fluidTag);
        }
        return tagList;
    }

    public void update(IFluidHandler fluidHandler) {
        te = new WeakReference<>(fluidHandler);
    }
}
