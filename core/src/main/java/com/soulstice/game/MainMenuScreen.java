package com.soulstice.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MainMenuScreen implements Screen {

    private static final float W = 1920f;
    private static final float H = 1080f;

    private static final Color BG = new Color(0.22f, 0.38f, 0f, 1f);
    private static final Color LOGO_FRAME = new Color(0.12f, 0.25f, 0.12f, 1f);
    private static final Color LOGO_SURFACE = new Color(0.94f, 0.91f, 0.73f, 1f);

    private final SoulsticeGame game;

    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapes;
    private SpriteBatch batch;
    private BitmapFont font;

    // GAME LOGO
    private Texture logo;
    private Texture background;

    private UIButton confirmButton;

    private boolean askingName = false;
    private final StringBuilder nameInput = new StringBuilder();

    private float cursorAccum = 0f;
    private boolean cursorOn = true;

    public MainMenuScreen(SoulsticeGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(W, H, camera);
        camera.position.set(W / 2f, H / 2f, 0);
        camera.update();

        shapes = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();

        background = new Texture(Gdx.files.internal("MainMenu_Background.png"));
        background.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        confirmButton = new UIButton(W / 2f - 150f, 300f, 300f, 78f, "CONFIRM");

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyTyped(char c) {
                if (!askingName) return false;
                if (c == '\b') {
                    if (nameInput.length() > 0) nameInput.deleteCharAt(nameInput.length() - 1);
                    // Reset cursor when name becomes empty
                    if (nameInput.length() == 0) {
                        cursorOn = true;
                        cursorAccum = 0f;
                    }
                } else if (c == '\r' || c == '\n') {
                    confirmName();
                } else if (c >= 32 && c < 127 && nameInput.length() < 16) {
                    nameInput.append(c);
                    // Ensure cursor is on when a character is typed
                    cursorOn = true;
                    cursorAccum = 0f;
                }
                return true;
            }
        });
    }

    private void confirmName() {
        String name = nameInput.toString().trim();
        if (name.length() > 1) {
            game.playerName = name;
            SaveSystem.save(game);
            game.showLevelMap();
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(BG.r, BG.g, BG.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();

        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        // Blinking cursor
        cursorAccum += delta;
        if (cursorAccum > 0.5f) {
            cursorAccum -= 0.5f;
            cursorOn = !cursorOn;
        }

        // Menu BG
        batch.begin();
        batch.draw(background, 0f, 0f, W, H);
        batch.end();

        if (askingName) {
            renderNamePrompt();
        } else {
            renderMenu();
        }
    }

    private void renderMenu() {
        if (!Gdx.input.justTouched()) return;
        Vector2 pointer = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

        if (contains(pointer, 770f, 485f, 365f, 115f)) {
            if (game.playerName == null || game.playerName.isEmpty()) {
                askingName = true;
                cursorAccum = 0f;
                cursorOn = true;
            } else {
                game.showLevelMap();
            }
            return;
        }

        if (contains(pointer, 570f, 365f, 365f, 115f)) {
            game.showSettings(false);
            return;
        }

        if (contains(pointer, 970f, 365f, 365f, 115f)) {
            game.showAchievements();
            return;
        }

        if (contains(pointer, 570f, 230f, 365f, 115f)) {
            game.showTutorial();
            return;
        }

        if (contains(pointer, 970f, 230f, 365f, 115f)) {
            Gdx.app.exit();
        }
    }

    private void renderNamePrompt() {
        confirmButton.update(viewport);

        if (confirmButton.isClicked()) {
            confirmName();
            return;
        }

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Color.WHITE);
        shapes.rect(W / 2f - 310f, 450f, 620f, 90f);
        shapes.end();

        batch.begin();

        font.getData().setScale(3f);
        font.setColor(Color.BLACK);

        font.draw(
            batch,
            "What should we call you?",
            W / 2f - 290f,
            720f
        );

        font.getData().setScale(2f);

        String shown = nameInput.length() == 0 ? "ENTER NAME"
            : nameInput.toString() + (cursorOn ? "|" : "");

        font.setColor(
            nameInput.length() == 0 ? Color.LIGHT_GRAY : Color.BLACK
        );

        font.draw(
            batch,
            shown,
            W / 2f - 260f,
            510f
        );

        batch.end();

        confirmButton.draw(shapes, batch, font);
    }

    private static boolean contains(Vector2 point, float x, float y, float width, float height) {
        return point.x >= x && point.x <= x + width && point.y >= y && point.y <= y + height;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();

        background.dispose();
    }
}