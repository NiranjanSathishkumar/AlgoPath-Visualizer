package util;

import model.Cell;
import model.Maze;

/**
 * Utility functions for decoupled deep-copying and helper mechanics.
 */
public class MazeUtils {
    
    /**
     * Performs a deep copy of a maze instance, preventing
     * threads from mutating shared cells during simultaneous race paths.
     */
    public static Maze copyMaze(Maze original) {
        Maze copy = new Maze(original.getWidth(), original.getHeight());
        
        for (int r = 0; r < original.getHeight(); r++) {
            for (int c = 0; c < original.getWidth(); c++) {
                Cell origCell = original.getCell(r, c);
                Cell newCell = copy.getCell(r, c);
                
                // Copy Wall states
                newCell.setTopWall(origCell.hasTopWall());
                newCell.setRightWall(origCell.hasRightWall());
                newCell.setBottomWall(origCell.hasBottomWall());
                newCell.setLeftWall(origCell.hasLeftWall());
                
                // State resets (copy is fresh for solving)
                newCell.setVisited(false);
                newCell.setParent(null);
            }
        }
        
        return copy;
    }

    /**
     * Prints an ASCII representation of the Maze to the console.
     */
    public static void printMaze(Maze maze) {
        int width = maze.getWidth();
        int height = maze.getHeight();

        for (int r = 0; r < height; r++) {
            // Draw top walls
            for (int c = 0; c < width; c++) {
                Cell cell = maze.getCell(r, c);
                System.out.print("+");
                System.out.print(cell.hasTopWall() ? "---" : "   ");
            }
            System.out.println("+");

            // Draw left/right walls and cell spaces
            for (int c = 0; c < width; c++) {
                Cell cell = maze.getCell(r, c);
                System.out.print(cell.hasLeftWall() ? "|" : " ");
                System.out.print("   "); // The cell's interior space
            }
            // Draw the right-most wall of the row
            Cell lastCell = maze.getCell(r, width - 1);
            System.out.println(lastCell.hasRightWall() ? "|" : " ");
        }

        // Draw the bottom walls of the last row
        for (int c = 0; c < width; c++) {
            Cell cell = maze.getCell(height - 1, c);
            System.out.print("+");
            System.out.print(cell.hasBottomWall() ? "---" : "   ");
        }
        System.out.println("+");
    }
}
