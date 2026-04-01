package solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import model.Cell;
import model.Maze;

public class BFSSolver implements MazeSolver {

    private final List<Cell> visitedOrder = new ArrayList<>();

    @Override
    public List<Cell> solve(Maze maze, Cell start, Cell end) {
        visitedOrder.clear();
        Queue<Cell> queue = new LinkedList<>();

        queue.add(start);
        start.setVisited(true);
        visitedOrder.add(start);

        while (!queue.isEmpty()) {
            Cell current = queue.poll();

            if (current == end) {
                return reconstructPath(end);
            }

            for (Cell neighbor : maze.getAccessibleNeighbors(current)) {
                if (!neighbor.isVisited()) {
                    neighbor.setVisited(true);
                    neighbor.setParent(current);
                    
                    visitedOrder.add(neighbor); // Track for visualization
                    queue.add(neighbor);
                }
            }
        }
        
        return new ArrayList<>(); // Path not found
    }

    @Override
    public List<Cell> getVisitedOrder() {
        return visitedOrder;
    }

    private List<Cell> reconstructPath(Cell end) {
        List<Cell> path = new ArrayList<>();
        Cell current = end;
        while (current != null) {
            path.add(current);
            current = current.getParent();
        }
        Collections.reverse(path);
        return path;
    }
}
