package net.dries007.holoInventory.compat;

import static net.dries007.holoInventory.util.NBTKeys.NBT_KEY_AMOUNT;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.util.Platform;

/**
 * AE2 is a compile only dependency, so everything that widens an AE2 type has to live in a class that is only ever
 * loaded when AE2 is present.
 */
public class AE2Patterns {

    private AE2Patterns() {}

    @Nullable
    public static ItemStack getPatternOutput(ItemStack pattern, World w) {
        if (pattern == null || !(pattern.getItem() instanceof ICraftingPatternItem)) return null;
        final ICraftingPatternDetails pd = ((ICraftingPatternItem) pattern.getItem()).getPatternForItem(pattern, w);
        if (pd == null) return null;
        final IAEStack<?>[] outs = pd.getCondensedAEOutputs();
        if (outs == null || outs.length == 0 || outs[0] == null) return null;
        final IAEStack<?> out = outs[0];

        final ItemStack output;
        if (out instanceof IAEFluidStack) {
            // getItemStackForNEI sends fluids through NEI, which hands back a stackable GregTech display item,
            // and the renderer would then draw one copy per mB. The AE2FC packet is unstackable.
            final IAEItemStack packet = Platform.stackConvertPacket(out);
            output = packet == null ? null : packet.getItemStack();
        } else {
            output = out.getItemStackForNEI();
        }
        if (output == null) return null;

        output.stackSize = (int) Math.min(out.getStackSize(), Integer.MAX_VALUE);
        // anything but an item stack is a resource amount shown on a display item, not a number of items,
        // so the renderer must not turn it into a pile of copies
        if (!(out instanceof IAEItemStack)) {
            if (!output.hasTagCompound()) output.setTagCompound(new NBTTagCompound());
            output.getTagCompound().setBoolean(NBT_KEY_AMOUNT, true);
        }
        return output;
    }
}
