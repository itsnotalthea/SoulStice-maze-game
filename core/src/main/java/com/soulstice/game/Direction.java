package com.soulstice.game;

/**
    This class represents a movement direction as a (dRow, dCol) vector.
    It shows the wall-index each direction corresponds to on a Cell.

    This is where "Vector Inversion" is implemented: SOUL's direction is
        always the exact opposite of BODY's direction.

    UP:    Body(0,-1) : Soul(0,1)
    DOWN:  Body(0,1)  : Soul(0,-1)
    LEFT:  Body(-1,0) : Soul(1,0)
    RIGHT: Body(1,0)  : Soul(-1,0)

    dRow/dCol uses row = vertical (y), col = horizontal (x).
 */

public enum Direction {
    UP(-1, 0, 0),
    DOWN(1, 0, 2),
    LEFT(0, -1, 3),
    RIGHT(0, 1, 1);

    public final int dRow;
    public final int dCol;
    public final int wallIndex; // which wall of the CURRENT cell this move exits through

    Direction(int dRow, int dCol, int wallIndex) {
        this.dRow = dRow;
        this.dCol = dCol;
        this.wallIndex = wallIndex;
    }

    // Returns the exact opposite direction.
    public Direction inverted() {
        switch (this) {
            case UP:    return DOWN;
            case DOWN:  return UP;
            case LEFT:  return RIGHT;
            case RIGHT: return LEFT;
            default:    throw new IllegalStateException();
        }
    }

    // The wall index on the neighbor cell that faces back toward this move's origin.
    public int oppositeWallIndex() {
        return inverted().wallIndex;
    }
}
