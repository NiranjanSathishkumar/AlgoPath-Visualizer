package model.dto;

import java.util.List;

public class SolveRequest {
    private int startRow = 0;
    private int startCol = 0;
    private int endRow = -1;
    private int endCol = -1;
    private List<CellUpdate> changedCells;

    public int getStartRow() { return startRow; }
    public void setStartRow(int startRow) { this.startRow = startRow; }

    public int getStartCol() { return startCol; }
    public void setStartCol(int startCol) { this.startCol = startCol; }

    public int getEndRow() { return endRow; }
    public void setEndRow(int endRow) { this.endRow = endRow; }

    public int getEndCol() { return endCol; }
    public void setEndCol(int endCol) { this.endCol = endCol; }

    public List<CellUpdate> getChangedCells() { return changedCells; }
    public void setChangedCells(List<CellUpdate> changedCells) { this.changedCells = changedCells; }

    public static class CellUpdate {
        private int row;
        private int column;
        private String terrain; // e.g. "WALL", "GRASS"

        public int getRow() { return row; }
        public void setRow(int row) { this.row = row; }

        public int getColumn() { return column; }
        public void setColumn(int column) { this.column = column; }

        public String getTerrain() { return terrain; }
        public void setTerrain(String terrain) { this.terrain = terrain; }
    }
}
