package com.soulstice.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

public class UIButton {

    private static final Color NORMAL = new Color(1f, 1f, 1f, 1f);
    private static final Color HOVER  = new Color(0.85f, 0.85f, 0.85f, 1f);

    public final float x, y, width, height;
    public String label;
    private boolean hovered = false;

    public UIButton(float x, float y, float width, float height, String label) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.label = label;
    }

    public void update(Viewport viewport) {
        Vector2 v = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        hovered = v.x >= x && v.x <= x + width && v.y >= y && v.y <= y + height;
    }

    public boolean isClicked() {
        return hovered && Gdx.input.justTouched();
    }

    public void draw(ShapeRenderer sr, SpriteBatch sb, BitmapFont font) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(hovered ? HOVER : NORMAL);
        sr.rect(x, y, width, height);
        sr.end();

        sb.begin();
        font.getData().setScale(1f);
        font.setColor(Color.BLACK);
        GlyphLayout layout = new GlyphLayout(font, label);
        font.draw(sb, label, x + (width - layout.width) / 2f, y + (height + layout.height) / 2f);
        sb.end();
    }
}
