package solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import model.Cell;
import model.Maze;

public class DFSSolver implements MazeSolver {

    private final List<Cell> visitedOrder = new ArrayList<>();

    @Override
    public List<Cell> solve(Maze maze, Cell start, Cell end) {
        visitedOrder.clear();
        Stack<Cell> stack = new Stack<>();
        
        stack.push(start);

        while (!stack.isEmpty()) {
            Cell current = stack.pop();

            // Visit upon popping to visually show the depth trace properly
            if (!current.isVisited()) {
                current.setVisited(true);
                visitedOrder.add(current);

                if (current == end) {
                    return reconstructPath(end);
                }

                for (Cell neighbor : maze.getAccessibleNeighbors(current)) {
                    if (!neighbor.isVisited()) {
                        neighbor.setParent(current);
                        stack.push(neighbor);
                    }
                }
            }
        }

        return new ArrayList<>();
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
