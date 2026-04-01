package generator;

import java.util.Random;
import model.Cell;
import model.Maze;
import model.TerrainType;

public class TerrainGenerator {
    
    private final Random random = new Random();

    /**
     * Iterates over all cells in the maze and assigns a random terrain type
     * according to the specified distribution rules.
     * 
     * @param maze the maze to apply terrains to
     */
    public void assignTerrain(Maze maze) {
        int width = maze.getWidth();
        int height = maze.getHeight();
        
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                Cell cell = maze.getCell(r, c);
                
                // Keep Start and End cells as GRASS
                if ((r == 0 && c == 0) || (r == height - 1 && c == width - 1)) {
                    cell.setTerrain(TerrainType.GRASS);
                    continue;
                }
                
                // 60% GRASS, 15% SAND, 10% FOREST, 10% WATER, 5% MOUNTAIN
                double roll = random.nextDouble();
                if (roll < 0.60) {
                    cell.setTerrain(TerrainType.GRASS);
                } else if (roll < 0.75) {
                    cell.setTerrain(TerrainType.SAND);
                } else if (roll < 0.85) {
                    cell.setTerrain(TerrainType.FOREST);
                } else if (roll < 0.95) {
                    cell.setTerrain(TerrainType.WATER);
                } else {
                    cell.setTerrain(TerrainType.MOUNTAIN);
                }
            }
        }
    }
}
