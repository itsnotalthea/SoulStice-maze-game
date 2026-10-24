package com.soulstice.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

public class UIButton {

    private static final Color NORMAL = new Color(1f, 1f, 1f, 1f);
    private static final Color HOVER  = new Color(0.85f, 0.85f, 0.85f, 1f);
    private static final float HOVER_SCALE = 1.06f;

    public final float x, y, width, height;
    public String label;
    private Texture texture;
    private boolean hovered = false;
    private float hoverScale = HOVER_SCALE;

    public UIButton(float x, float y, float width, float height, String label) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.label = label;
    }

    public void setTexture(Texture texture) {
    this.texture = texture;
    }

    public void setHoverScale(float hoverScale) {
        this.hoverScale = hoverScale;
    }

    public void update(Viewport viewport) {
        Vector2 v = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        hovered = v.x >= x && v.x <= x + width && v.y >= y && v.y <= y + height;
    }

    public boolean isClicked() {
        return hovered && Gdx.input.justTouched();
    }

    public void draw(ShapeRenderer sr, SpriteBatch sb, BitmapFont font) {
        if (texture != null) {
        float drawWidth = width * (hovered ? hoverScale : 1f);
        float drawHeight = height * (hovered ? hoverScale : 1f);

        sb.begin();
        sb.setColor(Color.WHITE);
        sb.draw(
            texture,
            x - (drawWidth - width) / 2f,
            y - (drawHeight - height) / 2f,
            drawWidth,
            drawHeight
        );

        sb.end();
        return;
        }

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

    public void draw(SpriteBatch sb, Texture texture) {
        sb.begin();
        float drawWidth = width * (hovered ? hoverScale : 1f);
        float drawHeight = height * (hovered ? hoverScale : 1f);
        sb.setColor(NORMAL);
        sb.draw(texture, x - (drawWidth - width) / 2f, y - (drawHeight - height) / 2f + (hovered ? 2f : 0f),
            drawWidth, drawHeight);
        sb.setColor(Color.WHITE);
        sb.end();
    }
}
