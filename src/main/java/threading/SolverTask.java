package threading;

import java.util.List;
import java.util.function.Consumer;
import model.Cell;
import model.Maze;
import solver.MazeSolver;

/**
 * Runnable Task that wraps a MazeSolver execution.
 * Collects analytical metrics independently on its bound thread
 * then fires a callback mapping state back to the Controller/UI.
 */
public class SolverTask implements Runnable {

    private final MazeSolver solver;
    private final Maze maze;
    private final Cell start;
    private final Cell end;
    
    // Configured Event Callback
    private final Consumer<SolverTask> onComplete;

    // Metrics
    private long executionTime;
    private int visitedNodes;
    private int pathLength;
    private int pathCost;
    private List<Cell> computedPath;

    public SolverTask(MazeSolver solver, Maze originalMaze, Cell start, Cell end, Consumer<SolverTask> onComplete) {
        this.solver = solver;
        
        // Deep clone protects simultaneous runs from overwriting visitation states
        this.maze = originalMaze.deepCopy();
        
        // Align new starting / ending references directly to the copied space
        this.start = this.maze.getCell(start.getRow(), start.getColumn());
        this.end = this.maze.getCell(end.getRow(), end.getColumn());
        
        this.onComplete = onComplete;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        
        // Execute pathfinding mapping
        this.computedPath = solver.solve(maze, start, end);
        
        this.executionTime = System.currentTimeMillis() - startTime;
        this.visitedNodes = solver.getVisitedOrder().size();
        this.pathLength = computedPath.size();
        
        // Calculate path cost (excluding start cell, which is free to stand on)
        this.pathCost = 0;
        if (computedPath.size() > 1) {
            for (int i = 1; i < computedPath.size(); i++) {
                this.pathCost += computedPath.get(i).getMovementCost();
            }
        }
        
        // Fire Completion Event dynamically
        if (onComplete != null) {
            onComplete.accept(this);
        }
    }

    // --- Metric Accessors ---
    public long getExecutionTime() { return executionTime; }
    public int getVisitedNodes() { return visitedNodes; }
    public int getPathLength() { return pathLength; }
    public int getPathCost() { return pathCost; }
    public List<Cell> getComputedPath() { return computedPath; }
    public String getSolverName() { return solver.getClass().getSimpleName(); }
    public MazeSolver getSolver() { return solver; }
}
