package net.threesided.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

public class Label extends UIComponent {
    protected String text;
    protected Color color = Color.white;
    protected Font font = new Font("Arial", Font.PLAIN, 20);

    Label() {
        super(0, 0);
    }

    Label(String text) {
        super(0, 0);
        setText(text);
    }

    public void setText(String text) {
        this.text = text;
        setSize(0, 0);
    }

    public String getText() {
        return text;
    }

    public void setFont(Font font) {
        this.font = font;
        setSize(0, 0);
    }

    public Font getFont() {
        return font;
    }

    protected void drawComponent(Graphics g) {
        if ((width == 0) && (height == 0)) {
            g.setFont(font);
            int new_width = g.getFontMetrics().stringWidth(text);
            int new_height = g.getFontMetrics().getHeight();
            setSize(new_width, new_height);
        }
        g.setColor(color);
        g.setFont(font);
        g.drawString(text, abs_x, abs_y);
    }
}
