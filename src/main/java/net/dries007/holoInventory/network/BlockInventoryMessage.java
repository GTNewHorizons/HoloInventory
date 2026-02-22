package net.dries007.holoInventory.network;

import static net.dries007.holoInventory.util.NBTKeys.NBT_KEY_CLASS;
import static net.dries007.holoInventory.util.NBTKeys.NBT_KEY_COUNT;
import static net.dries007.holoInventory.util.NBTKeys.NBT_KEY_ID;
import static net.dries007.holoInventory.util.NBTKeys.NBT_KEY_LIST;
import static net.dries007.holoInventory.util.NBTKeys.NBT_KEY_NAME;

import java.util.ArrayList;
import java.util.List;

import net.dries007.holoInventory.Config;
import net.dries007.holoInventory.client.Renderer;
import net.dries007.holoInventory.util.NamedData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import com.gtnewhorizon.gtnhlib.util.data.ItemId;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;

/**
 * Server -> Client
 */
public class BlockInventoryMessage implements IMessage {

    NBTTagCompound data;

    public BlockInventoryMessage(NBTTagCompound inventoryData) {
        data = inventoryData;
    }

    @SuppressWarnings("unused")
    public BlockInventoryMessage() {}

    @Override
    public void fromBytes(ByteBuf buf) {
        data = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, data);
    }

    public static class Handler implements IMessageHandler<BlockInventoryMessage, IMessage> {

        @Override
        public IMessage onMessage(BlockInventoryMessage message, MessageContext ctx) {
            if (message == null || message.data == null) return null;
            if (!ctx.side.isClient()) return null;

            final NBTTagCompound dataTag = message.data;
            final int tileId = dataTag.getInteger(NBT_KEY_ID);
            final String name = dataTag.getString(NBT_KEY_NAME);
            final String clazz = dataTag.hasKey(NBT_KEY_CLASS) ? dataTag.getString(NBT_KEY_CLASS) : null;

            final NBTTagList list = dataTag.getTagList(NBT_KEY_LIST, Constants.NBT.TAG_COMPOUND);
            final int count = list.tagCount();

            ItemStack[] itemStacks;

            if (Config.enableStacking) {
                Object2ObjectOpenCustomHashMap<ItemId, Long> mergedCounts = new Object2ObjectOpenCustomHashMap<>(
                        ItemId.ITEM_META_NBT_STRATEGY);

                Object2ObjectOpenCustomHashMap<ItemId, ItemStack> templates = new Object2ObjectOpenCustomHashMap<>(
                        ItemId.ITEM_META_NBT_STRATEGY);

                for (int i = 0; i < count; i++) {
                    NBTTagCompound tag = list.getCompoundTagAt(i);
                    ItemStack stack = ItemStack.loadItemStackFromNBT(tag);
                    if (stack == null) continue;

                    stack.stackSize = tag.getInteger(NBT_KEY_COUNT);
                    if (stack.stackSize <= 0) continue;

                    ItemId key = ItemId.createNoCopy(stack);

                    mergedCounts.merge(key, (long) stack.stackSize, Long::sum);

                    templates.putIfAbsent(key, stack.copy());
                }

                List<ItemStack> finalList = new ArrayList<>();

                for (Object2ObjectOpenCustomHashMap.Entry<ItemId, Long> entry : mergedCounts.object2ObjectEntrySet()) {
                    ItemId key = entry.getKey();
                    long total = entry.getValue();

                    ItemStack template = templates.get(key);
                    if (template == null) continue;

                    while (total > 0) {
                        int part = (int) Math.min(Integer.MAX_VALUE, total);

                        ItemStack copy = template.copy();
                        copy.stackSize = part;

                        finalList.add(copy);
                        total -= part;
                    }
                }

                itemStacks = finalList.toArray(new ItemStack[0]);

            } else {

                List<ItemStack> stacksList = new ArrayList<>(count);

                for (int i = 0; i < count; i++) {
                    NBTTagCompound tag = list.getCompoundTagAt(i);
                    ItemStack stack = ItemStack.loadItemStackFromNBT(tag);
                    if (stack != null) {
                        stack.stackSize = tag.getInteger(NBT_KEY_COUNT);
                        stacksList.add(stack);
                    }
                }

                itemStacks = stacksList.toArray(new ItemStack[0]);
            }

            NamedData<ItemStack[]> data;
            if (clazz != null) {
                data = new NamedData<>(name, clazz, itemStacks);
            } else {
                data = new NamedData<>(name, itemStacks);
            }

            Renderer.tileInventoryMap.put(tileId, data);
            return null;
        }
    }
}
