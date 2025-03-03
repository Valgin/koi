package ru.effectivegroup.client.algoil;

import java.util.Objects;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

/* loaded from: trading-client-app-1.47.12.jar:ru/effectivegroup/client/algoil/DragResizer.class */
public class DragResizer {
    private static final int RESIZE_MARGIN = 4;
    private final Region region;
    private double x;
    private boolean initMinWidth;
    private boolean draggableZoneX;
    private boolean dragging;
    private double initialMinWidth;
    private double initialMaxWidth;

    private DragResizer(Region aRegion) {
        this.region = aRegion;
        this.initialMinWidth = this.region.getMinWidth();
        this.initialMaxWidth = this.region.getMaxWidth();
    }

    public static void makeResizable(Region region) {
        DragResizer resizer = new DragResizer(region);
        Objects.requireNonNull(resizer);
        region.setOnMousePressed(resizer::mousePressed);
        Objects.requireNonNull(resizer);
        region.setOnMouseDragged(resizer::mouseDragged);
        Objects.requireNonNull(resizer);
        region.setOnMouseMoved(resizer::mouseOver);
        Objects.requireNonNull(resizer);
        region.setOnMouseReleased(resizer::mouseReleased);
    }

    protected void mouseReleased(MouseEvent event) {
        this.dragging = false;
        this.region.setCursor(Cursor.DEFAULT);
    }

    protected void mouseOver(MouseEvent event) {
        if (isInDraggableZone(event) || this.dragging) {
            if (this.draggableZoneX) {
                this.region.setCursor(Cursor.E_RESIZE);
                return;
            }
            return;
        }
        this.region.setCursor(Cursor.DEFAULT);
    }

    protected boolean isInDraggableZone(MouseEvent event) {
        this.draggableZoneX = event.getX() > this.region.getWidth() - 4.0d;
        return this.draggableZoneX;
    }

    protected void mouseDragged(MouseEvent event) {
        if (this.dragging && this.draggableZoneX) {
            double mousex = event.getX();
            double newWidth = this.region.getMinWidth() + (mousex - this.x);
            if (newWidth > this.initialMinWidth && newWidth < this.initialMaxWidth) {
                this.region.setMinWidth(newWidth);
                this.region.setPrefWidth(newWidth);
                this.region.setMaxWidth(newWidth);
                this.x = mousex;
            }
        }
    }

    protected void mousePressed(MouseEvent event) {
        if (!isInDraggableZone(event)) {
            return;
        }
        this.dragging = true;
        if (!this.initMinWidth) {
            this.region.setMinWidth(this.region.getWidth());
            this.initMinWidth = true;
        }
        this.x = event.getX();
    }
}
