package solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import model.Cell;
import model.Maze;

public class AStarSolver implements MazeSolver {

    private final List<Cell> visitedOrder = new ArrayList<>();

    private static class Node implements Comparable<Node> {
        Cell cell;
        int fScore; // g + h
        
        Node(Cell cell, int fScore) {
            this.cell = cell;
            this.fScore = fScore;
        }

        @Override
        public int compareTo(Node o) {
            return Integer.compare(this.fScore, o.fScore);
        }
    }

    @Override
    public List<Cell> solve(Maze maze, Cell start, Cell end) {
        visitedOrder.clear();
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Map<Cell, Integer> gScores = new HashMap<>();

        // Initialization
        gScores.put(start, 0);
        openSet.add(new Node(start, getHeuristic(start, end)));
        
        // Use a boolean array or just the native visited flag for closed set logically
        
        while (!openSet.isEmpty()) {
            Node currentWrapper = openSet.poll();
            Cell current = currentWrapper.cell;

            if (current.isVisited()) continue; // Already processed
            
            // Mark processed
            current.setVisited(true);
            visitedOrder.add(current);

            if (current == end) {
                return reconstructPath(end);
            }

            int currentG = gScores.getOrDefault(current, Integer.MAX_VALUE);

            for (Cell neighbor : maze.getAccessibleNeighbors(current)) {
                if (neighbor.isVisited()) continue; // Ignore fully evaluated nodes

                int tentativeG = currentG + neighbor.getMovementCost(); // Cost depends on terrain type

                if (tentativeG < gScores.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    // This path to neighbor is better than any previous one
                    neighbor.setParent(current);
                    gScores.put(neighbor, tentativeG);
                    
                    int fScore = tentativeG + getHeuristic(neighbor, end);
                    openSet.add(new Node(neighbor, fScore));
                }
            }
        }

        return new ArrayList<>();
    }
    
    // Manhattan Distance Heuristic
    private int getHeuristic(Cell current, Cell end) {
        return Math.abs(current.getRow() - end.getRow()) + Math.abs(current.getColumn() - end.getColumn());
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
