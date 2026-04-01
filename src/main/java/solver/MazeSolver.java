package solver;

import java.util.List;
import model.Cell;
import model.Maze;

/**
 * Interface standardizing How algorithms parse a generated Maze.
 */
public interface MazeSolver {

    /**
     * Executes the specific pathfinding routine from start to finish.
     * @param maze The initialized structure where walls determine pathing.
     * @param start The entry block.
     * @param end The destination block.
     * @return Ordered path of Cells travelled, or empty/null if no path is found.
     */
    List<Cell> solve(Maze maze, Cell start, Cell end);
    
    /**
     * Retrieves the chronological order of cells visited during the search.
     * Used exclusively for animation and visualization.
     * @return List of Cells in the exact order they were explored.
     */
    List<Cell> getVisitedOrder();
}
