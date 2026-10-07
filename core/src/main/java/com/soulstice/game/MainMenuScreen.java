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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MainMenuScreen implements Screen {

    private static final float W = 1024f;
    private static final float H = 768f;
    private static final Color BG = new Color(0.76f, 1f, 0.47f, 1f);

    private final SoulsticeGame game;

    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapes;
    private SpriteBatch batch;
    private BitmapFont font;

    // GAME LOGO
    private Texture logo;

    private UIButton startButton, settingsButton, quitButton;
    private UIButton confirmButton;

    private boolean askingName = false;
    private final StringBuilder nameInput = new StringBuilder();

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

        // LOAD GAME LOGO
        logo = new Texture(Gdx.files.internal("logo.png"));

        startButton    = new UIButton(W / 2f - 100f, 340f, 200f, 50f, "START");
        settingsButton = new UIButton(W / 2f - 100f, 270f, 200f, 50f, "SETTINGS");
        quitButton     = new UIButton(W / 2f - 100f, 200f, 200f, 50f, "QUIT");
        confirmButton  = new UIButton(W / 2f - 100f, 260f, 200f, 50f, "CONFIRM");

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyTyped(char c) {
                if (!askingName) return false;
                if (c == '\b') {
                    if (nameInput.length() > 0) nameInput.deleteCharAt(nameInput.length() - 1);
                } else if (c == '\r' || c == '\n') {
                    confirmName();
                } else if (c >= 32 && c < 127 && nameInput.length() < 16) {
                    nameInput.append(c);
                }
                return true;
            }
        });
    }

    private void confirmName() {
        String name = nameInput.toString().trim();
        if (name.length() > 1) {
            game.playerName = name;
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

        if (askingName) {
            renderNamePrompt();
        } else {
            renderMenu();
        }
    }

    private void renderMenu() {
        startButton.update(viewport);
        settingsButton.update(viewport);
        quitButton.update(viewport);

        if (startButton.isClicked()) {
            if (game.playerName == null || game.playerName.isEmpty()) {
                askingName = true;
            } else {
                game.showLevelMap();
            }
            return;
        }

        if (settingsButton.isClicked()) {
            game.showSettings(false);
            return;
        }

        if (quitButton.isClicked()) {
            Gdx.app.exit();
            return;
        }

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Color.BLACK);
        shapes.rect(W / 2f - 210f, 480f, 420f, 170f);
        shapes.end();

        // DRAW GAME LOGO
        batch.begin();

        float logoWidth = 500f;
        float logoHeight = 200f;

        batch.draw(
            logo,
            (W - logoWidth) / 2f,
            500f,
            logoWidth,
            logoHeight
        );

        batch.end();

        startButton.draw(shapes, batch, font);
        settingsButton.draw(shapes, batch, font);
        quitButton.draw(shapes, batch, font);
    }

    private void renderNamePrompt() {
        confirmButton.update(viewport);

        if (confirmButton.isClicked()) {
            confirmName();
            return;
        }

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Color.WHITE);
        shapes.rect(W / 2f - 150f, 340f, 300f, 45f);
        shapes.end();

        batch.begin();

        font.getData().setScale(2f);
        font.setColor(Color.BLACK);

        font.draw(
            batch,
            "What should we call you?",
            W / 2f - 180f,
            460f
        );

        font.getData().setScale(1.5f);

        String shown = nameInput.length() == 0 ? "ENTER NAME" : nameInput.toString();

        font.setColor(
            nameInput.length() == 0 ? Color.LIGHT_GRAY : Color.BLACK
        );

        font.draw(
            batch,
            shown,
            W / 2f - 140f,
            372f
        );

        batch.end();

        confirmButton.draw(shapes, batch, font);
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

        // DISPOSE LOGO
        logo.dispose();
    }
}