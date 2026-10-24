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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class TutorialScreen implements Screen {

    private static final float W = 1920f;
    private static final float H = 1080f;
    private static final Color PANEL = new Color(0.96f, 0.90f, 0.63f, 0.96f);
    private static final Color INK = new Color(0.15f, 0.24f, 0.08f, 1f);
    private static final String[][] PAGES = {
        {
            "THE MAZE",
            "Guide BODY and SOUL into the same maze cell, or cross into each other's cell, to win.",
            "Use the arrow keys or W, A, S, D to move BODY. SOUL moves in the opposite direction.",
            "Hold a direction to keep moving. Walls stop each character independently."
        },
        {
            "THE TIMER",
            "Bring BODY and SOUL together before the countdown reaches zero.",
            "Each level starts with its own time limit, shown in the gameplay screen.",
            "The limit decreases gradually from 35 seconds on Level 1 to 20 seconds on Level 21.",
            "If time runs out, the attempt ends. Choose Play Again to restart that level."
        },
        {
            "21 LEVELS",
            "Win a maze to unlock the next level.",
            "Choose any unlocked level from the level map. Your unlocked-level progress is saved.",
            "Complete all 21 levels to master Soulstice."
        }
    };

    private final SoulsticeGame game;

    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapes;
    private SpriteBatch batch;
    private BitmapFont font;
    private Texture background;
    private int page;

    public TutorialScreen(SoulsticeGame game) {
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
        if (Gdx.files.internal("MainMenu_Background.png").exists()) {
            background = new Texture(Gdx.files.internal("MainMenu_Background.png"));
        } else {
            Gdx.app.error("Soulstice", "Required tutorial background asset is missing: MainMenu_Background.png");
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.08f, 0.18f, 0.03f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();
        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        if (background != null) {
            batch.begin();
            batch.draw(background, 0f, 0f, W, H);
            batch.end();
        }

        Vector2 pointer = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
            || (Gdx.input.justTouched() && contains(pointer, 100f, 40f, 280f, 100f))) {
            game.showMainMenu();
            return;
        }
        if (Gdx.input.justTouched() && contains(pointer, 1350f, 40f, 260f, 100f)) {
            page = Math.min(PAGES.length - 1, page + 1);
        } else if (Gdx.input.justTouched() && contains(pointer, 300f, 40f, 260f, 100f)) {
            page = Math.max(0, page - 1);
        }

        drawPage();
    }

    private void drawPage() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(PANEL);
        shapes.rect(250f, 190f, 1420f, 700f);
        shapes.end();

        batch.begin();
        font.setColor(INK);
        font.getData().setScale(2.5f);
        String title = PAGES[page][0];
        GlyphLayout titleLayout = new GlyphLayout(font, title);
        font.draw(batch, title, (W - titleLayout.width) / 2f, 815f);

        font.getData().setScale(1.45f);
        float y = 700f;
        for (int i = 1; i < PAGES[page].length; i++) {
            String text = PAGES[page][i];
            font.draw(batch, text, 350f, y, 1220f, Align.center, true);
            y -= 125f;
        }

        batch.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(0.24f, 0.39f, 0.12f, 1f);
        shapes.rect(250f, 190f, 1420f, 700f);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.19f, 0.30f, 0.09f, 0.95f);
        shapes.rect(100f, 40f, 280f, 80f);
        if (page > 0) shapes.rect(410f, 40f, 280f, 80f);
        if (page < PAGES.length - 1) shapes.rect(1350f, 40f, 280f, 80f);
        shapes.end();

        batch.begin();
        font.getData().setScale(1.1f);
        font.setColor(PANEL);
        drawCentered("BACK", 240f, 90f);
        if (page > 0) drawCentered("PREVIOUS", 550f, 90f);
        if (page < PAGES.length - 1) drawCentered("NEXT", 1490f, 90f);
        batch.end();
    }

    private void drawCentered(String text, float centerX, float y) {
        GlyphLayout layout = new GlyphLayout(font, text);
        font.draw(batch, text, centerX - layout.width / 2f, y);
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
        if (background != null) background.dispose();
    }
}
