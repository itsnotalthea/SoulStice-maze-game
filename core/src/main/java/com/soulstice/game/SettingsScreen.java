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

public class SettingsScreen implements Screen {

    private static final float W = 1024f;
    private static final float H = 768f;
    private static final Color BG = new Color(0.76f, 1f, 0.47f, 1f);

    private final SoulsticeGame game;
    private final boolean fromPause;

    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapes;
    private SpriteBatch batch;
    private BitmapFont font;

    private UIButton backButton;

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

        backButton = new UIButton(W / 2f - 100f, 250f, 200f, 50f, "BACK");
    }

    private void goBack() {
        if (fromPause) game.showPause();
        else game.showMainMenu();
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            goBack();
            return;
        }

        backButton.update(viewport);
        if (backButton.isClicked()) {
            goBack();
            return;
        }

        Gdx.gl.glClearColor(BG.r, BG.g, BG.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();

        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        font.getData().setScale(2f);
        font.setColor(Color.BLACK);
        GlyphLayout t = new GlyphLayout(font, "Settings");
        font.draw(batch, "Settings", (W - t.width) / 2f, 560f);

        font.getData().setScale(1.2f);
        GlyphLayout s = new GlyphLayout(font, "Settings options coming soon.");
        font.draw(batch, "Settings options coming soon.", (W - s.width) / 2f, 420f);
        batch.end();

        backButton.draw(shapes, batch, font);
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
