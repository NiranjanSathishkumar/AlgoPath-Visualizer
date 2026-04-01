package generator;

import model.Maze;
import model.Cell;
import java.util.List;
import java.util.Random;
import java.util.Stack;

/**
 * Implementation of MazeGenerator using Recursive Backtracking (DFS).
 */
public class RecursiveBacktrackerGenerator implements MazeGenerator {

    private final Random random = new Random();

    @Override
    public void generateMaze(Maze maze) {
        // Reset visits in case the maze was already touched
        maze.resetVisited();

        Stack<Cell> stack = new Stack<>();
        
        // 1. Start from random cell
        int startRow = random.nextInt(maze.getHeight());
        int startCol = random.nextInt(maze.getWidth());
        Cell current = maze.getCell(startRow, startCol);
        
        // 2. Mark visited
        current.setVisited(true);
        
        // 5. Push current cell to stack
        stack.push(current);
        
        while (!stack.isEmpty()) {
            current = stack.pop();
            
            // Get unvisited neighbors
            List<Cell> unvisitedNeighbors = maze.getUnvisitedNeighbors(current);
            
            if (!unvisitedNeighbors.isEmpty()) {
                // Push current cell back to stack to allow backtracking
                stack.push(current);
                
                // 3. Choose random unvisited neighbor
                Cell neighbor = unvisitedNeighbors.get(random.nextInt(unvisitedNeighbors.size()));
                
                // 4. Remove wall between cells
                maze.removeWall(current, neighbor);
                
                // 6. Move to neighbor and mark it visited
                neighbor.setVisited(true);
                
                // Push neighbor to stack
                stack.push(neighbor);
            }
            // 7. If no neighbors -> backtrack happens naturally as elements are popped
        }
    }
}
