package com.holysweet.questshop.client.screen;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.List;

public class ShopList extends ObjectSelectionList<ShopListEntry> {
    public static final Logger LOGGER = LogUtils.getLogger();
    private boolean thumbDrag;
    private double thumbGrabOffset;

    public ShopList(Minecraft mc, int width, int listHeight, int top, int itemHeight) {
        super(mc, width, listHeight, top, itemHeight);
    }

    /** Public helper since addEntry is protected. */
    public void add(ShopListEntry entry) {
        this.addEntry(entry);
    }

    public void setEntries(List<ShopListEntry> entries) {
        this.children().clear();
        for (ShopListEntry e : entries) this.addEntry(e);
        this.setScrollAmount(0);
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        /**
         * NOTE: Neoforge 21.1.* has broken mouseDragged, onDrag, mouseMoved events.
         * Handling scrolldragging in render until otherwise fixable.
          */

        // Guard against empty list or no scroll amount
        if( this.thumbDrag ) {
            if( this.getMaxPosition() <= 0 || this.getMaxScroll() == 0) {
                this.thumbDrag = false;
            }
        }

        if( this.thumbDrag ) {
            var newScroll = getNewScroll(mouseY);
            this.setScrollAmount(newScroll);
            LOGGER.debug("[ShopList] renderWidget() => isDragging= {}, mouseY={}, newScroll={}", this.thumbDrag, mouseY, newScroll);
        }

        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }

    private double getNewScroll(int mouseY) {
        var scrollTrackSpan = this.getHeight() - this.getThumbHeight();

        var newThumbTop = Mth.clamp(
                mouseY - this.thumbGrabOffset,
                this.getY(),
                this.getY() + scrollTrackSpan
            );

        var ratio = (newThumbTop - this.getY()) / scrollTrackSpan;

        return Mth.clamp(
                ratio * this.getMaxScroll(),
                0,
                this.getMaxScroll()
            );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        var over = this.isMouseOver(mouseX, mouseY);
        LOGGER.debug("[ShopList] mouseClicked({}, {}, {}) => over={}, focused={}", mouseX, mouseY, button, over, this.isFocused());

        if( this.checkHitScrollbar(mouseX, mouseY) ) {
            if( this.checkHitThumb(mouseX, mouseY) ) {
                this.thumbDrag = true;
                this.thumbGrabOffset = mouseY - this.getThumbTop();
                return true;
            } else {
                if(this.pageScroll(mouseX, mouseY)) {
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if( this.thumbDrag ) {
            this.thumbDrag = false;
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean checkHitScrollbar(double mouseX, double mouseY) {
        var scrollLeft = this.getScrollbarPosition();
        var scrollRight = scrollLeft + SCROLLBAR_WIDTH;
        var scrollTop = this.getY();
        var scrollBottom = this.getBottom();

        LOGGER.debug("[ShopList] checkHitScrollbar({}, {}) => left={}, right={}, top={}, bottom={}", mouseX, mouseY, scrollLeft, scrollRight, scrollTop, scrollBottom);

        if (mouseX >= scrollLeft && mouseX < scrollRight && mouseY >= scrollTop && mouseY < scrollBottom) {
            LOGGER.debug("[ShopList] checkHitScrollbar() => scrollbar hit");
            return true;
        } else {
            LOGGER.debug("[ShopList] checkHitScrollbar() => scrollbar missed");
            return false;
        }
    }

    /**
     * Helper method to check if a click was on the scrollbar thumb.
     * Should only be called when the scrollbar is hit to prevent excessive checking.
     * @param mouseX Mouse X position
     * @param mouseY Mouse Y position
     * @return true when the click was on the thumb
     */
    private boolean checkHitThumb(double mouseX, double mouseY) {
        var thumbLeft = this.getScrollbarPosition();
        var thumbRight = thumbLeft + SCROLLBAR_WIDTH;

        var thumbHeight = this.getThumbHeight();
        var thumbYPos = this.getThumbTop();

        LOGGER.debug("[ShopList] checkHitThumb({}, {}) => left={}, right={}, height={}, YPos={}", mouseX, mouseY, thumbLeft, thumbRight, thumbHeight, thumbYPos);

        var thumbBottom = thumbYPos + thumbHeight;

        if (mouseX >= thumbLeft && mouseX < thumbRight && mouseY >= thumbYPos && mouseY < thumbBottom) {
            LOGGER.debug("[ShopList] checkHitThumb() => thumb hit");
            return true;
        } else {
            LOGGER.debug("[ShopList] checkHitThumb() => thumb missed");
            return false;
        }
    }

    /**
     * Helper method to perform a page scroll.
     * Should only be called when the scrollbar is hit, but not the thumb, to prevent excessive checking.
     * @param mouseX Mouse X position
     * @param mouseY Mouse Y position
     * @return true when page scroll has been applied
     */
    private boolean pageScroll(double mouseX, double mouseY) {
        LOGGER.debug("[ShopList] pageScroll({}, {}) called", mouseX, mouseY);

        // Validate we hit the scrollbar and not the thumb, otherwise return false.
        if(!this.checkHitScrollbar(mouseX, mouseY) || this.checkHitThumb(mouseX, mouseY)) return false;

        // Guard against not having a scroll, or an empty list
        if(this.getMaxScroll() == 0 || this.getMaxPosition() <= 0) return false;

        var thumbTop = this.getThumbTop();
        var thumbHeight = this.getThumbHeight();

        int direction;

        if( mouseY < thumbTop ) {
            direction = -1; // Page up
        } else if ( mouseY > thumbTop + thumbHeight) {
            direction = 1; // Page down
        } else {
            // This should not happen
            return false;
        }

        // Calculate scroll amount, range 0..MaxScroll
        double scrollAmount = Mth.clamp(
                this.getScrollAmount() + this.getPageSize() * direction,
                0,
                this.getMaxScroll()
                );

        LOGGER.debug("[ShopList] pageScroll() => direction={}, scrollAmount={}", direction, scrollAmount);

        this.setScrollAmount(scrollAmount);

        return true;
    }

    private int getThumbTop() {
        // Guard against if there's no scroll amount.
        if (this.getMaxScroll() == 0) return this.getY();

        return (int) (this.getScrollAmount() * (this.getHeight() - this.getThumbHeight()) / getMaxScroll() + this.getY());
    }

    private int getThumbHeight() {
        // Guard against if the content is empty.
        if (this.getMaxPosition() == 0) return 32;

        return Mth.clamp(
                (this.getHeight() * this.getHeight()) / this.getMaxPosition(),
                32,
                getHeight() - 8);
    }

    private int getPageSize() {
        int size = this.getHeight() - this.itemHeight;
        if( size <= 0 ) {
            return this.itemHeight;
        } else {
            return size;
        }
    }

}
