package com.soulstice.game;

import java.util.Objects;

// Immutable integer (row, col) pair is used to track BODY and SOUL positions.
public class GridPoint {

    public final int row;
    public final int col;

    public GridPoint(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public GridPoint offset(int dRow, int dCol) {
        return new GridPoint(this.row + dRow, this.col + dCol);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GridPoint)) return false;
        GridPoint that = (GridPoint) o;
        return row == that.row && col == that.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public String toString() {
        return "(" + row + ", " + col + ")";
    }
}
