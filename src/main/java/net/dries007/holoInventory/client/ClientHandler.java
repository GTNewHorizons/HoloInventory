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

package net.dries007.holoInventory.client;

import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.FMLCommonHandler;

public class ClientHandler {

    public static final KeyManager KEY_MANAGER = new KeyManager();
    public static final RenderItem RENDER_ITEM = new RenderItem() {

        @Override
        public void doRender(EntityItem par1EntityItem, double par2, double par4, double par6, float par8, float par9) {
            try {
                super.doRender(par1EntityItem, par2, par4, par6, par8, par9);
            } catch (Exception ignored) {}
        }

        @Override
        public boolean shouldBob() {
            return false;
        }

        @Override
        public boolean shouldSpreadItems() {
            return false;
        }
    };

    public void init() {
        MinecraftForge.EVENT_BUS.register(Renderer.INSTANCE);
        FMLCommonHandler.instance().bus().register(KEY_MANAGER);
    }

    public void postInit() {
        RENDER_ITEM.setRenderManager(RenderManager.instance);
    }
}
