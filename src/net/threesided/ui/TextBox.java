package net.threesided.ui;

import java.awt.Color;
import java.awt.Graphics;

public class TextBox extends UIComponent {
    private int count;

    protected int maxLength = 18;
    protected boolean focused = false;
    protected String caption;
    protected String value = "";

    static final Color selectedCol = new Color(0, 64, 255, 200);
    static final Color deselectedCol = new Color(0, 16, 64, 200);

    TextBox(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    TextBox(int width, int height) {
        super(width, height);
    }

    public void append(char c) {
        if (!focused) return;
        if (c == 8) {
            if (value.length() > 0) value = value.substring(0, value.length() - 1);
        } else if ((Character.isLetterOrDigit(c) || c == '.') && value.length() < maxLength)
            value += c;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public void setText(String value) {
        this.value = value;
    }

    public String getText() {
        return value;
    }

    public void focus() {
        focused = true;
    }

    public void unfocus() {
        focused = false;
    }

    public void toggleFocus() {
        focused = !focused;
    }

    public boolean hasFocus() {
        return focused;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    protected void drawComponent(Graphics g) {
        if (focused) g.setColor(selectedCol);
        else g.setColor(deselectedCol);
        g.fillRect(abs_x, abs_y, width, height);

        g.setColor(Color.white);
        g.drawString(caption, abs_x - g.getFontMetrics().stringWidth(caption) - 5, abs_y + 15);
        g.drawString(value, abs_x + 3, abs_y + 17);

        if (focused && count++ % 50 > 25) {
            int width = g.getFontMetrics().stringWidth(value) + 5;
            g.drawLine(abs_x + width, abs_y + 1, abs_x + width, abs_y + 17);
        }
    }
}
