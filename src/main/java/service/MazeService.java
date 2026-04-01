package service;

import generator.MazeGenerator;
import generator.TerrainGenerator;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import model.Maze;
import model.MazeComplexity;
import model.dto.SolverResult;
import org.springframework.stereotype.Service;
import threading.SolverTask;

@Service
public class MazeService {
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    
    // Global Application State
    private Maze activeMaze;
    private final Map<String, SolverResult> latestResults = new ConcurrentHashMap<>();

    public Maze createMaze(int width, int height) {
        this.activeMaze = new Maze(width, height);
        this.latestResults.clear(); // Reset results on new generation
        return this.activeMaze;
    }
    
    /**
     * Generates a perfect maze using the supplied generator, then adds random
     * loops according to the given {@link MazeComplexity}.
     *
     * <p>Process:
     * <ol>
     *   <li>Run the generator — produces a perfect (tree) maze with a single
     *       solution between any two cells.
     *   <li>Call {@link Maze#addRandomLoops} — removes extra walls to create
     *       cycles, giving BFS, DFS and A* genuinely different paths to find.
     * </ol>
     *
     * @param maze       a freshly created {@link Maze} (all walls up)
     * @param generator  the algorithm used to carve the initial spanning tree
     * @param complexity controls loop density ({@code EASY} / {@code MEDIUM} /
     *                   {@code HARD})
     */
    public void generateMaze(Maze maze, MazeGenerator generator, MazeComplexity complexity) {
        // Step 1 — carve the perfect maze (spanning tree, one unique solution)
        generator.generateMaze(maze);

        // Step 2 — punch extra passages to create cycles and multiple solutions
        int loops = complexity.loopCount(maze.getWidth(), maze.getHeight());
        maze.addRandomLoops(loops);

        // Step 3 — apply weighted terrain
        new TerrainGenerator().assignTerrain(maze);
    }

    /**
     * Convenience overload that defaults to {@link MazeComplexity#MEDIUM}.
     * Existing callers that do not care about complexity level are unaffected.
     */
    public void generateMaze(Maze maze, MazeGenerator generator) {
        generateMaze(maze, generator, MazeComplexity.MEDIUM);
    }
    
    public Maze getActiveMaze() {
        return activeMaze;
    }

    public void executeSolverTask(SolverTask task) {
        executorService.submit(task);
    }
    
    public void registerSolverResult(SolverResult result) {
        latestResults.put(result.getAlgorithmName(), result);
    }
    
    public Collection<SolverResult> getResults() {
        return latestResults.values();
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
