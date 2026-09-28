package com.soulstice.game;

/*
    This represents a single cell in the maze grid.
    Each cell has its own coordinates, visited status, and four walls.

    Wall indexes:
    0 = TOP
    1 = RIGHT
    2 = BOTTOM
    3 = LEFT
*/

public class Cell {

    public final int row;
    public final int col;

    // true = wall is solid, false = wall has been carved open,
    public boolean[] walls = {true, true, true, true};

    // Only used during maze generation. It is not needed after generation is complete.
    public boolean visited = false;

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public boolean hasWallTop()    { return walls[0]; }
    public boolean hasWallRight()  { return walls[1]; }
    public boolean hasWallBottom() { return walls[2]; }
    public boolean hasWallLeft()   { return walls[3]; }
}
