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
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MazeScreen implements Screen {

    private static final Color COLOR_BACKGROUND = new Color(0.20f, 0.13f, 0.09f, 1f);
    private static final Color COLOR_PAUSE      = new Color(0.96f, 0.60f, 0.40f, 1f);
    private static final Color COLOR_TIMER_PANEL = new Color(0.12f, 0.25f, 0.12f, 1f);
    private static final Color COLOR_TIMER_TEXT = new Color(1f, 0.95f, 0.78f, 1f);
    private static final Color COLOR_TIMER_URGENT = new Color(1f, 0.35f, 0.24f, 1f);

    private static final float VIRTUAL_WIDTH = 1024f;
    private static final float VIRTUAL_HEIGHT = 768f;
    private static final float SCREEN_MARGIN = 15f;
    private static final float MAZE_BORDER_CELL_RATIO = 0.4f;
    private static final float MOVE_DURATION = 0.12f;
    private static final float SOUL_FRAME_DURATION = 0.15f;

    // changed: new body sheet has 2 walk frames per direction (8 rows total)
    private static final int BODY_FRAMES_PER_DIRECTION = 2;
    private static final int SPRITE_SIZE = 32;

    private static final float PAUSE_X = VIRTUAL_WIDTH - 45f;
    private static final float PAUSE_Y = VIRTUAL_HEIGHT - 45f;
    private static final float PAUSE_RADIUS = 20f;
    private static final float TIMER_PANEL_WIDTH = 130f;
    private static final float TIMER_PANEL_HEIGHT = 88f;

    private final int level;
    private final MazeGenerator maze;
    private final GameplayController gameplay;
    private final InputHandler inputHandler;

    private final Runnable onWin;
    private final Runnable onPause;
    private final Runnable onTimeUp;

    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private BitmapFont font;
    private Texture wallTexture;
    private Texture cellTexture;
    private Texture mazeBorderTexture;
    private Texture bodyTexture;
    private Texture soulTexture;

    private TextureRegion mazeBorderTop;
    private TextureRegion mazeBorderBottom;
    private TextureRegion mazeBorderLeft;
    private TextureRegion mazeBorderRight;
    private TextureRegion[][] bodyFrames;
    private TextureRegion[][] soulFrames;

    private Direction bodyFacing = Direction.DOWN;
    private float bodyWalkClock = 0f;
    private float soulAnimClock = 0f;

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
    private boolean timeUpFired = false;
    private boolean skipNextTimerDelta = false;
    private float timeRemaining;
    private final float timeLimit;

    public MazeScreen(int level, Runnable onWin, Runnable onPause, Runnable onTimeUp) {
        this.level = level;
        this.maze = new MazeGenerator(level);
        this.gameplay = new GameplayController(maze);
        this.inputHandler = new InputHandler();
        this.onWin = onWin;
        this.onPause = onPause;
        this.onTimeUp = onTimeUp;
        this.timeLimit = timeLimitForLevel(level);
        this.timeRemaining = timeLimit;

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
        font = new BitmapFont();
        wallTexture = new Texture("walls.png");
        cellTexture = new Texture("cells.png");
        mazeBorderTexture = new Texture("maze.png");
        bodyTexture = new Texture("body.png");
        soulTexture = new Texture("soul.png");

        mazeBorderTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        mazeBorderTop = new TextureRegion(mazeBorderTexture, 18, 28, 370, 34);
        mazeBorderBottom = new TextureRegion(mazeBorderTexture, 18, 358, 370, 36);
        mazeBorderLeft = new TextureRegion(mazeBorderTexture, 18, 28, 34, 366);
        mazeBorderRight = new TextureRegion(mazeBorderTexture, 352, 28, 36, 366);

        // changed: sprites are now 32x32 (was 16x16)
        bodyTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        soulTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        bodyFrames = TextureRegion.split(bodyTexture, SPRITE_SIZE, SPRITE_SIZE);
        soulFrames = TextureRegion.split(soulTexture, SPRITE_SIZE, SPRITE_SIZE);

        computeMazeLayout();
    }

    private void computeMazeLayout() {
        int gridSize = maze.getSize();
        cellPixelSize = Math.min(
            (VIRTUAL_WIDTH - SCREEN_MARGIN * 2f) / (gridSize + MAZE_BORDER_CELL_RATIO * 2f),
            (VIRTUAL_HEIGHT - SCREEN_MARGIN * 2f) / (gridSize + MAZE_BORDER_CELL_RATIO * 2f)
        );

        float mazePixelSize = cellPixelSize * gridSize;
        mazeOriginX = (VIRTUAL_WIDTH - mazePixelSize) / 2f;
        mazeOriginY = (VIRTUAL_HEIGHT - mazePixelSize) / 2f;
    }

    @Override
    public void render(float delta) {
        if (timeUpFired || winFired) return;

        float timerDelta = skipNextTimerDelta ? 0f : delta;
        skipNextTimerDelta = false;
        timeRemaining = Math.max(0f, timeRemaining - timerDelta);
        if (timeRemaining <= 0f) {
            timeUpFired = true;
            onTimeUp.run();
            return;
        }

        handleInput();
        updateAnimations(delta);

        Gdx.gl.glClearColor(COLOR_BACKGROUND.r, COLOR_BACKGROUND.g, COLOR_BACKGROUND.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();

        drawWalls();
        drawMazeBorder();
        drawEntities();
        drawTimer();
        drawPauseButton();

        if (gameplay.isWon() && !bodyAnimating && !soulAnimating && !winFired) {
            winFired = true;
            onWin.run();
        }
    }

    static int timeLimitForLevel(int level) {
        int clampedLevel = Math.max(1, Math.min(21, level));
        return Math.round(35f - ((clampedLevel - 1) * 15f / 20f));
    }

    float getElapsedSeconds() {
        return timeLimit - timeRemaining;
    }

    private void drawTimer() {
        // changed: timer moved to the empty right-hand strip
        float borderSize = cellPixelSize * MAZE_BORDER_CELL_RATIO;
        float mazeRight = mazeOriginX + cellPixelSize * maze.getSize() + borderSize;
        float panelX = (mazeRight + VIRTUAL_WIDTH) / 2f - TIMER_PANEL_WIDTH / 2f;
        float panelY = (VIRTUAL_HEIGHT - TIMER_PANEL_HEIGHT) / 2f;

        int displayedSeconds = (int) Math.ceil(timeRemaining);
        String levelText = "LEVEL " + level;
        String timeText = String.format(java.util.Locale.ROOT, "TIME 00:%02d", displayedSeconds);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(COLOR_TIMER_PANEL);
        shapeRenderer.rect(panelX, panelY, TIMER_PANEL_WIDTH, TIMER_PANEL_HEIGHT);
        shapeRenderer.end();

        font.getData().setScale(1.25f);
        font.setColor(displayedSeconds <= 5 ? COLOR_TIMER_URGENT : COLOR_TIMER_TEXT);
        GlyphLayout levelLayout = new GlyphLayout(font, levelText);
        GlyphLayout timeLayout = new GlyphLayout(font, timeText);
        float lineGap = 8f;
        float blockHeight = levelLayout.height + lineGap + timeLayout.height;
        float textTop = panelY + (TIMER_PANEL_HEIGHT + blockHeight) / 2f;

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        font.draw(spriteBatch, levelText,
            panelX + (TIMER_PANEL_WIDTH - levelLayout.width) / 2f, textTop);
        font.draw(spriteBatch, timeText,
            panelX + (TIMER_PANEL_WIDTH - timeLayout.width) / 2f, textTop - levelLayout.height - lineGap);
        spriteBatch.end();
    }

    private void drawMazeBorder() {
        float borderSize = cellPixelSize * MAZE_BORDER_CELL_RATIO;
        float mazeWidth = cellPixelSize * maze.getSize();
        float left = mazeOriginX - borderSize;
        float bottom = mazeOriginY - borderSize;

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        spriteBatch.draw(mazeBorderTop, left, mazeOriginY + mazeWidth, mazeWidth, borderSize);
        spriteBatch.draw(mazeBorderBottom, left, bottom - borderSize * 0.15f, mazeWidth, borderSize);
        spriteBatch.draw(mazeBorderLeft, left, mazeOriginY, borderSize, mazeWidth);
        spriteBatch.draw(mazeBorderRight, mazeOriginX + mazeWidth, mazeOriginY, borderSize, mazeWidth);
        spriteBatch.end();
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

        // turn first, so the sprite reacts even when a wall blocks the move
        bodyFacing = direction;

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
        if (bodyAnimating) bodyWalkClock += delta;
        soulAnimClock += delta;

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
        float spriteSize = cellPixelSize * 0.95f;

        int bodyFrame = 0;
        if (bodyAnimating) {
            int step = (int) (bodyWalkClock / (MOVE_DURATION * 0.5f));
            bodyFrame = step % BODY_FRAMES_PER_DIRECTION;
        }
        // changed: body uses direction row + walk frame; soul loops all 8 frames
        int bodyRow = directionRow(bodyFacing) * BODY_FRAMES_PER_DIRECTION + bodyFrame;
        int soulFrame = (int) (soulAnimClock / SOUL_FRAME_DURATION) % soulFrames.length;

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        drawSprite(bodyFrames[bodyRow][0], bodyDrawCol, bodyDrawRow, spriteSize);
        drawSprite(soulFrames[soulFrame][0], soulDrawCol, soulDrawRow, spriteSize);
        spriteBatch.end();
    }

    private void drawSprite(TextureRegion region, float col, float row, float size) {
        float x = cellCenterX(col) - (size / 2f);
        float y = cellCenterY(row) - (size / 2f);
        spriteBatch.draw(region, x, y, size, size);
    }

    // changed: sheet direction pairs are ordered down, up, left, right
    private static int directionRow(Direction direction) {
        switch (direction) {
            case UP:    return 1;
            case LEFT:  return 2;
            case RIGHT: return 3;
            default:    return 0;
        }
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

    @Override public void pause() { skipNextTimerDelta = true; }
    @Override public void resume() { skipNextTimerDelta = true; }
    @Override public void hide() {}

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
        font.dispose();
        wallTexture.dispose();
        cellTexture.dispose();
        mazeBorderTexture.dispose();
        bodyTexture.dispose();
        soulTexture.dispose();
    }
}
