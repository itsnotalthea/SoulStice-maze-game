package com.soulstice.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

/**
    This class demonstrates the focal point of the project, which is the Depth-first Search Algorithm.
    Grid size formula:
        size = 9 + 2 * ((level - 1) / 3)

    Levels 1-3  -> 9x9
    Levels 4-6  -> 11x11
    Levels 7-9  -> 13x13
    ... and so on.
 */

public class MazeGenerator {

    private final int size;
    private final Cell[][] grid;

    public MazeGenerator(int level) {
        this.size = calculateGridSize(level);
        this.grid = new Cell[size][size];
        initGrid();
        generateMazeDFS();
    }

    // Changed the formula here. Initially at the demo, it starts at 5x5.
    // Changed it to 9x9 to make the difficulty progression more noticeable.
    public static int calculateGridSize(int level) {
        return 9 + 2 * ((level - 1) / 3);
    }

    public int getSize() {
        return size;
    }

    public Cell[][] getGrid() {
        return grid;
    }

    public Cell getCell(int row, int col) {
        return grid[row][col];
    }

    private void initGrid() {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                grid[r][c] = new Cell(r, c);
            }
        }
    }


    // Depth-First Search with Backtracking Algorithm
    private void generateMazeDFS() {
        Stack<Cell> trailStack = new Stack<>();

        Cell start = grid[0][0];
        start.visited = true;
        trailStack.push(start);

        while (!trailStack.isEmpty()) {
            Cell current = trailStack.peek();
            ArrayList<Cell> unvisitedNeighbors = getUnvisitedNeighbors(current);

            if (!unvisitedNeighbors.isEmpty()) {
                Collections.shuffle(unvisitedNeighbors);
                Cell next = unvisitedNeighbors.get(0);

                removeWallsBetween(current, next);
                next.visited = true;
                trailStack.push(next);
            } else {
                // Dead end - backtrack
                trailStack.pop();
            }
        }
    }

    private ArrayList<Cell> getUnvisitedNeighbors(Cell cell) {
        ArrayList<Cell> neighbors = new ArrayList<>();
        int r = cell.row;
        int c = cell.col;

        if (r - 1 >= 0 && !grid[r - 1][c].visited) neighbors.add(grid[r - 1][c]); // TOP
        if (c + 1 < size && !grid[r][c + 1].visited) neighbors.add(grid[r][c + 1]); // RIGHT
        if (r + 1 < size && !grid[r + 1][c].visited) neighbors.add(grid[r + 1][c]); // BOTTOM
        if (c - 1 >= 0 && !grid[r][c - 1].visited) neighbors.add(grid[r][c - 1]); // LEFT

        return neighbors;
    }

    private void removeWallsBetween(Cell a, Cell b) {
        int dRow = a.row - b.row;
        int dCol = a.col - b.col;

        if (dRow == 1) {         // b is above a
            a.walls[0] = false;  // a's top
            b.walls[2] = false;  // b's bottom
        } else if (dRow == -1) { // b is below a
            a.walls[2] = false;  // a's bottom
            b.walls[0] = false;  // b's top
        }

        if (dCol == 1) {         // b is left of a
            a.walls[3] = false;  // a's left
            b.walls[1] = false;  // b's right
        } else if (dCol == -1) { // b is right of a
            a.walls[1] = false;  // a's right
            b.walls[3] = false;  // b's left
        }
    }
}
