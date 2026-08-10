package net.dries007.holoInventory.network;

import static net.dries007.holoInventory.util.NBTKeys.NBT_KEY_CLASS;
import static net.dries007.holoInventory.util.NBTKeys.NBT_KEY_ID;
import static net.dries007.holoInventory.util.NBTKeys.NBT_KEY_LIST;
import static net.dries007.holoInventory.util.NBTKeys.NBT_KEY_NAME;
import static net.dries007.holoInventory.util.NBTKeys.NBT_KEY_TYPE;

import net.dries007.holoInventory.Config;
import net.dries007.holoInventory.HoloInventory;
import net.dries007.holoInventory.compat.InventoryDecoderRegistry;
import net.dries007.holoInventory.server.ServerHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;

import com.google.common.base.Strings;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/**
 * Client -> Server
 */
public class EntityRequestMessage implements IMessage {

    private int dim;
    private int entityId;

    @SuppressWarnings("unused")
    public EntityRequestMessage() {}

    public EntityRequestMessage(int dim, int entityId) {
        this.dim = dim;
        this.entityId = entityId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        dim = buf.readInt();
        entityId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(dim);
        buf.writeInt(entityId);
    }

    public static class Handler implements IMessageHandler<EntityRequestMessage, IMessage> {

        @Override
        public IMessage onMessage(EntityRequestMessage message, MessageContext ctx) {
            if (!ctx.side.isServer()) return null;

            final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            ServerHandler.serverEventHandler.schedule(player, () -> handle(message, player));
            return null;
        }

        private static void handle(EntityRequestMessage message, EntityPlayerMP player) {
            if (player.isDead || player.dimension != message.dim) return;

            final WorldServer world = player.getServerForPlayer();
            if (world == null) return;
            final Entity entity = world.getEntityByID(message.entityId);
            final double reach = player.theItemInWorldManager.getBlockReachDistance() + 1;
            if (entity == null || entity.isDead
                    || player.getDistanceSqToEntity(entity) > reach * reach
                    || !(entity instanceof IInventory || entity instanceof IMerchant))
                return;

            String type = entity.getClass().getCanonicalName();
            if (type == null) type = entity.getClass().getName();
            if (Config.bannedEntities.contains(type)) {
                final NBTTagCompound root = new NBTTagCompound();
                root.setByte(NBT_KEY_TYPE, (byte) 1);
                root.setInteger(NBT_KEY_ID, message.entityId);
                HoloInventory.getSnw().sendTo(new RemoveInventoryMessage(root), player);
                return;
            }

            if (entity instanceof IInventory inventory) {
                final NBTTagCompound root = new NBTTagCompound();
                root.setInteger(NBT_KEY_ID, message.entityId);
                root.setString(NBT_KEY_NAME, Strings.nullToEmpty(inventory.getInventoryName()));
                root.setString(NBT_KEY_CLASS, type);
                root.setTag(NBT_KEY_LIST, InventoryDecoderRegistry.toNBT(inventory));
                HoloInventory.getSnw().sendTo(new EntityInventoryMessage(root), player);
            } else {
                final NBTTagCompound tag = ((IMerchant) entity).getRecipes(player).getRecipiesAsTags();
                tag.setInteger(NBT_KEY_ID, message.entityId);
                tag.setString(NBT_KEY_NAME, entity.getCommandSenderName());
                tag.setString(NBT_KEY_CLASS, type);
                HoloInventory.getSnw().sendTo(new MerchantInventoryMessage(tag), player);
            }
        }
    }
}
