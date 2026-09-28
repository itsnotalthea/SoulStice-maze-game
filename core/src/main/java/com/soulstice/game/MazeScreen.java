package com.soulstice.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MazeScreen implements Screen {

    private static final Color COLOR_BACKGROUND = new Color(0.20f, 0.13f, 0.09f, 1f);
    private static final Color COLOR_BODY       = new Color(0.82f, 0.65f, 0.52f, 1f);
    private static final Color COLOR_SOUL       = new Color(0.97f, 0.92f, 0.55f, 1f);
    private static final Color COLOR_PAUSE      = new Color(0.96f, 0.60f, 0.40f, 1f);

    private static final float VIRTUAL_WIDTH = 1024f;
    private static final float VIRTUAL_HEIGHT = 768f;
    private static final float SCREEN_MARGIN = 15f;
    private static final float MOVE_DURATION = 0.12f;

    private static final float PAUSE_X = VIRTUAL_WIDTH - 45f;
    private static final float PAUSE_Y = VIRTUAL_HEIGHT - 45f;
    private static final float PAUSE_RADIUS = 20f;

    private final MazeGenerator maze;
    private final GameplayController gameplay;
    private final InputHandler inputHandler;

    private final Runnable onWin;
    private final Runnable onPause;

    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private Texture wallTexture;
    private Texture cellTexture;

    private float cellPixelSize;
    private float mazeOriginX, mazeOriginY;

    private float bodyDrawRow, bodyDrawCol;
    private float bodyFromRow, bodyFromCol, bodyToRow, bodyToCol;
    private float bodyMoveTimer = 0f;
    private boolean bodyAnimating = false;

    private float soulDrawRow, soulDrawCol;
    private float soulFromRow, soulFromCol, soulToRow, soulToCol;
    private float soulMoveTimer = 0f;
    private boolean soulAnimating = false;

    private boolean winFired = false;

    public MazeScreen(int level, Runnable onWin, Runnable onPause) {
        this.maze = new MazeGenerator(level);
        this.gameplay = new GameplayController(maze);
        this.inputHandler = new InputHandler();
        this.onWin = onWin;
        this.onPause = onPause;

        GridPoint bodyStart = gameplay.getBodyPos();
        GridPoint soulStart = gameplay.getSoulPos();
        bodyDrawRow = bodyToRow = bodyFromRow = bodyStart.row;
        bodyDrawCol = bodyToCol = bodyFromCol = bodyStart.col;
        soulDrawRow = soulToRow = soulFromRow = soulStart.row;
        soulDrawCol = soulToCol = soulFromCol = soulStart.col;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);
        camera.update();

        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        wallTexture = new Texture("walls.png");
        cellTexture = new Texture("cells.png");

        computeMazeLayout();
    }

    private void computeMazeLayout() {
        int gridSize = maze.getSize();
        float availableWidth = VIRTUAL_WIDTH - (SCREEN_MARGIN * 2);
        float availableHeight = VIRTUAL_HEIGHT - (SCREEN_MARGIN * 2);

        cellPixelSize = Math.min(availableWidth, availableHeight) / gridSize;

        float mazePixelSize = cellPixelSize * gridSize;
        mazeOriginX = (VIRTUAL_WIDTH - mazePixelSize) / 2f;
        mazeOriginY = (VIRTUAL_HEIGHT - mazePixelSize) / 2f;
    }

    @Override
    public void render(float delta) {
        handleInput();
        updateAnimations(delta);

        Gdx.gl.glClearColor(COLOR_BACKGROUND.r, COLOR_BACKGROUND.g, COLOR_BACKGROUND.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();

        drawWalls();
        drawEntities();
        drawPauseButton();

        if (gameplay.isWon() && !bodyAnimating && !soulAnimating && !winFired) {
            winFired = true;
            onWin.run();
        }
    }

    private void handleInput() {
        if (winFired) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            onPause.run();
            return;
        }

        if (Gdx.input.justTouched()) {
            float mx = Gdx.input.getX();
            float my = Gdx.input.getY();
            com.badlogic.gdx.math.Vector2 v = viewport.unproject(new com.badlogic.gdx.math.Vector2(mx, my));
            float dx = v.x - PAUSE_X;
            float dy = v.y - PAUSE_Y;
            if (dx * dx + dy * dy <= PAUSE_RADIUS * PAUSE_RADIUS) {
                onPause.run();
                return;
            }
        }

        if (bodyAnimating || soulAnimating || gameplay.isWon()) return;

        Direction direction = inputHandler.pollDirection();
        if (direction == null) return;

        GridPoint bodyBefore = gameplay.getBodyPos();
        GridPoint soulBefore = gameplay.getSoulPos();

        gameplay.handleInput(direction);

        GridPoint bodyAfter = gameplay.getBodyPos();
        GridPoint soulAfter = gameplay.getSoulPos();

        if (!bodyAfter.equals(bodyBefore)) {
            startMoveAnimation(true, bodyBefore, bodyAfter);
        }
        if (!soulAfter.equals(soulBefore)) {
            startMoveAnimation(false, soulBefore, soulAfter);
        }
    }

    private void startMoveAnimation(boolean isBody, GridPoint from, GridPoint to) {
        if (isBody) {
            bodyFromRow = from.row; bodyFromCol = from.col;
            bodyToRow = to.row;     bodyToCol = to.col;
            bodyMoveTimer = 0f;
            bodyAnimating = true;
        } else {
            soulFromRow = from.row; soulFromCol = from.col;
            soulToRow = to.row;     soulToCol = to.col;
            soulMoveTimer = 0f;
            soulAnimating = true;
        }
    }

    private void updateAnimations(float delta) {
        if (bodyAnimating) {
            bodyMoveTimer += delta;
            float t = Math.min(bodyMoveTimer / MOVE_DURATION, 1f);
            float eased = Interpolation.smooth.apply(t);
            bodyDrawRow = bodyFromRow + (bodyToRow - bodyFromRow) * eased;
            bodyDrawCol = bodyFromCol + (bodyToCol - bodyFromCol) * eased;
            if (t >= 1f) bodyAnimating = false;
        }
        if (soulAnimating) {
            soulMoveTimer += delta;
            float t = Math.min(soulMoveTimer / MOVE_DURATION, 1f);
            float eased = Interpolation.smooth.apply(t);
            soulDrawRow = soulFromRow + (soulToRow - soulFromRow) * eased;
            soulDrawCol = soulFromCol + (soulToCol - soulFromCol) * eased;
            if (t >= 1f) soulAnimating = false;
        }
    }

    private float cellCenterX(float col) {
        return mazeOriginX + (col * cellPixelSize) + (cellPixelSize / 2f);
    }

    private float cellCenterY(float row) {
        int gridSize = maze.getSize();
        return mazeOriginY + ((gridSize - 1 - row) * cellPixelSize) + (cellPixelSize / 2f);
    }

    private void drawWalls() {
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        Cell[][] grid = maze.getGrid();
        int gridSize = maze.getSize();
        float wallSize = 2f;

        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                Cell cell = grid[r][c];
                float x = mazeOriginX + c * cellPixelSize;
                float y = mazeOriginY + (gridSize - 1 - r) * cellPixelSize;

                spriteBatch.draw(cellTexture, x, y, cellPixelSize, cellPixelSize);

                if (cell.hasWallTop())    spriteBatch.draw(wallTexture, x, y + cellPixelSize - wallSize, cellPixelSize, wallSize);
                if (cell.hasWallBottom()) spriteBatch.draw(wallTexture, x, y, cellPixelSize, wallSize);
                if (cell.hasWallLeft())   spriteBatch.draw(wallTexture, x, y, wallSize, cellPixelSize);
                if (cell.hasWallRight())  spriteBatch.draw(wallTexture, x + cellPixelSize - wallSize, y, wallSize, cellPixelSize);
            }
        }

        spriteBatch.end();
    }

    private void drawEntities() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        float radius = cellPixelSize * 0.3f;

        shapeRenderer.setColor(COLOR_BODY);
        shapeRenderer.circle(cellCenterX(bodyDrawCol), cellCenterY(bodyDrawRow), radius);

        shapeRenderer.setColor(COLOR_SOUL);
        shapeRenderer.circle(cellCenterX(soulDrawCol), cellCenterY(soulDrawRow), radius);

        shapeRenderer.end();
    }

    private void drawPauseButton() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(COLOR_PAUSE);
        shapeRenderer.circle(PAUSE_X, PAUSE_Y, PAUSE_RADIUS);
        shapeRenderer.setColor(1f, 1f, 1f, 1f);
        shapeRenderer.rect(PAUSE_X - 8f, PAUSE_Y - 8f, 5f, 16f);
        shapeRenderer.rect(PAUSE_X + 3f, PAUSE_Y - 8f, 5f, 16f);
        shapeRenderer.end();
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
        shapeRenderer.dispose();
        spriteBatch.dispose();
        wallTexture.dispose();
        cellTexture.dispose();
    }
}
