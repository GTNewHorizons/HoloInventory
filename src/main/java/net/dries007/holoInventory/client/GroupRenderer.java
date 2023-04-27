package net.dries007.holoInventory.client;

import java.text.DecimalFormat;

import net.dries007.holoInventory.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/**
 * Keeps track of render scale, spacing, etc. to draw a set of icons prettier
 */
public class GroupRenderer {

    private static float time;
    private final EntityItem fakeEntityItem = new EntityItem(Minecraft.getMinecraft().theWorld);
    private static final int TEXT_COLOR = 255 + (255 << 8) + (255 << 16) + (170 << 24);

    // changed with an attached debugger..
    static int stackSizeDebugOverride = 0;

    private float scale, width, height, spacing;
    private int columns, rows;
    private boolean renderText;

    public GroupRenderer() {
        fakeEntityItem.hoverStart = 0f;
    }

    public static void updateTime() {
        time = (float) (360.0 * (double) (System.currentTimeMillis() & 0x3FFFL) / (double) 0x3FFFL);
    }

    public void calculateColumns(int totalAmount) {
        if (totalAmount < 9) columns = totalAmount;
        else if (totalAmount <= 27) columns = 9;
        else if (totalAmount <= 54) columns = 11;
        else if (totalAmount <= 90) columns = 14;
        else if (totalAmount <= 109) columns = 18;
        else columns = 21;
    }

    public void calculateRows(int totalAmount) {
        setRows((totalAmount % columns == 0) ? (totalAmount / columns) - 1 : totalAmount / columns);
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public void setScale(float scaleAddition) {
        float scaleModifier;
        if (columns > 9) scaleModifier = 0.2f - columns * 0.005f;
        else scaleModifier = 0.2f + (9 - columns) * 0.05f;
        scale = scaleModifier + scaleAddition;
    }

    public void setSpacing(float spacing) {
        this.spacing = spacing;
        width = columns * scale * (spacing + 0.1f) * 0.4f;
        height = rows * scale * (spacing + 0.1f) * 0.4f;
    }

    public void setRenderText(boolean renderText) {
        this.renderText = renderText;
    }

    public int getColumns() {
        return columns;
    }

    /**
     * Renders 1 item
     *
     * @param itemStack itemStack to render
     * @param column    the column the item needs to be rendered at
     * @param row       the row the item needs to be rendered at
     * @param stackSize the stackSize to use for text
     */
    public void renderItem(ItemStack itemStack, int column, int row, int stackSize) {
        RenderHelper.enableStandardItemLighting();
        GL11.glPushMatrix();
        GL11.glTranslatef(width - ((column + 0.2f) * scale * spacing), height - ((row + 0.05f) * scale * spacing), 0f);
        GL11.glScalef(scale, scale, scale);
        if (Minecraft.getMinecraft().gameSettings.fancyGraphics)
            GL11.glRotatef(Config.rotateItems ? time : 0f, 0.0F, 1.0F, 0.0F);
        else GL11.glRotatef(RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
        fakeEntityItem.setEntityItemStack(itemStack);
        ClientHandler.RENDER_ITEM.doRender(fakeEntityItem, 0, 0, 0, 0, 0);
        if (itemStack.hasEffect(0)) GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
        RenderHelper.disableStandardItemLighting();
        if (renderText && !(itemStack.getMaxStackSize() == 1 && itemStack.stackSize == 1)) {
            GL11.glPushMatrix();
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glTranslatef(
                    width - ((column + 0.2f) * scale * spacing),
                    height - ((row + 0.05f) * scale * spacing),
                    0f);
            GL11.glScalef(scale, scale, scale);
            GL11.glScalef(0.03f, 0.03f, 0.03f);
            GL11.glRotatef(180, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(-1f, 1f, 0f);
            RenderManager.instance.getFontRenderer().drawString(doStackSizeCrap(stackSize), 0, 0, TEXT_COLOR, true);
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glPopMatrix();
        }
    }

    public void renderBG() {
        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        Tessellator tess = Tessellator.instance;
        Tessellator.renderingWorldRenderer = false;
        tess.startDrawing(GL11.GL_QUADS);
        tess.setColorRGBA(Config.colorR, Config.colorG, Config.colorB, Config.colorAlpha);
        double d = scale / 3;
        tess.addVertex(width + d, -d - height, 0);
        tess.addVertex(-width - d, -d - height, 0);
        tess.addVertex(-width - d, d + height, 0);
        tess.addVertex(width + d, d + height, 0);
        tess.draw();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    public void renderName(String name) {
        FontRenderer fontRenderer = RenderManager.instance.getFontRenderer();
        if (Config.nameOverrides.containsKey(name)) name = Config.nameOverrides.get(name);
        else name = StatCollector.translateToLocal(name);
        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        GL11.glTranslated(0f, height + scale / 1.25, 0f);

        GL11.glScaled(scale, scale, scale);
        GL11.glScalef(1.5f, 1.5f, 1.5f);
        GL11.glScalef(0.03f, 0.03f, 0.03f);
        GL11.glTranslated(fontRenderer.getStringWidth(name) / 2f, 0f, 0f);
        GL11.glRotatef(180, 0.0F, 0.0F, 1.0F);
        fontRenderer.drawString(name, 0, 0, TEXT_COLOR, true);

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glPopMatrix();
    }

    private static final DecimalFormat DF_ONE_FRACTION_DIGIT = new DecimalFormat("##.0");
    private static final DecimalFormat DF_TWO_FRACTION_DIGIT = new DecimalFormat("#.00");
    private static final String[] suffixNormal = { "", "K", "M", "B" };
    private static final String[] suffixDarkened = { "", EnumChatFormatting.GRAY + "K", EnumChatFormatting.GRAY + "M",
            EnumChatFormatting.GRAY + "B" };

    /**
     * Shifts GL & returns the string
     *
     * @param stackSize the stackSize.
     * @return the string to be rendered
     */
    private String doStackSizeCrap(int stackSize) {
        if (stackSizeDebugOverride != 0) stackSize = stackSizeDebugOverride;
        String string = formatStackSize(stackSize);

        GL11.glTranslatef(-RenderManager.instance.getFontRenderer().getStringWidth(string) / 2.f, 0f, 0f);
        return string;
    }

    private static String formatStackSize(long i) {
        String[] suffixSelected = Config.renderSuffixDarkened ? suffixDarkened : suffixNormal;
        int level = 0;
        while (i > 1000 && level < suffixSelected.length - 1) {
            level++;
            if (i >= 100_000) {
                // still more level to go, or 0 fraction digit
                i /= 1000;
            } else if (i >= 10_000) {
                // 1 fraction digit
                return DF_ONE_FRACTION_DIGIT.format(i / 1000.0d) + suffixSelected[level];
            } else {
                // 2 fraction digit
                return DF_TWO_FRACTION_DIGIT.format(i / 1000.0d) + suffixSelected[level];
            }
        }
        return i + suffixSelected[level];
    }
}
