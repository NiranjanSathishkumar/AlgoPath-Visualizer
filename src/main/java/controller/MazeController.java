package controller;

import generator.RecursiveBacktrackerGenerator;
import java.util.Collection;
import model.Cell;
import model.Maze;
import model.MazeComplexity;
import model.dto.MazeResponse;
import model.dto.SolveRequest;
import model.dto.SolverResult;
import model.TerrainType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import service.MazeService;
import solver.AStarSolver;
import solver.BFSSolver;
import solver.DFSSolver;
import solver.DijkstraSolver;
import solver.GreedyBestFirstSolver;
import solver.MazeSolver;
import threading.SolverTask;

@RestController
@RequestMapping("/maze")
public class MazeController {

    private final MazeService mazeService;

    @Autowired
    public MazeController(MazeService mazeService) {
        this.mazeService = mazeService;

        // Auto-initialize a default maze on boot with MEDIUM complexity
        Maze maze = mazeService.createMaze(20, 15);
        mazeService.generateMaze(maze, new RecursiveBacktrackerGenerator(), MazeComplexity.MEDIUM);
    }

    /**
     * Generates a new maze with optional size and complexity parameters.
     *
     * @param width      number of columns (default 30)
     * @param height     number of rows    (default 15)
     * @param complexity loop density — EASY (10%), MEDIUM (20%), HARD (35%).
     *                   Defaults to MEDIUM when not supplied.
     */
    @GetMapping("/generate")
    public ResponseEntity<MazeResponse> generateMaze(
            @RequestParam(name = "width",      defaultValue = "30")     int width,
            @RequestParam(name = "height",     defaultValue = "15")     int height,
            @RequestParam(name = "complexity", defaultValue = "MEDIUM") String complexity) {

        // Parse complexity safely — fall back to MEDIUM for unknown values
        MazeComplexity level;
        try {
            level = MazeComplexity.valueOf(complexity.toUpperCase());
        } catch (IllegalArgumentException e) {
            level = MazeComplexity.MEDIUM;
        }

        Maze maze = mazeService.createMaze(width, height);
        mazeService.generateMaze(maze, new RecursiveBacktrackerGenerator(), level);
        return ResponseEntity.ok(new MazeResponse(maze, "Generated Successfully"));
    }

    @PostMapping("/solve")
    public ResponseEntity<String> solveMaze(@org.springframework.web.bind.annotation.RequestBody(required = false) SolveRequest request) {
        Maze maze = mazeService.getActiveMaze();
        if (maze == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No active maze generated.");
        }

        int startR = 0;
        int startC = 0;
        int endR = maze.getHeight() - 1;
        int endC = maze.getWidth() - 1;

        if (request != null) {
            startR = request.getStartRow();
            startC = request.getStartCol();
            if (request.getEndRow() >= 0) endR = request.getEndRow();
            if (request.getEndCol() >= 0) endC = request.getEndCol();

            if (request.getChangedCells() != null) {
                for (SolveRequest.CellUpdate update : request.getChangedCells()) {
                    Cell c = maze.getCell(update.getRow(), update.getColumn());
                    if (c != null && update.getTerrain() != null) {
                        try {
                            c.setTerrain(TerrainType.valueOf(update.getTerrain().toUpperCase()));
                        } catch (IllegalArgumentException ignored) {}
                    }
                }
            }
        }

        Cell start = maze.getCell(startR, startC);
        Cell end = maze.getCell(endR, endC);
        
        MazeSolver[] algorithmsToRace = {
            new BFSSolver(),
            new DFSSolver(),
            new AStarSolver(),
            new DijkstraSolver(),
            new GreedyBestFirstSolver()
        };
        
        for (MazeSolver solver : algorithmsToRace) {
            SolverTask algorithmicThread = new SolverTask(
                solver, 
                maze, 
                start, 
                end, 
                (task) -> {
                    SolverResult result = new SolverResult(
                        task.getSolverName(),
                        task.getExecutionTime(),
                        task.getVisitedNodes(),
                        task.getPathLength(),
                        task.getPathCost(),
                        task.getSolver().getVisitedOrder(),
                        task.getComputedPath()
                    );
                    mazeService.registerSolverResult(result);
                }
            );
            
            mazeService.executeSolverTask(algorithmicThread);
        }

        return ResponseEntity.accepted().body("Solver Tasks dispatched successfully.");
    }
    
    @GetMapping("/state")
    public ResponseEntity<MazeResponse> getMazeState() {
        Maze maze = mazeService.getActiveMaze();
        if (maze != null) {
            return ResponseEntity.ok(new MazeResponse(maze, "Active"));
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/results")
    public ResponseEntity<Collection<SolverResult>> getResults() {
        return ResponseEntity.ok(mazeService.getResults());
    }
}
