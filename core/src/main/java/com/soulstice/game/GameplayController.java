package com.soulstice.game;

import java.util.Random;

/**
    This manages the live gameplay state for a single maze level:
    Holds BODY and SOUL grid positions.
    Applies player input as a vector for BODY, and the inverted vector for SOUL.
    Performs O(1) wall-based collision detection per entity.

    BODY and SOUL are resolved independently. If one is blocked
        by a wall, the other can still move (per project decision).

    This also checks the win condition (BODY and SOUL occupy the exact same cell).
 */
public class GameplayController {

    private final Cell[][] grid;
    private final int gridSize;

    private GridPoint bodyPos;
    private GridPoint soulPos;

    private boolean won = false;

    public GameplayController(MazeGenerator maze) {
        this.grid = maze.getGrid();
        this.gridSize = maze.getSize();
        randomizeStartPositions();
    }

    // Upon maze generation, the positions of the BODY and SOUL are randomly generated.
    private void randomizeStartPositions() {
        Random random = new Random();
        int minDistance = gridSize / 2; // At least half the grid size apart

        do {
            bodyPos = new GridPoint(random.nextInt(gridSize), random.nextInt(gridSize));
            soulPos = new GridPoint(random.nextInt(gridSize), random.nextInt(gridSize));
        } while (manhattanDistance(bodyPos, soulPos) < minDistance);
    }

    private int manhattanDistance(GridPoint p1, GridPoint p2) {
        return Math.abs(p1.row - p2.row) + Math.abs(p1.col - p2.col);
    } // Added this to ensure that they are always far apart on initial placement :)

    public GridPoint getBodyPos() {
        return bodyPos;
    }

    public GridPoint getSoulPos() {
        return soulPos;
    }

    public boolean isWon() {
        return won;
    }

    // Movement input, using WASD and Arrow keys.
    public boolean handleInput(Direction bodyDirection) {
        if (won) return false;

        Direction soulDirection = bodyDirection.inverted();

        GridPoint bodyBefore = bodyPos;
        GridPoint soulBefore = soulPos;

        boolean bodyMoved = tryMove(true, bodyDirection);
        boolean soulMoved = tryMove(false, soulDirection);

        if (checkWinCondition(bodyBefore, soulBefore)) {
            won = true;
        }

        return bodyMoved || soulMoved;
    } // Fixed the bug where BODY and SOUL just swaps instead of uniting.

    private boolean checkWinCondition(GridPoint bodyBefore, GridPoint soulBefore) {
        boolean sameCell = bodyPos.equals(soulPos);
        boolean swapped = bodyBefore.equals(soulPos) && soulBefore.equals(bodyPos);
        return sameCell || swapped;
    }

    private boolean tryMove(boolean isBody, Direction direction) {
        GridPoint currentPos = isBody ? bodyPos : soulPos;
        Cell currentCell = grid[currentPos.row][currentPos.col];

        // Collision detection: blocked if the wall in this direction is solid.
        if (currentCell.walls[direction.wallIndex]) {
            return false; // blocked by wall, this entity does not move.
        }

        GridPoint nextPos = currentPos.offset(direction.dRow, direction.dCol);

        // Should always pass if walls are consistent, but guards against edge-of-grid instances.
        if (nextPos.row < 0 || nextPos.row >= gridSize
            || nextPos.col < 0 || nextPos.col >= gridSize) {
            return false;
        }

        if (isBody) {
            bodyPos = nextPos;
        } else {
            soulPos = nextPos;
        }
        return true;
    }

    // Win condition: BODY and SOUL occupy the exact same grid tile.
    private boolean checkWinCondition() {
        return bodyPos.equals(soulPos);
    }
}
