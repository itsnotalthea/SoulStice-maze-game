package com.soulstice.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class PauseMenuScreen implements Screen {

    private static final float W = 1024f;
    private static final float H = 768f;
    private static final Color BG  = new Color(0.20f, 0.13f, 0.09f, 1f);
    private static final Color BOX = new Color(1f, 0.95f, 0.78f, 1f);

    private final SoulsticeGame game;

    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapes;
    private SpriteBatch batch;
    private BitmapFont font;

    private UIButton unpauseButton, optionsButton, quitButton;

    public PauseMenuScreen(SoulsticeGame game) {
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

        unpauseButton = new UIButton(W / 2f - 90f, 400f, 180f, 45f, "UNPAUSE");
        optionsButton = new UIButton(W / 2f - 90f, 340f, 180f, 45f, "OPTIONS");
        quitButton    = new UIButton(W / 2f - 90f, 280f, 180f, 45f, "QUIT");
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.resumeLevel();
            return;
        }

        unpauseButton.update(viewport);
        optionsButton.update(viewport);
        quitButton.update(viewport);

        if (unpauseButton.isClicked()) { game.resumeLevel(); return; }
        if (optionsButton.isClicked()) { game.showSettings(true); return; }
        if (quitButton.isClicked())    { game.showMainMenu(); return; }

        Gdx.gl.glClearColor(BG.r, BG.g, BG.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();

        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(BOX);
        shapes.rect(W / 2f - 150f, 250f, 300f, 260f);
        shapes.end();

        batch.begin();
        font.getData().setScale(2f);
        font.setColor(Color.BLACK);
        GlyphLayout t = new GlyphLayout(font, "Pause");
        font.draw(batch, "Pause", (W - t.width) / 2f, 490f);
        batch.end();

        unpauseButton.draw(shapes, batch, font);
        optionsButton.draw(shapes, batch, font);
        quitButton.draw(shapes, batch, font);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
    }
}
