package net.dries007.holoInventory.compat;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
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
        final IAEItemStack[] outs = pd.getCondensedOutputs();
        if (outs == null || outs.length == 0 || outs[0] == null) return null;
        // AE2FC encodes fluids as drop items, which render as a plain droplet.
        // Convert to a fluid packet so the hologram shows the fluid itself.
        final IAEItemStack out = Platform.stackConvertPacket(outs[0]);
        if (out == null) return null;
        final ItemStack output = out.getItemStack();
        output.stackSize = (int) Math.min(outs[0].getStackSize(), Integer.MAX_VALUE);
        return output;
    }
}
