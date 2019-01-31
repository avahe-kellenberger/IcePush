package net.threesided.game;

import static net.threesided.shared.Constants.*;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import net.threesided.graphics2d.SpriteLoader;

public class Object2D extends GameObject {
    public int x, y, width, height;
    private BufferedImage sprite;

    private static BufferedImage tree = SpriteLoader.getSprite("images/tree.png");
    private static BufferedImage snowman = SpriteLoader.getSprite("images/snowman.png");
    private static BufferedImage ball = SpriteLoader.getSprite("images/ball.png");

    public Object2D(String spriteName, int type) {
        super(type);
        sprite = SpriteLoader.getSprite(spriteName);
        width = sprite.getWidth();
        height = sprite.getHeight();
    }

    public Object2D(int type) {
        super(type);
        if (type == TREE) sprite = tree;
        if (type == SNOWMAN) sprite = snowman;
        if (type == BALL) sprite = ball;
        if (sprite == null) throw new IllegalArgumentException();
        width = sprite.getWidth();
        height = sprite.getHeight();
    }

    public void draw(Graphics g) {
        g.drawImage(sprite, getScreenX(), getScreenY(), null);
    }

    public int getScreenX() {
        return x;
    }

    public int getScreenY() {
        return y;
    }
}
