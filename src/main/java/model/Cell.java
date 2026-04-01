package model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a single cell/node in the maze grid.
 * Encapsulates positional state, wall boundaries, and traversal history
 * for algorithms to use.
 */
public class Cell {
    private final int row;
    private final int column;

    // Walls around the cell. True means the wall is intact.
    @JsonProperty("topWall")
    private boolean topWall = true;
    @JsonProperty("rightWall")
    private boolean rightWall = true;
    @JsonProperty("bottomWall")
    private boolean bottomWall = true;
    @JsonProperty("leftWall")
    private boolean leftWall = true;

    // Used by maze generation and pathfinding algorithms.
    // Making it volatile provides some thread-safety for basic concurrent reads, 
    // but full data cloning is recommended for simultaneous solver executions.
    private volatile boolean visited = false;

    // Movement cost tracking
    @JsonProperty("terrain")
    private TerrainType terrain = TerrainType.GRASS;

    // Reference used to backtrack paths in solving algorithms like BFS/A*.
    @JsonIgnore
    private Cell parent = null;

    public Cell(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public boolean hasTopWall() { return topWall; }
    public void setTopWall(boolean topWall) { this.topWall = topWall; }

    public boolean hasRightWall() { return rightWall; }
    public void setRightWall(boolean rightWall) { this.rightWall = rightWall; }

    public boolean hasBottomWall() { return bottomWall; }
    public void setBottomWall(boolean bottomWall) { this.bottomWall = bottomWall; }

    public boolean hasLeftWall() { return leftWall; }
    public void setLeftWall(boolean leftWall) { this.leftWall = leftWall; }

    public boolean isVisited() { return visited; }
    public void setVisited(boolean visited) { this.visited = visited; }

    public TerrainType getTerrain() { return terrain; }
    public void setTerrain(TerrainType terrain) { this.terrain = terrain; }

    @JsonProperty("cost")
    public int getMovementCost() { return terrain.getMovementCost(); }

    @JsonIgnore
    public Cell getParent() { return parent; }
    public void setParent(Cell parent) { this.parent = parent; }
    
    // Additional utility methods like equals/hashCode could be added for Collections matching
}
