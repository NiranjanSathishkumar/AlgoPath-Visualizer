package model;

/**
 * Controls how many extra "loop" passages are punched through the maze after
 * the perfect-maze generation step.
 *
 * <p><b>Why does this matter?</b><br>
 * A perfect maze (produced by Recursive Backtracking) is a spanning tree:
 * there is exactly ONE path between any two cells.  Because there is only one
 * solution, BFS, DFS and A* all find the identical route — the differences in
 * their behaviour are invisible.
 *
 * <p>By removing additional walls after generation we introduce <em>cycles</em>
 * (multiple possible routes from start to end).  This lets each algorithm show
 * its real character:
 * <ul>
 *   <li><b>BFS</b>  — always explores level-by-level and guarantees the
 *       <em>shortest</em> path.
 *   <li><b>DFS</b>  — dives deep along one branch; on a loopy maze it often
 *       finds a correct but <em>longer</em> path.
 *   <li><b>A*</b>   — uses a heuristic to guide the search; typically finds the
 *       shortest path faster than BFS by exploring fewer cells.
 * </ul>
 *
 * <p><b>Loop density as a fraction of total cells:</b>
 * <pre>
 *   EASY   →  ~10 %  (a few extra routes; maze stays mostly tree-like)
 *   MEDIUM →  ~20 %  (clear differences between algorithms)
 *   HARD   →  ~35 %  (many alternate routes; almost labyrinthine)
 * </pre>
 */
public enum MazeComplexity {

    /**
     * ~10 % extra connections.
     * The maze still feels like a classic puzzle with mostly one solution,
     * but BFS vs DFS path lengths will occasionally differ.
     */
    EASY(0.10),

    /**
     * ~20 % extra connections (default).
     * Enough loops to consistently show different algorithm behaviours
     * without making the maze feel too open.
     */
    MEDIUM(0.20),

    /**
     * ~35 % extra connections.
     * A dense web of alternate paths; BFS, DFS and A* will almost always
     * explore very different regions of the maze before finding their paths.
     */
    HARD(0.35);

    /** Fraction of (width × height) cells used as the loop count. */
    private final double loopFraction;

    MazeComplexity(double loopFraction) {
        this.loopFraction = loopFraction;
    }

    /**
     * Calculates the concrete number of loops to add for a given maze size.
     *
     * @param width  maze width  (columns)
     * @param height maze height (rows)
     * @return number of extra walls to remove
     */
    public int loopCount(int width, int height) {
        return (int) Math.round(width * height * loopFraction);
    }
}
