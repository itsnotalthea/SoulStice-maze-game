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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
    private static final Color LEVEL_NUMBER_HOVERED = new Color(1f, 1f, 0.78f, 1f);
    private static final Color LOCKED_BUTTON = new Color(0.55f, 0.56f, 0.58f, 1f);
    private static final Color LOCKED_NUMBER = new Color(0.85f, 0.86f, 0.88f, 1f);
    private static final float BACK_X = 32f;
    private static final float BACK_Y = 680f;
    private static final float BACK_WIDTH = 66f;
    private static final float BACK_HEIGHT = 52f;

    private final SoulsticeGame game;

    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapes;
    private SpriteBatch batch;
    private BitmapFont font;
    private Texture backgroundTexture;
    private Texture titleTexture;
    private Texture backTexture;
    private Texture levelButtonTexture;
    private final TextureRegion[] levelButtonRegions = new TextureRegion[TOTAL_LEVELS];
    private final TextureRegion[] levelButtonHoverRegions = new TextureRegion[TOTAL_LEVELS];

    private final float[] cx = new float[TOTAL_LEVELS];
    private final float[] cy = new float[TOTAL_LEVELS];
    private final float radius = 43f;
    private int hoveredLevel = -1;
    private boolean backHovered;

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
        backgroundTexture = new Texture(Gdx.files.internal("MainMenu_Background.png"));
        backgroundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        titleTexture = new Texture(Gdx.files.internal("selectlevel.png"));
        titleTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        backTexture = new Texture(Gdx.files.internal("backicon.png"));
        backTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        levelButtonTexture = new Texture(Gdx.files.internal("levelbtn.png"));
        levelButtonTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        for (int i = 0; i < 10; i++) {
            int x = 28 + i * 123;
            levelButtonRegions[i] = new TextureRegion(levelButtonTexture, x, 31, 86, 86);
            levelButtonHoverRegions[i] = new TextureRegion(levelButtonTexture, x, 138, 86, 86);
            levelButtonRegions[i + 10] = new TextureRegion(levelButtonTexture, x, 257, 86, 86);
            levelButtonHoverRegions[i + 10] = new TextureRegion(levelButtonTexture, x, 376, 86, 86);
        }
        levelButtonRegions[20] = new TextureRegion(levelButtonTexture, 1014, 483, 86, 86);
        levelButtonHoverRegions[20] = new TextureRegion(levelButtonTexture, 1137, 483, 86, 86);

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
        Gdx.gl.glClearColor(BG.r, BG.g, BG.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();

        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(backgroundTexture, 0f, 0f, W, H);
        batch.end();

        Vector2 pointer = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        backHovered = pointer.x >= BACK_X && pointer.x <= BACK_X + BACK_WIDTH
            && pointer.y >= BACK_Y && pointer.y <= BACK_Y + BACK_HEIGHT;
        hoveredLevel = -1;
        for (int i = 0; i < TOTAL_LEVELS; i++) {
            float dx = pointer.x - cx[i];
            float dy = pointer.y - cy[i];
            if (i < game.unlockedLevel && dx * dx + dy * dy <= radius * radius) {
                hoveredLevel = i;
                break;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || (backHovered && Gdx.input.justTouched())) {
            game.showMainMenu();
            return;
        }

        if (Gdx.input.justTouched() && hoveredLevel >= 0 && hoveredLevel < game.unlockedLevel) {
            game.startLevel(hoveredLevel + 1);
            return;
        }

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < TOTAL_LEVELS; i++) {
            if (i >= game.unlockedLevel) {
                shapes.setColor(LOCKED_BUTTON);
                shapes.circle(cx[i], cy[i], radius);
            }
        }
        shapes.end();

        batch.begin();
        batch.draw(titleTexture, (W - 392f) / 2f, 640f, 392f, 58f);
        batch.setColor(backHovered ? LEVEL_NUMBER_HOVERED : Color.WHITE);
        batch.draw(backTexture, BACK_X, BACK_Y, BACK_WIDTH, BACK_HEIGHT);
        batch.setColor(Color.WHITE);

        font.getData().setScale(1.5f);
        for (int i = 0; i < TOTAL_LEVELS; i++) {
            if (i < game.unlockedLevel) {
                TextureRegion button = i == hoveredLevel ? levelButtonHoverRegions[i] : levelButtonRegions[i];
                batch.draw(button, cx[i] - radius, cy[i] - radius, radius * 2f, radius * 2f);
            } else {
                String level = String.valueOf(i + 1);
                GlyphLayout layout = new GlyphLayout(font, level);
                font.setColor(LOCKED_NUMBER);
                font.draw(batch, level, cx[i] - layout.width / 2f,
                    cy[i] + layout.height / 2f);
            }
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
        backgroundTexture.dispose();
        titleTexture.dispose();
        backTexture.dispose();
        levelButtonTexture.dispose();
    }
}
