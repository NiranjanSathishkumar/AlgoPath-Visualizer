package solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import model.Cell;
import model.Maze;

public class GreedyBestFirstSolver implements MazeSolver {

    private final List<Cell> visitedOrder = new ArrayList<>();

    private static class NodeRecord implements Comparable<NodeRecord> {
        Cell cell;
        int heuristic; // h(n)

        NodeRecord(Cell cell, int heuristic) {
            this.cell = cell;
            this.heuristic = heuristic;
        }

        @Override
        public int compareTo(NodeRecord o) {
            return Integer.compare(this.heuristic, o.heuristic);
        }
    }

    @Override
    public List<Cell> solve(Maze maze, Cell start, Cell end) {
        visitedOrder.clear();
        PriorityQueue<NodeRecord> openSet = new PriorityQueue<>();
        Set<Cell> closedSet = new HashSet<>();

        openSet.add(new NodeRecord(start, getHeuristic(start, end)));

        while (!openSet.isEmpty()) {
            NodeRecord currentRecord = openSet.poll();
            Cell current = currentRecord.cell;

            if (closedSet.contains(current)) continue;

            // Mark processed
            current.setVisited(true);
            closedSet.add(current);
            visitedOrder.add(current);

            if (current == end) {
                return reconstructPath(end);
            }

            for (Cell neighbor : maze.getAccessibleNeighbors(current)) {
                if (!closedSet.contains(neighbor) && !neighbor.isVisited()) {
                    // Update parent and add to the queue. 
                    // No cost g(n) map needed as it purely looks at the target.
                    // To prevent infinite cycles in some edge cases with duplicate expansions we set parent carefully
                    // However if it's already in the open set with a better path, we ignore this check 
                    // since greedy is suboptimal and blindly uses the first discovered path.
                    
                    // Only update parent if it hasn't been visited/explored
                    if (neighbor.getParent() == null && neighbor != start) {
                        neighbor.setParent(current);
                        openSet.add(new NodeRecord(neighbor, getHeuristic(neighbor, end)));
                    }
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
