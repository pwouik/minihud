package fi.dy.masa.minihud.gui.widget;

import fi.dy.masa.malilib.gui.BaseScreen;
import fi.dy.masa.malilib.gui.util.GuiUtils;
import fi.dy.masa.malilib.gui.widget.button.GenericButton;
import fi.dy.masa.malilib.gui.widget.button.OnOffButton;
import fi.dy.masa.malilib.gui.widget.button.OnOffStyle;
import fi.dy.masa.malilib.gui.widget.list.DataListWidget;
import fi.dy.masa.malilib.gui.widget.list.entry.BaseDataListEntryWidget;
import fi.dy.masa.malilib.render.text.StyledTextLine;
import fi.dy.masa.minihud.gui.GuiShapeEditor;
import fi.dy.masa.minihud.renderer.shapes.ShapeBase;
import fi.dy.masa.minihud.renderer.shapes.ShapeManager;

public class WidgetShapeEntry extends BaseDataListEntryWidget<ShapeBase>
{
    private final ShapeBase shape;
    private final GenericButton configureButton;
    private final GenericButton toggleButton;
    private final GenericButton removeButton;
    private final int buttonsStartX;

    public WidgetShapeEntry(int x, int y, int width, int height, int listIndex,
                            int originalListIndex, ShapeBase shape, DataListWidget<ShapeBase> listWidget)
    {
        super(x, y, width, height, listIndex, originalListIndex, shape, listWidget);

        this.shape = shape;

        this.setText(StyledTextLine.of(shape.getDisplayName()));

        this.configureButton = new GenericButton(x, y + 1, -1, true, "minihud.gui.button.configure");
        this.toggleButton = new OnOffButton(x, y + 1, -1, 20, OnOffStyle.SLIDER_ON_OFF, this.shape::isEnabled, null);
        this.toggleButton.setRightAlign(true, x, true);
        this.removeButton = new GenericButton(x, y + 1, -1, true, "minihud.gui.button.remove");

        this.configureButton.setActionListener(() -> {
            GuiShapeEditor gui = new GuiShapeEditor(this.shape);
            gui.setParent(GuiUtils.getCurrentScreen());
            BaseScreen.openScreen(gui);
        });

        this.toggleButton.setActionListener(() -> {
            this.shape.toggleEnabled();
            this.listWidget.reCreateListEntryWidgets();
        });

        this.removeButton.setActionListener(() -> {
            ShapeManager.INSTANCE.removeShape(this.shape);
            this.listWidget.refreshEntries();
        });

        this.buttonsStartX = x + width - this.configureButton.getWidth() - this.toggleButton.getWidth() - this.removeButton.getWidth() - 6;

        this.setHoverStringProvider("shape_info", shape::getWidgetHoverLines);
        this.setRenderNormalBackground(true);
    }

    @Override
    public void updateSubWidgetsToGeometryChanges()
    {
        super.updateSubWidgetsToGeometryChanges();

        int x = this.getX() + this.getWidth() - 2;

        this.removeButton.setRightX(x);
        x = this.removeButton.getX() - 2;

        this.toggleButton.setRightX(x);
        x = this.toggleButton.getX() - 2;

        this.configureButton.setRightX(x);
    }

    @Override
    public void reAddSubWidgets()
    {
        super.reAddSubWidgets();

        this.addWidget(this.configureButton);
        this.addWidget(this.toggleButton);
        this.addWidget(this.removeButton);
    }

    @Override
    public boolean canHoverAt(int mouseX, int mouseY, int mouseButton)
    {
        return mouseX < this.buttonsStartX && super.canHoverAt(mouseX, mouseY, mouseButton);
    }

    @Override
    protected boolean isSelected()
    {
        return ShapeManager.INSTANCE.getSelectedShape() == this.data;
    }
}
