/*
 * Copyright (c) 2014. Dries K. Aka Dries007 Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions: The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.dries007.holoInventory.util;

import static net.dries007.holoInventory.util.NBTKeys.NBT_KEY_LIST;
import static net.dries007.holoInventory.util.NBTKeys.NBT_KEY_NAME;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import net.dries007.holoInventory.Config;
import net.dries007.holoInventory.HoloInventory;
import net.dries007.holoInventory.compat.InventoryDecoderRegistry;
import net.dries007.holoInventory.network.BlockInventoryMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;

import com.google.common.base.Strings;

public class InventoryData {

    public final Coord coord;
    public WeakReference<IInventory> te;
    public final WeakHashMap<EntityPlayer, NBTTagCompound> playerSet = new WeakHashMap<>();
    private final WeakHashMap<EntityPlayer, Long> lastSentTicks = new WeakHashMap<>();
    private String name;
    /** Class of the tile entity, not of the inventory it was unwrapped into. */
    private final String type;
    private long snapshotTick = Long.MIN_VALUE;
    private NBTTagCompound snapshot;

    public InventoryData(IInventory te, Coord coord, String type) {
        this.coord = coord;
        this.te = new WeakReference<>(te);
        this.name = Strings.nullToEmpty(te.getInventoryName());
        this.type = type;
    }

    public void sendIfOld(EntityPlayerMP player) {
        IInventory ste = te.get();
        if (ste == null) {
            return;
        }
        final long tick = player.worldObj.getTotalWorldTime();
        if (snapshot == null || snapshotTick != tick) {
            snapshotTick = tick;
            snapshot = new NBTTagCompound();
            coord.writeToNBT(snapshot);
            snapshot.setString(NBT_KEY_NAME, name);
            snapshot.setTag(NBT_KEY_LIST, InventoryDecoderRegistry.toNBT(ste));
        }

        final Long lastSentTick = lastSentTicks.get(player);
        if (!snapshot.equals(playerSet.get(player)) || lastSentTick == null
                || tick - lastSentTick >= 20L * Math.max(1, Config.syncFreq)) {
            playerSet.put(player, snapshot);
            lastSentTicks.put(player, tick);
            HoloInventory.getSnw().sendTo(new BlockInventoryMessage(snapshot), player);
        }
    }

    /** An ender chest hands out a different inventory per player, so the cached snapshot and name cannot be reused. */
    public void update(IInventory inventory) {
        if (te.get() != inventory) {
            snapshot = null;
            te = new WeakReference<>(inventory);
            name = Strings.nullToEmpty(inventory.getInventoryName());
        }
    }

    public String getType() {
        return type;
    }
}
