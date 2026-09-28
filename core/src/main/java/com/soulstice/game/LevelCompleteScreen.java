package com.soulstice.game;

import com.badlogic.gdx.Gdx;
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

public class LevelCompleteScreen implements Screen {

    private static final float W = 1024f;
    private static final float H = 768f;
    private static final int TOTAL_LEVELS = 21;
    private static final Color BG  = new Color(0.20f, 0.13f, 0.09f, 1f);
    private static final Color BOX = new Color(1f, 0.95f, 0.78f, 1f);

    private final SoulsticeGame game;
    private final int level;

    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapes;
    private SpriteBatch batch;
    private BitmapFont font;

    private UIButton mainMenuButton, nextButton;

    public LevelCompleteScreen(SoulsticeGame game, int level) {
        this.game = game;
        this.level = level;
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

        mainMenuButton = new UIButton(W / 2f - 200f, 290f, 180f, 45f, "MAIN MENU");
        String nextLabel = level < TOTAL_LEVELS ? "NEXT LEVEL" : "LEVEL MAP";
        nextButton = new UIButton(W / 2f + 20f, 290f, 180f, 45f, nextLabel);
    }

    @Override
    public void render(float delta) {
        mainMenuButton.update(viewport);
        nextButton.update(viewport);

        if (mainMenuButton.isClicked()) { game.showMainMenu(); return; }
        if (nextButton.isClicked()) {
            if (level < TOTAL_LEVELS) game.startLevel(level + 1);
            else game.showLevelMap();
            return;
        }

        Gdx.gl.glClearColor(BG.r, BG.g, BG.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();

        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(BOX);
        shapes.rect(W / 2f - 260f, 240f, 520f, 280f);
        shapes.end();

        batch.begin();
        font.getData().setScale(2.2f);
        font.setColor(Color.BLACK);
        GlyphLayout t = new GlyphLayout(font, "LEVEL COMPLETE!");
        font.draw(batch, "LEVEL COMPLETE!", (W - t.width) / 2f, 490f);

        font.getData().setScale(1.3f);
        String sub = "Level " + level + " cleared";
        GlyphLayout s = new GlyphLayout(font, sub);
        font.draw(batch, sub, (W - s.width) / 2f, 420f);
        batch.end();

        mainMenuButton.draw(shapes, batch, font);
        nextButton.draw(shapes, batch, font);
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
