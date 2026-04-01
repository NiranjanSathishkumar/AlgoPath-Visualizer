package model.dto;

import java.util.List;
import model.Cell;

public class SolverResult {
    private String algorithmName;
    private long executionTimeMs;
    private int nodesExplored;
    private int pathLength;
    private int pathCost;
    private List<Cell> visitedOrder;
    private List<Cell> finalPath;

    public SolverResult() {}

    public SolverResult(String algorithmName, long executionTimeMs, int nodesExplored, int pathLength, int pathCost, List<Cell> visitedOrder, List<Cell> finalPath) {
        this.algorithmName = algorithmName;
        this.executionTimeMs = executionTimeMs;
        this.nodesExplored = nodesExplored;
        this.pathLength = pathLength;
        this.pathCost = pathCost;
        this.visitedOrder = visitedOrder;
        this.finalPath = finalPath;
    }

    public String getAlgorithmName() { return algorithmName; }
    public void setAlgorithmName(String algorithmName) { this.algorithmName = algorithmName; }

    public long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }

    public int getNodesExplored() { return nodesExplored; }
    public void setNodesExplored(int nodesExplored) { this.nodesExplored = nodesExplored; }

    public int getPathLength() { return pathLength; }
    public void setPathLength(int pathLength) { this.pathLength = pathLength; }

    public int getPathCost() { return pathCost; }
    public void setPathCost(int pathCost) { this.pathCost = pathCost; }

    public List<Cell> getVisitedOrder() { return visitedOrder; }
    public void setVisitedOrder(List<Cell> visitedOrder) { this.visitedOrder = visitedOrder; }

    public List<Cell> getFinalPath() { return finalPath; }
    public void setFinalPath(List<Cell> finalPath) { this.finalPath = finalPath; }
}
