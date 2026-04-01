package generator;

import model.Maze;

/**
 * Interface outlining the contract for any Maze Generation algorithm.
 */
public interface MazeGenerator {
    /**
     * Generates a maze within the provided Maze instance by knocking down
     * walls between cells according to a unified algorithm behavior.
     * 
     * @param maze Provide a pristine Maze with all walls up.
     */
    void generateMaze(Maze maze);
}
