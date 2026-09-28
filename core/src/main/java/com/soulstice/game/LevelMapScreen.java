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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class LevelMapScreen implements Screen {

    private static final float W = 1024f;
    private static final float H = 768f;
    private static final int TOTAL_LEVELS = 21;
    private static final int COLUMNS = 7;

    private static final Color BG       = new Color(0.76f, 1f, 0.47f, 1f);
    private static final Color UNLOCKED = new Color(0.96f, 0.60f, 0.40f, 1f);
    private static final Color LOCKED   = new Color(0.85f, 0.85f, 0.85f, 1f);

    private final SoulsticeGame game;

    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapes;
    private SpriteBatch batch;
    private BitmapFont font;

    private final float[] cx = new float[TOTAL_LEVELS];
    private final float[] cy = new float[TOTAL_LEVELS];
    private final float radius = 36f;

    public LevelMapScreen(SoulsticeGame game) {
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

        float spacing = 110f;
        float startX = (W - (COLUMNS - 1) * spacing) / 2f;
        float startY = 470f;
        for (int i = 0; i < TOTAL_LEVELS; i++) {
            cx[i] = startX + (i % COLUMNS) * spacing;
            cy[i] = startY - (i / COLUMNS) * spacing;
        }
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.showMainMenu();
            return;
        }

        if (Gdx.input.justTouched()) {
            Vector2 v = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            for (int i = 0; i < TOTAL_LEVELS; i++) {
                float dx = v.x - cx[i];
                float dy = v.y - cy[i];
                if (dx * dx + dy * dy <= radius * radius && i < game.unlockedLevel) {
                    game.startLevel(i + 1);
                    return;
                }
            }
        }

        Gdx.gl.glClearColor(BG.r, BG.g, BG.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();

        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < TOTAL_LEVELS; i++) {
            shapes.setColor(i < game.unlockedLevel ? UNLOCKED : LOCKED);
            shapes.circle(cx[i], cy[i], radius);
        }
        shapes.end();

        batch.begin();
        font.getData().setScale(2f);
        font.setColor(Color.BLACK);
        GlyphLayout title = new GlyphLayout(font, "Select Level");
        font.draw(batch, "Select Level", (W - title.width) / 2f, 650f);

        font.getData().setScale(1.5f);
        for (int i = 0; i < TOTAL_LEVELS; i++) {
            String s = String.valueOf(i + 1);
            GlyphLayout g = new GlyphLayout(font, s);
            font.setColor(i < game.unlockedLevel ? Color.WHITE : UNLOCKED);
            font.draw(batch, s, cx[i] - g.width / 2f, cy[i] + g.height / 2f);
        }
        batch.end();
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
