package com.soulstice.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class SettingsScreen implements Screen {

    private static final float W = 1024f;
    private static final float H = 768f;

    private static final Color LIGHT_BG = new Color(0.76f, 1f, 0.47f, 1f);
    private static final Color DARK_BG  = new Color(0.14f, 0.16f, 0.12f, 1f);
    private static final Color BOX      = new Color(1f, 0.95f, 0.78f, 1f);

    private static final float SLIDER_X = 560f;
    private static final float SLIDER_Y = 505f;
    private static final float SLIDER_W = 260f;

    private final SoulsticeGame game;
    private final boolean fromPause;

    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapes;
    private SpriteBatch batch;
    private BitmapFont font;

    private UIButton themeButton, soundButton, renameButton, devInfoButton, backButton;
    private UIButton renameConfirm, renameCancel, devInfoClose;

    private boolean renaming = false;
    private boolean showDevInfo = false;
    private boolean draggingSlider = false;
    private final StringBuilder nameInput = new StringBuilder();

    public SettingsScreen(SoulsticeGame game, boolean fromPause) {
        this.game = game;
        this.fromPause = fromPause;
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

        themeButton   = new UIButton(SLIDER_X, 570f, 160f, 40f, "");
        soundButton   = new UIButton(SLIDER_X, 430f, 160f, 40f, "");
        renameButton  = new UIButton(SLIDER_X, 360f, 160f, 40f, "RENAME");
        devInfoButton = new UIButton(SLIDER_X, 290f, 160f, 40f, "DEVELOPER INFO");
        backButton    = new UIButton(W / 2f - 100f, 170f, 200f, 45f, "BACK");

        renameConfirm = new UIButton(W / 2f - 170f, 300f, 150f, 42f, "CONFIRM");
        renameCancel  = new UIButton(W / 2f + 20f, 300f, 150f, 42f, "CANCEL");
        devInfoClose  = new UIButton(W / 2f - 75f, 250f, 150f, 42f, "CLOSE");

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyTyped(char c) {
                if (!renaming) return false;
                if (c == '\b') {
                    if (nameInput.length() > 0) nameInput.deleteCharAt(nameInput.length() - 1);
                } else if (c == '\r' || c == '\n') {
                    confirmRename();
                } else if (c >= 32 && c < 127 && nameInput.length() < 16) {
                    nameInput.append(c);
                }
                return true;
            }
        });
    }

    private void confirmRename() {
        String name = nameInput.toString().trim();
        if (name.length() > 1) {
            game.playerName = name;
            renaming = false;
        }
    }

    private void goBack() {
        if (fromPause) game.showPause();
        else game.showMainMenu();
    }

    private Vector2 mouse() {
        return viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
    }

    @Override
    public void render(float delta) {
        GameSettings s = game.settings;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (renaming) renaming = false;
            else if (showDevInfo) showDevInfo = false;
            else { goBack(); return; }
        }

        themeButton.label = s.darkMode ? "DARK" : "LIGHT";
        soundButton.label = s.soundEffects ? "ON" : "OFF";

        if (renaming) {
            renameConfirm.update(viewport);
            renameCancel.update(viewport);
            if (renameConfirm.isClicked()) confirmRename();
            else if (renameCancel.isClicked()) renaming = false;
        } else if (showDevInfo) {
            devInfoClose.update(viewport);
            if (devInfoClose.isClicked()) showDevInfo = false;
        } else {
            themeButton.update(viewport);
            soundButton.update(viewport);
            renameButton.update(viewport);
            devInfoButton.update(viewport);
            backButton.update(viewport);

            if (themeButton.isClicked()) s.darkMode = !s.darkMode;
            if (soundButton.isClicked()) s.soundEffects = !s.soundEffects;
            if (renameButton.isClicked()) {
                nameInput.setLength(0);
                nameInput.append(game.playerName == null ? "" : game.playerName);
                renaming = true;
            }
            if (devInfoButton.isClicked()) showDevInfo = true;
            if (backButton.isClicked()) { goBack(); return; }

            updateSlider(s);
        }

        Color bg = s.darkMode ? DARK_BG : LIGHT_BG;
        Color text = s.darkMode ? Color.WHITE : Color.BLACK;

        Gdx.gl.glClearColor(bg.r, bg.g, bg.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();

        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        drawSlider(s);

        batch.begin();
        font.getData().setScale(2f);
        font.setColor(text);
        GlyphLayout t = new GlyphLayout(font, "Settings");
        font.draw(batch, "Settings", (W - t.width) / 2f, 700f);

        font.getData().setScale(1.3f);
        font.setColor(text);
        font.draw(batch, "Theme", 220f, 598f);
        font.draw(batch, "Background Music", 220f, 528f);
        font.draw(batch, "Sound Effects", 220f, 458f);
        font.draw(batch, "SOUL Name: " + (game.playerName == null || game.playerName.isEmpty() ? "(not set)" : game.playerName), 220f, 388f);
        font.draw(batch, "About", 220f, 318f);
        font.draw(batch, "Version 0.1", W / 2f - 45f, 120f);
        batch.end();

        themeButton.draw(shapes, batch, font);
        soundButton.draw(shapes, batch, font);
        renameButton.draw(shapes, batch, font);
        devInfoButton.draw(shapes, batch, font);
        backButton.draw(shapes, batch, font);

        if (renaming) drawRenamePopup();
        if (showDevInfo) drawDevInfoPopup();
    }

    private void updateSlider(GameSettings s) {
        Vector2 v = mouse();
        if (Gdx.input.justTouched() && v.x >= SLIDER_X - 10f && v.x <= SLIDER_X + SLIDER_W + 10f
            && v.y >= SLIDER_Y - 15f && v.y <= SLIDER_Y + 15f) {
            draggingSlider = true;
        }
        if (!Gdx.input.isTouched()) draggingSlider = false;
        if (draggingSlider) {
            s.musicVolume = Math.max(0f, Math.min(1f, (v.x - SLIDER_X) / SLIDER_W));
        }
    }

    private void drawSlider(GameSettings s) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Color.LIGHT_GRAY);
        shapes.rect(SLIDER_X, SLIDER_Y - 3f, SLIDER_W, 6f);
        shapes.setColor(0.96f, 0.60f, 0.40f, 1f);
        shapes.rect(SLIDER_X, SLIDER_Y - 3f, SLIDER_W * s.musicVolume, 6f);
        shapes.circle(SLIDER_X + SLIDER_W * s.musicVolume, SLIDER_Y, 11f);
        shapes.end();
    }

    private void drawRenamePopup() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(BOX);
        shapes.rect(W / 2f - 250f, 270f, 500f, 250f);
        shapes.setColor(Color.WHITE);
        shapes.rect(W / 2f - 150f, 385f, 300f, 45f);
        shapes.end();

        batch.begin();
        font.getData().setScale(1.6f);
        font.setColor(Color.BLACK);
        GlyphLayout t = new GlyphLayout(font, "Rename your SOUL");
        font.draw(batch, "Rename your SOUL", (W - t.width) / 2f, 495f);
        font.getData().setScale(1.4f);
        font.draw(batch, nameInput.toString() + "|", W / 2f - 140f, 416f);
        batch.end();

        renameConfirm.draw(shapes, batch, font);
        renameCancel.draw(shapes, batch, font);
    }

    private void drawDevInfoPopup() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(BOX);
        shapes.rect(W / 2f - 260f, 230f, 520f, 300f);
        shapes.end();

        batch.begin();
        font.getData().setScale(1.6f);
        font.setColor(Color.BLACK);
        GlyphLayout t = new GlyphLayout(font, "SoulStice - Version 0.1");
        font.draw(batch, "SoulStice - Version 0.1", (W - t.width) / 2f, 500f);
        font.getData().setScale(1.1f);
        font.draw(batch, "Orquiola, Althea Bernice G. - Leader / Programmer", W / 2f - 230f, 450f);
        font.draw(batch, "Emata, Shaina Mae V. - UI Designer", W / 2f - 230f, 420f);
        font.draw(batch, "Naval, Corybelle - Documentation / Tester", W / 2f - 230f, 390f);
        font.draw(batch, "Andrada, Elizah Beatrice B. - Programmer", W / 2f - 230f, 360f);
        font.draw(batch, "Gonzales, Jenelle M. - Programmer", W / 2f - 230f, 330f);
        batch.end();

        devInfoClose.draw(shapes, batch, font);
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
    }
}
