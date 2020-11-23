import java.util.List;
import java.util.ArrayList;

public class Board {
    public static final int BOARD_WIDTH = 15;

    public enum Direction {
        RISING, FALLING, DOWN
    }

    public static class Tile {
        private char data;
        private int col;
        private int row;

        public Tile(char data) {
            this.data = data;
            this.col = -1;
            this.row = -1;
        }

        public Tile(char data, int col, int row) {
            this.data = data;
            this.col = col;
            this.row = row;
        }

        public char getData() {
            return this.data;
        }

        public int getCol() {
            return this.col;
        }

        public int getRow() {
            return this.row;
        }

        public int getDia() {
            return (BOARD_WIDTH/2) + this.col - this.row;
        }

        public void setCol(int col) {
            this.col = col;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public int getFile(Direction direction) {
            switch (direction) {
                case RISING:
                    return this.row;
                case FALLING:
                    return this.getDia();
                case DOWN:
                    return this.col;
            }
            return -1;
        }

        public int getPos(Direction direction) {
            switch (direction) {
                case RISING:
                    return this.col;
                case FALLING:
                    return this.col;
                case DOWN:
                    return this.row;
            }
            return -1;
        }
    }

    public static class Strand {
        private int startCol;
        private int startRow;
        private Direction direction;
        private int length;

        public Strand(int startCol, int startRow, Direction direction, int length) {
            this.startCol = startCol;
            this.startRow = startRow;
            this.direction = direction;
            this.length = length;
        }

        public Strand(Direction direction, int file, int minPos, int maxPos) {
            int[] startCoords = getColRowFromFilePos(direction, file, minPos);
            this.startCol = startCoords[0];
            this.startRow = startCoords[1];
            this.direction = direction;
            this.length = 1 + maxPos - minPos;
        }

        public int getStartCol() {
            return this.startCol;
        }

        public int getStartRow() {
            return this.startRow;
        }

        public Direction getDirection() {
            return this.direction;
        }

        public int getLength() {
            return this.length;
        }

        public int[] getCoords() {
            int[] coords = new int[2 * length];
            int col = startCol;
            int row = startRow;
            for (int i = 0; i < length; i++) {
                coords[2*i] = col;
                coords[2*i + 1] = row;
                switch (direction) {
                    case RISING:
                        col++;
                        break;
                    case FALLING:
                        col++;
                        row++;
                        break;
                    case DOWN:
                        row++;
                        break;
                }
            }

            return coords;
        }
    }

    public static final char BLK = '?'; // Blank
    public static final char VOI = '*'; // No hexagon exists
    public static final char EMT = '-'; // No tile exists
    public static final char NBS = '0'; // No bonus
    public static final char DLS = '1'; // Double letter score
    public static final char DWS = '2'; // Double word score
    public static final char TLS = '3'; // Triple letter score
    public static final char TWS = '4'; // Triple word score
    private static final char[][] BONUS_VALUES = {
        { TWS, NBS, NBS, NBS, NBS, DLS, NBS, TWS, VOI, VOI, VOI, VOI, VOI, VOI, VOI },
        { NBS, NBS, DLS, NBS, DWS, NBS, NBS, NBS, NBS, VOI, VOI, VOI, VOI, VOI, VOI },
        { NBS, DLS, NBS, NBS, NBS, NBS, TLS, NBS, NBS, DLS, VOI, VOI, VOI, VOI, VOI },
        { NBS, NBS, NBS, TLS, NBS, DWS, NBS, NBS, TLS, NBS, NBS, VOI, VOI, VOI, VOI },
        { NBS, DWS, NBS, NBS, NBS, NBS, NBS, DLS, NBS, NBS, DWS, NBS, VOI, VOI, VOI },
        { DLS, NBS, NBS, DWS, NBS, NBS, DLS, NBS, NBS, DWS, NBS, NBS, NBS, VOI, VOI },
        { NBS, NBS, TLS, NBS, NBS, DLS, NBS, NBS, DLS, NBS, NBS, NBS, DLS, NBS, VOI },
        { TWS, NBS, NBS, NBS, DLS, NBS, NBS, DWS, NBS, NBS, NBS, TLS, NBS, NBS, TWS },
        { VOI, NBS, NBS, TLS, NBS, NBS, DLS, NBS, NBS, DLS, NBS, NBS, NBS, DLS, NBS },
        { VOI, VOI, DLS, NBS, NBS, DWS, NBS, NBS, DLS, NBS, NBS, DWS, NBS, NBS, NBS },
        { VOI, VOI, VOI, NBS, DWS, NBS, NBS, NBS, NBS, NBS, DLS, NBS, NBS, DWS, NBS },
        { VOI, VOI, VOI, VOI, NBS, NBS, NBS, TLS, NBS, DWS, NBS, NBS, TLS, NBS, NBS },
        { VOI, VOI, VOI, VOI, VOI, NBS, DLS, NBS, NBS, NBS, NBS, TLS, NBS, NBS, DLS },
        { VOI, VOI, VOI, VOI, VOI, VOI, NBS, NBS, DLS, NBS, DWS, NBS, NBS, NBS, NBS },
        { VOI, VOI, VOI, VOI, VOI, VOI, VOI, TWS, NBS, NBS, NBS, NBS, DLS, NBS, TWS }
    };
    private static final char[][] BLANK_BOARD = {
        { EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, VOI, VOI, VOI, VOI, VOI, VOI, VOI },
        { EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, VOI, VOI, VOI, VOI, VOI, VOI },
        { EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, VOI, VOI, VOI, VOI, VOI },
        { EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, VOI, VOI, VOI, VOI },
        { EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, VOI, VOI, VOI },
        { EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, VOI, VOI },
        { EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, VOI },
        { EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT },
        { VOI, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT },
        { VOI, VOI, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT },
        { VOI, VOI, VOI, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT },
        { VOI, VOI, VOI, VOI, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT },
        { VOI, VOI, VOI, VOI, VOI, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT },
        { VOI, VOI, VOI, VOI, VOI, VOI, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT },
        { VOI, VOI, VOI, VOI, VOI, VOI, VOI, EMT, EMT, EMT, EMT, EMT, EMT, EMT, EMT }
    };

    public char[][] data;
    public List<Tile> newTiles;

    public Board() {
        data = BLANK_BOARD;
    }

    public Board(Board oldBoard) {
        for (int row = 0; row < BOARD_WIDTH; row++) {
            for (int col = 0; col < BOARD_WIDTH; col++) {
                this.data[row][col] = oldBoard.data[row][col];
            }
        }
    }

    public static int[] getColRowFromFilePos(Direction direction, int file, int pos) {
        int[] coords = new int[2];
        int col = -1;
        int row = -1;
        switch (direction) {
            case RISING:
                col = pos;
                row = file;
                break;
            case FALLING:
                col = pos;
                row = (BOARD_WIDTH/2) + pos - file;
                break;
            case DOWN:
                col = file;
                row = pos;
                break;
        }
        coords[0] = col;
        coords[1] = row;
        return coords;
    }

    public char getCharAt(int col, int row) {
        return this.data[row][col];
    }

    public char getCharAt(Direction direction, int file, int pos) {
        int[] coords = getColRowFromFilePos(direction, file, pos);
        return this.data[coords[1]][coords[0]];
    }

    public boolean isEmpty(int col, int row) {
        if (col < 0 || col >= BOARD_WIDTH) {
            return false;
        }
        if (row < 0 || row >= BOARD_WIDTH) {
            return false;
        }
        return (this.data[row][col] == EMT);
    }

    public boolean isValid(int col, int row) {
        if (col < 0 || col >= BOARD_WIDTH) {
            return false;
        }
        if (row < 0 || row >= BOARD_WIDTH) {
            return false;
        }
        return (this.data[row][col] != VOI);
    }

    public boolean checkStrand(Strand strand) {
        int[] coords = strand.getCoords();
        for (int i = 0; i < coords.length/2; i++) {
            if (this.data[2*i + 1][2*i] < 'A') return false;
        }
        return true;
    }

    /*
        Finds all Strands that have been created by the play and returns a List
        of them. If the play is invalid, a null List is returned instead.
    */
    /*
    public List<Strand> checkTilePlacement(List<Tile> newTiles) {
        Board nextBoard = new Board(this);
        Board freshBoard = new Board(); // Tracks placement of new tiles
        for (Tile tile : newTiles) {
            if (nextBoard.placeTile(tile) == false) {
                return null;
            }
            freshBoard.placeTile(tile);
        }

        List<Strand> strands = new ArrayList<Strand>();

        Direction direction = null;
        int file; // row index, col index, or dia index

        // Finds direction of first two tiles
        if (newTiles.size() >= 2) {
            Tile first = newTiles.get(0);
            Tile second = newTiles.get(1);
            if (first.getRow() == second.getRow()) {
                direction = Direction.RISING;
                file = first.getRow();
            }
            else if (first.getDia() == second.getDia()) {
                direction = Direction.FALLING;
                file = first.getDia();
            }
            else if (first.getCol() == second.getCol()) {
                direction = Direction.DOWN;
                file = first.getCol();
            }
            else return null; // Tiles do not make a line, so play is invalid

            int minPos = first.getPos(direction);
            Tile minTile = first;
            int maxPos = minPos;
            Tile maxTile = minTile;
            for (Tile tile : newTiles) {
                if (tile.getFile(direction) != file) return null; // Tiles do not line up, so play is invalid
                int pos = tile.getPos(direction);
                if (pos > maxPos) {
                    maxPos = pos;
                    maxTile = tile;
                }
                else if (pos < minPos) {
                    minPos = pos;
                    minTile = tile;
                }
            }

            // Find start to Strand formed by all tiles
            while (nextBoard.getCharAt(direction, file, --minPos) >= 'A') {}
            minPos++;

            // Find end to Strand formed by all Tiles
            while (nextBoard.getCharAt(direction, file, ++maxPos) >= 'A') {}
            maxPos--;

            // Add the strand formed by all tiles and affixes
            Strand coreStrand = new Strand(direction, file, minPos, maxPos);
        }
    }
    */

    public void addTile(char data, int col, int row) {
        Tile tile = new Tile(data, col, row);
        newTiles.add(tile);
    }

    public boolean placeTile(Tile tile) {
        if (tile.getRow() == -1 && tile.getCol() == -1) {
            return false;
        }
        if (data[tile.getRow()][tile.getCol()] == EMT) {
            data[tile.getRow()][tile.getCol()] = tile.getData();
            return true;
        }
        else return false;
    }

    public void clearBoard() {
        data = BLANK_BOARD;
    }

    public static char getBonusValue(int x, int y) {
        return BONUS_VALUES[y][x];
    }
}
