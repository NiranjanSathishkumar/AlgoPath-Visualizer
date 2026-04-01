package model.dto;

import model.Maze;

public class MazeResponse {
    private Maze maze;
    private String status;
    private long timestamp;

    public MazeResponse() {}
    
    public MazeResponse(Maze maze, String status) {
        this.maze = maze;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }

    public Maze getMaze() { return maze; }
    public void setMaze(Maze maze) { this.maze = maze; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
