package solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import model.Cell;
import model.Maze;

public class DijkstraSolver implements MazeSolver {

    private final List<Cell> visitedOrder = new ArrayList<>();

    private static class NodeRecord implements Comparable<NodeRecord> {
        Cell cell;
        int cost; // g(n)

        NodeRecord(Cell cell, int cost) {
            this.cell = cell;
            this.cost = cost;
        }

        @Override
        public int compareTo(NodeRecord o) {
            return Integer.compare(this.cost, o.cost);
        }
    }

    @Override
    public List<Cell> solve(Maze maze, Cell start, Cell end) {
        visitedOrder.clear();
        PriorityQueue<NodeRecord> openSet = new PriorityQueue<>();
        Map<Cell, Integer> bestCosts = new HashMap<>();

        bestCosts.put(start, 0);
        openSet.add(new NodeRecord(start, 0));

        while (!openSet.isEmpty()) {
            NodeRecord currentRecord = openSet.poll();
            Cell current = currentRecord.cell;

            if (current.isVisited()) continue; // Already processed entirely

            current.setVisited(true);
            visitedOrder.add(current);

            if (current == end) {
                return reconstructPath(end);
            }

            int currentCost = bestCosts.getOrDefault(current, Integer.MAX_VALUE);

            for (Cell neighbor : maze.getAccessibleNeighbors(current)) {
                if (neighbor.isVisited()) continue;

                int newCost = currentCost + neighbor.getMovementCost();

                if (newCost < bestCosts.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    neighbor.setParent(current);
                    bestCosts.put(neighbor, newCost);
                    openSet.add(new NodeRecord(neighbor, newCost));
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
