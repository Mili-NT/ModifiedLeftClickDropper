package com.modifiedleftclickdropper;

import com.google.inject.Inject;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ItemOverlay extends WidgetItemOverlay
{

    private final ModifiedLeftClickDropper modifiedLeftClickDropper;
    private final ItemManager itemManager;

    @Inject
    private ItemOverlay(ModifiedLeftClickDropper modifiedLeftClickDropper, ItemManager itemManager)
    {
        this.modifiedLeftClickDropper = modifiedLeftClickDropper;
        this.itemManager = itemManager;

        showOnInventory();
    }
    @Override
    public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem itemWidget)
    {
        Color color = new Color(0, 0, 0, 0);
        String itemName = itemManager.getItemComposition(itemId).getMembersName();
        if (modifiedLeftClickDropper.isItemNameIncluded(itemName)) {
            color = new Color(255, 0 ,0);
        }

        Rectangle bounds = itemWidget.getCanvasBounds();

        final BufferedImage outline = itemManager.getItemOutline(itemId, itemWidget.getQuantity(), color);
        graphics.drawImage(outline, (int)bounds.getX(), (int)bounds.getY(), null);
    }
}
