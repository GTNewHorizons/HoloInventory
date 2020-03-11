/*
 * Copyright (c) 2014. Dries K. Aka Dries007
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.dries007.holoInventory.util;

import cpw.mods.fml.common.registry.GameRegistry;
import net.dries007.holoInventory.HoloInventory;
import net.dries007.holoInventory.server.ServerHandler;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class CommonProxy
{
    private ServerHandler serverHandler;

    public void preInit()
    {
        serverHandler = new ServerHandler();
    }

    public void init()
    {
        serverHandler.init();
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(HoloInventory.holoGlasses), "I I", "I#I", "AAA", '#', Blocks.glass_pane, 'I', Items.iron_ingot, 'A', Blocks.coal_block));
    }

    public void postInit()
    {

    }

    public void serverStarting()
    {
        serverHandler.init();
    }
}
