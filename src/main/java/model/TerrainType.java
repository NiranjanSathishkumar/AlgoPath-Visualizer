package model;

/**
 * Defines the terrain types for the maze, including traversal cost, display 
 * color for the frontend, and whether the terrain can be walked on.
 */
public enum TerrainType {
    
    GRASS(1, "#7CFC00", true),
    SAND(3, "#F4A460", true),
    FOREST(5, "#228B22", true),
    WATER(8, "#1E90FF", true),
    MOUNTAIN(12, "#8B7765", true),
    
    // Integer.MAX_VALUE - 1 to prevent overflow if cost is added later
    WALL(Integer.MAX_VALUE - 1000, "#2F2F2F", false);

    private final int movementCost;
    private final String displayColor;
    private final boolean isWalkable;

    TerrainType(int movementCost, String displayColor, boolean isWalkable) {
        this.movementCost = movementCost;
        this.displayColor = displayColor;
        this.isWalkable = isWalkable;
    }

    public int getMovementCost() {
        return movementCost;
    }

    public String getDisplayColor() {
        return displayColor;
    }

    public boolean isWalkable() {
        return isWalkable;
    }
}
