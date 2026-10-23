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
    private Texture unpauseButtonTexture;
    private Texture optionsButtonTexture;
    private Texture quitButtonTexture;

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

        unpauseButtonTexture = new Texture(Gdx.files.internal("unpausebtn.png"));
        optionsButtonTexture = new Texture(Gdx.files.internal("optionsbtn.png"));
        quitButtonTexture = new Texture(Gdx.files.internal("quitbtn.png"));
        unpauseButtonTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        optionsButtonTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        quitButtonTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        float buttonWidth = 190f;
        float buttonHeight = 62f;
        float buttonX = (W - buttonWidth) / 2f;

        unpauseButton = new UIButton(buttonX, 420f - buttonHeight / 2f, buttonWidth, buttonHeight, "UNPAUSE");
        optionsButton = new UIButton(buttonX, 350f - buttonHeight / 2f, buttonWidth, buttonHeight, "OPTIONS");
        quitButton = new UIButton(buttonX, 280f - buttonHeight / 2f, buttonWidth, buttonHeight, "QUIT");
        unpauseButton.setTexture(unpauseButtonTexture);
        optionsButton.setTexture(optionsButtonTexture);
        quitButton.setTexture(quitButtonTexture);
    }

    @Override
    public void render(float delta) {
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
        shapes.rect(W / 2f - 150f, 230f, 300f, 280f);
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
        unpauseButtonTexture.dispose();
        optionsButtonTexture.dispose();
        quitButtonTexture.dispose();
    }
}
