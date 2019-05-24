package fi.dy.masa.minihud.gui.widgets;

import java.util.List;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.ButtonOnOff;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.widgets.WidgetListEntryBase;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.minihud.gui.GuiShapeEditor;
import fi.dy.masa.minihud.renderer.shapes.ShapeBase;
import fi.dy.masa.minihud.renderer.shapes.ShapeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;

public class WidgetShapeEntry extends WidgetListEntryBase<ShapeBase>
{
    private final WidgetListShapes parent;
    private final ShapeBase shape;
    private final List<String> hoverLines;
    private final Minecraft mc;
    private final boolean isOdd;
    private final int buttonsStartX;

    public WidgetShapeEntry(int x, int y, int width, int height, float zLevel, boolean isOdd,
            ShapeBase shape, int listIndex, WidgetListShapes parent, Minecraft mc)
    {
        super(x, y, width, height, zLevel, shape, listIndex);

        this.shape = shape;
        this.hoverLines = shape.getWidgetHoverLines();
        this.mc = mc;
        this.isOdd = isOdd;
        this.parent = parent;
        y += 1;

        int posX = x + width - 2;

        posX -= this.addButton(posX, y, ButtonListener.Type.REMOVE);
        posX -= this.createButtonOnOff(posX, y, this.shape.isEnabled(), ButtonListener.Type.ENABLED);
        posX -= this.addButton(posX, y, ButtonListener.Type.CONFIGURE);

        this.buttonsStartX = posX;
    }

    protected int addButton(int x, int y, ButtonListener.Type type)
    {
        ButtonGeneric button = ButtonGeneric.createGeneric(x, y, -1, true, type.getDisplayName());
        this.addButton(button, new ButtonListener(type, this));

        return button.getWidth() + 1;
    }

    private int createButtonOnOff(int xRight, int y, boolean isCurrentlyOn, ButtonListener.Type type)
    {
        ButtonOnOff button = ButtonOnOff.createOnOff(xRight, y, -1, true, type.getTranslationKey(), isCurrentlyOn);
        this.addButton(button, new ButtonListener(type, this));

        return button.getWidth() + 2;
    }

    @Override
    public void render(int mouseX, int mouseY, boolean selected)
    {
        GlStateManager.color(1, 1, 1, 1);

        boolean shapeSelected = ShapeManager.INSTANCE.getSelectedShape() == this.entry;

        // Draw a lighter background for the hovered and the selected entry
        if (selected || shapeSelected || this.isMouseOver(mouseX, mouseY))
        {
            GuiBase.drawRect(this.x, this.y, this.x + this.width, this.y + this.height, 0x70FFFFFF);
        }
        else if (this.isOdd)
        {
            GuiBase.drawRect(this.x, this.y, this.x + this.width, this.y + this.height, 0x20FFFFFF);
        }
        // Draw a slightly lighter background for even entries
        else
        {
            GuiBase.drawRect(this.x, this.y, this.x + this.width, this.y + this.height, 0x50FFFFFF);
        }

        if (shapeSelected)
        {
            RenderUtils.drawOutline(this.x, this.y, this.width, this.height, 0xFFE0E0E0);
        }

        String name = this.shape.getType().getDisplayName();
        this.mc.fontRenderer.drawString(name, this.x + 4, this.y + 7, 0xFFFFFFFF);

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.disableBlend();

        super.render(mouseX, mouseY, selected);

        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
    }

    @Override
    public void postRenderHovered(int mouseX, int mouseY, boolean selected)
    {
        super.postRenderHovered(mouseX, mouseY, selected);

        if (mouseX >= this.x && mouseX < this.buttonsStartX && mouseY >= this.y && mouseY <= this.y + this.height)
        {
            RenderUtils.drawHoverText(mouseX, mouseY, this.hoverLines);
        }
    }

    private static class ButtonListener implements IButtonActionListener
    {
        private final Type type;
        private final WidgetShapeEntry widget;

        public ButtonListener(Type type, WidgetShapeEntry widget)
        {
            this.type = type;
            this.widget = widget;
        }

        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton)
        {
            if (this.type == Type.CONFIGURE)
            {
                Minecraft mc = Minecraft.getMinecraft();
                GuiShapeEditor gui = new GuiShapeEditor(this.widget.shape);
                gui.setParent(mc.currentScreen);
                mc.displayGuiScreen(gui);
            }
            else if (this.type == Type.ENABLED)
            {
                this.widget.shape.toggleEnabled();
                this.widget.parent.refreshEntries();
            }
            else if (this.type == Type.REMOVE)
            {
                ShapeManager.INSTANCE.removeShape(this.widget.shape);
                this.widget.parent.refreshEntries();
            }
        }

        public enum Type
        {
            CONFIGURE   ("minihud.gui.button.configure"),
            ENABLED     ("minihud.gui.button.shape_entry.enabled"),
            REMOVE      ("minihud.gui.button.remove");

            private final String translationKey;

            private Type(String translationKey)
            {
                this.translationKey = translationKey;
            }

            public String getTranslationKey()
            {
                return this.translationKey;
            }
            
            public String getDisplayName(Object... args)
            {
                return I18n.format(this.translationKey, args);
            }
        }
    }
}
