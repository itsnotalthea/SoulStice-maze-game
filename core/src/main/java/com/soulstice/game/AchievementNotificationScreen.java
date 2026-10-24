package com.soulstice.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.List;

public class AchievementNotificationScreen implements Screen {

    private static final float W = 1024f;
    private static final float H = 768f;
    private static final float ENTER_DURATION = 0.38f;
    private static final float EXIT_DURATION = 0.30f;
    private static final Color DIM = new Color(0f, 0f, 0f, 0.62f);
    private static final Color INK = new Color(0.55f, 0.25f, 0.05f, 1f);

    private final SoulsticeGame game;
    private final Array<Achievement> queue = new Array<Achievement>();
    private final Runnable onFinished;

    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapes;
    private SpriteBatch batch;
    private BitmapFont font;
    private Texture[] panels;
    private Texture[] nextButtons;
    private float elapsed;
    private float exitElapsed;
    private boolean exiting;

    public AchievementNotificationScreen(
        SoulsticeGame game,
        List<Achievement> achievements,
        Runnable onFinished
    ) {
        this.game = game;
        this.queue.addAll(achievements.toArray(new Achievement[achievements.size()]));
        this.onFinished = onFinished;
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
        panels = new Texture[Achievement.values().length];
        nextButtons = new Texture[Achievement.values().length];
        for (Achievement achievement : queue) {
            int index = achievement.ordinal();
            panels[index] = loadTexture(achievement.popupAsset);
            if (achievement.nextButtonAsset != null) {
                nextButtons[index] = loadTexture(achievement.nextButtonAsset);
            }
        }
    }

    private Texture loadTexture(String asset) {
        if (!Gdx.files.internal(asset).exists()) {
            Gdx.app.error("Soulstice", "Required achievement popup asset is missing: " + asset);
            return null;
        }
        return new Texture(Gdx.files.internal(asset));
    }

    @Override
    public void render(float delta) {
        elapsed += delta;
        if (exiting) {
            exitElapsed += delta;
            if (exitElapsed >= EXIT_DURATION) {
                finishCurrent();
                return;
            }
        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();
        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        float progress = exiting
            ? 1f - Math.min(1f, exitElapsed / EXIT_DURATION)
            : Math.min(1f, elapsed / ENTER_DURATION);
        float alpha = Interpolation.smooth.apply(progress);
        Achievement current = queue.first();
        Texture panel = panels[current.ordinal()];
        float panelWidth = 820f;
        float panelHeight = panel == null ? 460f
            : panelWidth * panel.getHeight() / panel.getWidth();
        if (panelHeight > H - 100f) {
            panelHeight = H - 100f;
            panelWidth = panel == null ? panelWidth : panelHeight * panel.getWidth() / panel.getHeight();
        }
        float panelX = (W - panelWidth) / 2f;
        float panelY = (H - panelHeight) / 2f - (1f - alpha) * 46f;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, DIM.a * alpha);
        shapes.rect(0f, 0f, W, H);
        if (panel == null) {
            shapes.setColor(0.98f, 0.91f, 0.65f, alpha);
            shapes.rect(panelX, panelY, panelWidth, panelHeight);
        }
        shapes.end();

        if (panel != null) {
            batch.begin();
            batch.setColor(1f, 1f, 1f, alpha);
            batch.draw(panel, panelX, panelY, panelWidth, panelHeight);
            batch.setColor(Color.WHITE);
            batch.end();
        }

        drawExactDescription(current, panelX, panelY, panelWidth, panelHeight, alpha);
        drawNextButton(current, panelX, panelY, panelWidth, panelHeight, alpha);

        if (!exiting && elapsed >= ENTER_DURATION
            && Gdx.input.justTouched() && clickedNext(current, panelX, panelY, panelWidth, panelHeight)) {
            exiting = true;
        }
    }

    private void drawExactDescription(
        Achievement achievement,
        float panelX,
        float panelY,
        float panelWidth,
        float panelHeight,
        float alpha
    ) {
        if (achievement != Achievement.SOULSTICE_LEGEND
            && achievement != Achievement.ALL_ACHIEVEMENTS) return;
        float boxX = panelX + panelWidth * 0.16f;
        float boxWidth = panelWidth * 0.68f;
        float boxY = panelY + panelHeight * 0.08f;
        float boxHeight = panelHeight * 0.23f;
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(1f, 0.91f, 0.60f, 0.95f * alpha);
        shapes.rect(boxX, boxY, boxWidth, boxHeight);
        shapes.end();

        batch.begin();
        font.getData().setScale(1.05f);
        font.setColor(INK.r, INK.g, INK.b, alpha);
        font.draw(batch, achievement.popupDescription,
            boxX + 16f, boxY + boxHeight * 0.72f, boxWidth - 32f,
            com.badlogic.gdx.utils.Align.center, true);
        font.setColor(Color.WHITE);
        batch.end();
    }

    private void drawNextButton(
        Achievement achievement,
        float panelX,
        float panelY,
        float panelWidth,
        float panelHeight,
        float alpha
    ) {
        Texture button = nextButtons[achievement.ordinal()];
        if (button == null) return;
        float buttonWidth = 165f;
        float buttonHeight = buttonWidth * button.getHeight() / button.getWidth();
        float buttonX = panelX + panelWidth - buttonWidth - 28f;
        float buttonY = panelY + 20f;
        float buttonAlpha = alpha * Math.min(1f, Math.max(0f, (elapsed - 0.18f) / 0.20f));
        batch.begin();
        batch.setColor(1f, 1f, 1f, buttonAlpha);
        batch.draw(button, buttonX, buttonY, buttonWidth, buttonHeight);
        batch.setColor(Color.WHITE);
        batch.end();
    }

    private boolean clickedNext(
        Achievement achievement,
        float panelX,
        float panelY,
        float panelWidth,
        float panelHeight
    ) {
        Vector2 point = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        if (achievement == Achievement.STREAK) {
            return contains(point,
                panelX + panelWidth * 0.70f,
                panelY + panelHeight * 0.04f,
                panelWidth * 0.27f,
                panelHeight * 0.22f);
        }
        Texture button = nextButtons[achievement.ordinal()];
        if (button == null) return contains(point, panelX, panelY, panelWidth, panelHeight);
        float buttonWidth = 165f;
        float buttonHeight = buttonWidth * button.getHeight() / button.getWidth();
        return contains(point,
            panelX + panelWidth - buttonWidth - 28f,
            panelY + 20f,
            buttonWidth,
            buttonHeight);
    }

    private static boolean contains(
        Vector2 point,
        float x,
        float y,
        float width,
        float height
    ) {
        return point.x >= x && point.x <= x + width && point.y >= y && point.y <= y + height;
    }

    private void finishCurrent() {
        queue.removeIndex(0);
        if (queue.size == 0) {
            onFinished.run();
        } else {
            elapsed = 0f;
            exitElapsed = 0f;
            exiting = false;
        }
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
        for (Texture texture : panels) {
            if (texture != null) texture.dispose();
        }
        for (Texture texture : nextButtons) {
            if (texture != null) texture.dispose();
        }
    }
}
