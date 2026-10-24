package com.soulstice.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class AchievementsScreen implements Screen {

    private static final float W = 1920f;
    private static final float H = 1080f;
    private static final Color LOCKED_SHADE = new Color(0.08f, 0.12f, 0.03f, 0.72f);
    private static final Color STATUS_BAND = new Color(0.08f, 0.20f, 0.05f, 0.84f);
    private static final Color STATUS_TEXT = new Color(1f, 0.95f, 0.62f, 1f);

    private final SoulsticeGame game;

    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapes;
    private SpriteBatch batch;
    private BitmapFont font;
    private Texture[] pages;
    private int page;

    public AchievementsScreen(SoulsticeGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(W, H, camera);
        camera.position.set(W / 2f, H / 2f, 0f);
        camera.update();
        shapes = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        pages = new Texture[] {
            loadTexture("achievementsPanel1.png"),
            loadTexture("achievementsPanel2.png")
        };
    }

    private Texture loadTexture(String asset) {
        if (!Gdx.files.internal(asset).exists()) {
            Gdx.app.error("Soulstice", "Required achievement asset is missing: " + asset);
            return null;
        }
        return new Texture(Gdx.files.internal(asset));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.12f, 0.22f, 0.04f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();
        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        Texture background = pages[page];
        if (background != null) {
            batch.begin();
            batch.draw(background, 0f, 0f, W, H);
            batch.end();
        } else {
            drawFallbackBackground();
        }

        Vector2 pointer = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        boolean touched = Gdx.input.justTouched();
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.showMainMenu();
            return;
        }
        if (touched && contains(pointer, 95f, 865f, 125f, 130f)) {
            if (page == 0) game.showMainMenu();
            else page = 0;
            return;
        }
        if (touched && page == 0 && contains(pointer, 1700f, 865f, 130f, 130f)) {
            page = 1;
            return;
        }
        if (touched && contains(pointer, 45f, 25f, 230f, 76f)) {
            game.showMainMenu();
            return;
        }

        drawStatuses();
        drawBackLabel();
    }

    private void drawStatuses() {
        Achievement[] visible = page == 0
            ? new Achievement[] {
                Achievement.FIRST_WIN, Achievement.STREAK, Achievement.TIME_BEATER
            }
            : new Achievement[] {
                Achievement.SOULSTICE_LEGEND, Achievement.ALL_ACHIEVEMENTS
            };
        float[][] cards = page == 0
            ? new float[][] {
                {155f, 138f, 506f, 670f}, {707f, 138f, 506f, 670f}, {1259f, 138f, 506f, 670f}
            }
            : new float[][] {
                {431f, 150f, 506f, 670f}, {982f, 150f, 506f, 670f}
            };

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < visible.length; i++) {
            float[] card = cards[i];
            if (game.isAchievementUnlocked(visible[i])) {
                shapes.setColor(STATUS_BAND);
                shapes.rect(card[0] + 22f, card[1] + 18f, card[2] - 44f, 44f);
            } else {
                shapes.setColor(LOCKED_SHADE);
                shapes.rect(card[0], card[1], card[2], card[3]);
            }
        }
        shapes.end();

        batch.begin();
        font.getData().setScale(1.35f);
        font.setColor(STATUS_TEXT);
        for (int i = 0; i < visible.length; i++) {
            float[] card = cards[i];
            String status = game.isAchievementUnlocked(visible[i]) ? "UNLOCKED" : "LOCKED";
            GlyphLayout layout = new GlyphLayout(font, status);
            float statusY = game.isAchievementUnlocked(visible[i])
                ? card[1] + 50f
                : card[1] + card[3] / 2f + layout.height / 2f;
            font.draw(batch, status, card[0] + (card[2] - layout.width) / 2f, statusY);
        }
        batch.end();
    }

    private void drawFallbackBackground() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.08f, 0.22f, 0.04f, 1f);
        shapes.rect(0f, 0f, W, H);
        shapes.end();
        batch.begin();
        font.getData().setScale(2.4f);
        font.setColor(STATUS_TEXT);
        font.draw(batch, "ACHIEVEMENTS", 780f, 930f);
        batch.end();
    }

    private void drawBackLabel() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.20f, 0.28f, 0.08f, 0.9f);
        shapes.rect(45f, 25f, 230f, 76f);
        shapes.end();
        batch.begin();
        font.getData().setScale(1.2f);
        font.setColor(STATUS_TEXT);
        font.draw(batch, "MAIN MENU", 77f, 73f);
        batch.end();
    }

    private static boolean contains(Vector2 point, float x, float y, float width, float height) {
        return point.x >= x && point.x <= x + width && point.y >= y && point.y <= y + height;
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
        for (Texture texture : pages) {
            if (texture != null) texture.dispose();
        }
    }
}
