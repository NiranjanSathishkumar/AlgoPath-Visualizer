package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The core data structure embodying the maze.
 * Holds a 2D grid of Cells and provides abstractions to query adjacencies.
 */
public class Maze {
    private final int width;
    private final int height;
    private final Cell[][] grid;

    /** Shared Random instance used only for addRandomLoops. */
    private final Random loopRng = new Random();

    public Maze(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new Cell[height][width];
        initializeGrid();
    }

    private void initializeGrid() {
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                grid[row][col] = new Cell(row, col);
            }
        }
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    
    public Cell[][] getGrid() { return grid; }
    
    public Cell getCell(int row, int col) {
        if (isValidCoordinate(row, col)) {
            return grid[row][col];
        }
        return null;
    }

    /**
     * Gets all adjacent logical neighbors within bounds. 
     * Useful for maze generation before walls are knocked down.
     */
    public List<Cell> getNeighbors(Cell cell) {
        List<Cell> neighbors = new ArrayList<>();
        int row = cell.getRow();
        int col = cell.getColumn();

        if (isValidCoordinate(row - 1, col)) neighbors.add(grid[row - 1][col]); // Top
        if (isValidCoordinate(row, col + 1)) neighbors.add(grid[row][col + 1]); // Right
        if (isValidCoordinate(row + 1, col)) neighbors.add(grid[row + 1][col]); // Bottom
        if (isValidCoordinate(row, col - 1)) neighbors.add(grid[row][col - 1]); // Left

        return neighbors;
    }

    /**
     * Gets all adjacent neighbors that have not been visited yet.
     */
    public List<Cell> getUnvisitedNeighbors(Cell cell) {
        List<Cell> unvisited = new ArrayList<>();
        for (Cell neighbor : getNeighbors(cell)) {
            if (!neighbor.isVisited()) {
                unvisited.add(neighbor);
            }
        }
        return unvisited;
    }

    /**
     * Removes the shared wall between two adjacent cells.
     */
    public void removeWall(Cell current, Cell next) {
        if (current.getRow() == next.getRow()) {
            if (current.getColumn() > next.getColumn()) {
                // Next is to the left of Current
                current.setLeftWall(false);
                next.setRightWall(false);
            } else {
                // Next is to the right of Current
                current.setRightWall(false);
                next.setLeftWall(false);
            }
        } else if (current.getColumn() == next.getColumn()) {
            if (current.getRow() > next.getRow()) {
                // Next is above Current
                current.setTopWall(false);
                next.setBottomWall(false);
            } else {
                // Next is below Current
                current.setBottomWall(false);
                next.setTopWall(false);
            }
        }
    }

    /**
     * Optional: gets reachable neighbors (where no walls block the path).
     * This is highly useful for Solver classes.
     */
    public List<Cell> getAccessibleNeighbors(Cell cell) {
        List<Cell> neighbors = new ArrayList<>();
        int row = cell.getRow();
        int col = cell.getColumn();

        if (!cell.hasTopWall() && isValidCoordinate(row - 1, col) && grid[row - 1][col].getTerrain().isWalkable()) neighbors.add(grid[row - 1][col]);
        if (!cell.hasRightWall() && isValidCoordinate(row, col + 1) && grid[row][col + 1].getTerrain().isWalkable()) neighbors.add(grid[row][col + 1]);
        if (!cell.hasBottomWall() && isValidCoordinate(row + 1, col) && grid[row + 1][col].getTerrain().isWalkable()) neighbors.add(grid[row + 1][col]);
        if (!cell.hasLeftWall() && isValidCoordinate(row, col - 1) && grid[row][col - 1].getTerrain().isWalkable()) neighbors.add(grid[row][col - 1]);
        
        return neighbors;
    }

    private boolean isValidCoordinate(int row, int col) {
        return row >= 0 && row < height && col >= 0 && col < width;
    }

    /**
     * Resets visited flags and parent references so different algorithms 
     * can run back-to-back on the same maze logic.
     */
    public void resetVisited() {
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Cell c = grid[row][col];
                c.setVisited(false);
                c.setParent(null);
            }
        }
    }

    /**
     * Introduces cycles (loops) into the maze by randomly removing walls between
     * adjacent cells.
     *
     * <p><b>Why loops?</b><br>
     * A perfect maze produced by Recursive Backtracking is a spanning tree —
     * there is exactly one path between any two cells.  Because there is only
     * one solution, BFS, DFS and A* all find the identical route, making it
     * impossible to observe the differences in their behaviour.
     *
     * <p>Removing additional walls creates <em>cycles</em> in the maze graph so
     * that multiple paths exist between start and end:
     * <ul>
     *   <li>BFS will still find the <em>shortest</em> path (fewest steps).
     *   <li>DFS may commit to a long branch and return a <em>longer</em> path.
     *   <li>A* uses a heuristic and typically reaches the goal while exploring
     *       fewer cells than BFS.
     * </ul>
     *
     * <p><b>Wall removal is always symmetric:</b> when the shared wall between
     * two adjacent cells is removed, both sides of that wall are cleared to keep
     * the maze graph consistent and to avoid one-way passages.
     *
     * <p><b>Safety guarantees:</b>
     * <ul>
     *   <li>Out-of-bounds coordinates are never accessed.
     *   <li>Only immediately adjacent neighbours (±1 row or ±1 column) are
     *       considered — diagonal pairs cannot share a wall and are skipped.
     *   <li>The total number of attempts is capped at {@code loopCount * 3} to
     *       avoid an infinite loop when the maze is very small.
     * </ul>
     *
     * @param loopCount number of extra passages to carve.  Use
     *                  {@link model.MazeComplexity#loopCount(int, int)} to
     *                  obtain a density-appropriate value for a given maze size.
     */
    public void addRandomLoops(int loopCount) {
        if (loopCount <= 0) return;

        int added   = 0;
        int maxTries = loopCount * 3; // safety cap — prevents infinite loop on tiny mazes
        int tries   = 0;

        while (added < loopCount && tries < maxTries) {
            tries++;

            // Pick a random cell anywhere in the grid.
            int row = loopRng.nextInt(height);
            int col = loopRng.nextInt(width);
            Cell cell = grid[row][col];

            // Collect the four potential neighbours (bounded by grid edges).
            List<Cell> neighbours = getNeighbors(cell);
            if (neighbours.isEmpty()) continue;

            // Choose one neighbour at random.
            Cell neighbour = neighbours.get(loopRng.nextInt(neighbours.size()));

            // Only adjacent cells (same row ±1 col, or same col ±1 row) can
            // share a wall.  getNeighbors guarantees this, so we can proceed
            // directly.  removeWall handles wall-direction detection internally
            // and always clears both sides symmetrically.
            removeWall(cell, neighbour);
            added++;
        }
    }

    /**
     * Performs a deep dive copy of this maze to allow isolated pathfinding
     * execution on multithreaded systems.
     */
    public Maze deepCopy() {
        Maze copy = new Maze(this.width, this.height);
        
        for (int r = 0; r < this.height; r++) {
            for (int c = 0; c < this.width; c++) {
                Cell orig = this.getCell(r, c);
                Cell cloned = copy.getCell(r, c);
                
                cloned.setTopWall(orig.hasTopWall());
                cloned.setRightWall(orig.hasRightWall());
                cloned.setBottomWall(orig.hasBottomWall());
                cloned.setLeftWall(orig.hasLeftWall());

                // Clone terrain type! This was missing and caused algorithms to act on GRASS-only grids
                cloned.setTerrain(orig.getTerrain());
                
                cloned.setVisited(false);
                cloned.setParent(null);
            }
        }
        
        return copy;
    }
}
