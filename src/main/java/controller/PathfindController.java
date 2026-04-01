package controller;

import model.Cell;
import model.Maze;
import model.dto.PathfindRequest;
import model.dto.SolverResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.MazeService;
import solver.AStarSolver;
import solver.BFSSolver;
import solver.DFSSolver;
import solver.DijkstraSolver;
import solver.GreedyBestFirstSolver;
import solver.MazeSolver;
import java.util.List;

@RestController
@RequestMapping("/api")
public class PathfindController {

    private final MazeService mazeService;

    @Autowired
    public PathfindController(MazeService mazeService) {
        this.mazeService = mazeService;
    }

    @PostMapping("/pathfind")
    public ResponseEntity<SolverResult> singlePathfind(@RequestBody PathfindRequest request) {
        if (request == null || request.getAlgorithm() == null) {
            return ResponseEntity.badRequest().build();
        }

        Maze maze = mazeService.getActiveMaze();
        if (maze == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        MazeSolver solver;
        switch (request.getAlgorithm().toUpperCase()) {
            case "BFS":
            case "BFSSOLVER":
                solver = new BFSSolver();
                break;
            case "DFS":
            case "DFSSOLVER":
                solver = new DFSSolver();
                break;
            case "ASTAR":
            case "ASTARSOLVER":
                solver = new AStarSolver();
                break;
            case "DIJKSTRA":
            case "DIJKSTRASOLVER":
                solver = new DijkstraSolver();
                break;
            case "GREEDY":
            case "GREEDYBESTFIRST":
            case "GREEDYBESTFIRSTSOLVER":
                solver = new GreedyBestFirstSolver();
                break;
            default:
                return ResponseEntity.badRequest().build();
        }

        // Deep copy maze for safe execution
        Maze workingMaze = maze.deepCopy();
        Cell start = workingMaze.getCell(0, 0);
        Cell end = workingMaze.getCell(workingMaze.getHeight() - 1, workingMaze.getWidth() - 1);

        long startTime = System.currentTimeMillis();
        List<Cell> path = solver.solve(workingMaze, start, end);
        long execTime = System.currentTimeMillis() - startTime;

        int cost = 0;
        if (path.size() > 1) {
            for (int i = 1; i < path.size(); i++) {
                cost += path.get(i).getMovementCost();
            }
        }

        SolverResult result = new SolverResult(
            solver.getClass().getSimpleName(),
            execTime,
            solver.getVisitedOrder().size(),
            path.size(),
            cost,
            solver.getVisitedOrder(),
            path
        );

        return ResponseEntity.ok(result);
    }
}
