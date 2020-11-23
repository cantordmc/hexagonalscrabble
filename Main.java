import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import java.util.EmptyStackException;


import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.lang.Math;

public class Main extends Application {
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;

    private static final int BOARD_X_OFFSET = 175;
    private static final int BOARD_Y_OFFSET = 115;
    private static final int CURSOR_X_OFFSET = 3;
    private static final int CURSOR_Y_OFFSET = -1;
    private static final int RACK_X_OFFSET = 200;
    private static final int RACK_Y_OFFSET = 500;

    private static final int RACK_TILE_WIDTH = 50;
    private static final int RACK_TILE_GAP = 5;

    private static final int SPACE_WIDTH = 32;
    private static final int SPACE_OVERLAP = 2;

    private static final double TILE_ASPECT_RATIO = 1.16;
    private static final double UNPLACED_TILE_SIZE = .8;


    private Pane window;
    private Group boardGroup;
    private Group boardTileGroup;
    private Group rackTileGroup;
    private Rectangle background;

    private Game game;
    private RackTile[] rackTiles;
    private Stack<RackTile> placedTiles;
    private Space[][] gameBoard;
    private BoardTile[][] boardTiles;
    private Cursor cursor;
    private int currPlayerIndex = 0;

    @Override
    public void start(Stage stage) throws FileNotFoundException {
        background = new Rectangle(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        background.setFill(Color.WHITE);
        background.setOnMouseClicked(e -> {
            cursor.deactivate();
        });
        game = new Game();
        window = new Pane();
        rackTiles = new RackTile[Game.NUM_TILES_IN_RACK];
        placedTiles = new Stack<RackTile>();

        cursor = new Cursor();
        boardGroup = new Group();
        boardTileGroup = new Group();
        rackTileGroup = new Group();
        window.getChildren().addAll(background, boardGroup, boardTileGroup, rackTileGroup, cursor);

        boardTiles = new BoardTile[Board.BOARD_WIDTH][Board.BOARD_WIDTH];

        gameBoard = new Space[Board.BOARD_WIDTH][Board.BOARD_WIDTH];

        for (int row = 0; row < Board.BOARD_WIDTH; row++) {
            for (int col = 0; col < Board.BOARD_WIDTH; col++) {
                String bonus = "VOI";
                switch (Board.getBonusValue(col, row)) {
                    case Board.NBS:
                        bonus = "NBS";
                        break;
                    case Board.DLS:
                        bonus = "DLS";
                        break;
                    case Board.DWS:
                        bonus = "DWS";
                        break;
                    case Board.TLS:
                        bonus = "TLS";
                        break;
                    case Board.TWS:
                        bonus = "TWS";
                        break;
                }

                if (bonus.equals("VOI")) continue;

                Space space = new Space(bonus, col, row);
                gameBoard[row][col] = space;

                boardGroup.getChildren().add(space);
            }
        }

        updateRack(currPlayerIndex);

        //Creating a scene object
        Scene scene = new Scene(window, WINDOW_WIDTH, WINDOW_HEIGHT);

        scene.setOnKeyPressed(e -> {
            if (e.getCode().isLetterKey()) {
                cursor.typeLetter(e.getCode().getChar().charAt(0));
            }
            else if (e.getCode() == KeyCode.BACK_SPACE) {
                cursor.backspace();
            }
            else if (e.getCode() == KeyCode.ENTER) {
                cursor.deactivate();
                placeTiles();
            }
        });

        //Setting title to the Stage
        stage.setTitle("Hexica (WIP)");

        //Adding scene to the stage
        stage.setScene(scene);

        //Displaying the contents of the stage
        stage.show();
    }

    private class Space extends ImageView {
        int col;
        int row;
        Space(String bonus, int col, int row) {
            super();
            this.col = col;
            this.row = row;
            Image image;
            try {
                image = new Image(new FileInputStream("images/"+bonus+".png"));
            }
            catch (FileNotFoundException e) {
                return;
            }
            setImage(image);
            setX(calculateX(col, row));
            setY(calculateY(col, row));
            setPreserveRatio(true);
            setFitHeight(SPACE_WIDTH);
            setOnMouseClicked(e -> {
                cursor.activate(this.col, this.row);
            });
        }
    }

    private class BoardTile extends ImageView {
        private Board.Tile tile;

        public BoardTile(char data, int col, int row) {
            super();
            tile = new Board.Tile(data, col, row);
            Image image;
            try {
                image = new Image(new FileInputStream("images/woodtile"+data+".png"));
            }
            catch (FileNotFoundException e) {
                return;
            }
            setImage(image);
            setPreserveRatio(true);
            setX(calculateX(col, row));
            setY(calculateY(col, row));
            setFitHeight(SPACE_WIDTH - SPACE_OVERLAP/2);
            setOnMouseClicked(e -> {
                cursor.activate(tile.getCol(), tile.getRow());
            });
        }
        public int getRow() {
            return tile.getRow();
        }

        public int getCol() {
            return tile.getCol();
        }

        public int getData() {
            return tile.getData();
        }

        public Board.Tile getTile() {
            return tile;
        }
    }

    private class RackTile extends ImageView {
        private Board.Tile tile;
        private int pos;

        public RackTile(char letter, int pos) {
            super();
            this.tile = new Board.Tile(letter, -1, -1);
            this.pos = pos;
            Image image;
            try {
                image = new Image(new FileInputStream("images/woodtile"+letter+".png"));
            }
            catch (FileNotFoundException e) {
                return;
            }
            setImage(image);
            setPreserveRatio(true);
            moveToRack();
        }

        public void moveOnBoard(int col, int row) {
            tile.setCol(col);
            tile.setRow(row);
            setFitHeight((int)(SPACE_WIDTH*UNPLACED_TILE_SIZE) - 1);
            setX(calculateX(col, row) - (SPACE_WIDTH*(UNPLACED_TILE_SIZE-1)*TILE_ASPECT_RATIO/2.0));
            setY(calculateY(col, row) - (SPACE_WIDTH*(UNPLACED_TILE_SIZE-1)/2.0));
            setOnMouseClicked(e -> {
                cursor.activate(col, row);
            });
        }

        public void moveToRack() {
            tile.setCol(-1);
            tile.setRow(-1);
            setOnMouseClicked(e -> {});
            setFitHeight(RACK_TILE_WIDTH);
            setX(RACK_X_OFFSET + (RACK_TILE_WIDTH + RACK_TILE_GAP) * pos);
            setY(RACK_Y_OFFSET);
        }
        public int getRow() {
            return tile.getRow();
        }

        public int getCol() {
            return tile.getCol();
        }

        public int getData() {
            return tile.getData();
        }

        public Board.Tile getTile() {
            return tile;
        }
    }

    private class Cursor extends ImageView {
        int col;
        int row;
        Board.Direction direction;
        Cursor() {
            super();
            col = -1;
            row = -1;
            direction = Board.Direction.DOWN;
            setVisible(false);

            Image image;
            try {
                image = new Image(new FileInputStream("images/cursor.png"));
            }
            catch (FileNotFoundException e) {
                return;
            }

            setImage(image);
            setPreserveRatio(true);
            setFitHeight(SPACE_WIDTH);
            setOnMouseClicked(e -> {
                activate(this.col, this.row);
            });
        }

        private void drawCursor() {
            if (this.col == -1 && this.row == -1) {
                setVisible(false);
            }
            else {
                int orientation = 0;
                switch (direction) {
                    case RISING:
                        orientation = -120;
                        break;
                    case FALLING:
                        orientation = -60;
                        break;
                    case DOWN:
                        orientation = 0;
                        break;
                }
                setRotate(orientation);
                setX(calculateX(this.col, this.row) + CURSOR_X_OFFSET);
                setY(calculateY(this.col, this.row) + CURSOR_Y_OFFSET);
                setVisible(true);
            }
        }

        private void activate(int col, int row) {
            placeTilesOnRack();
            if (this.col == col && this.row == row) {
                switch (direction) {
                    case RISING:
                        direction = Board.Direction.FALLING;
                        break;
                    case FALLING:
                        direction = Board.Direction.DOWN;
                        break;
                    case DOWN:
                        this.col = -1;
                        this.row = -1;
                }
            }
            else {
                if (!game.board.isEmpty(col, row)) {
                    deactivate();
                    return;
                }
                direction = Board.Direction.RISING;
                this.col = col;
                this.row = row;
            }

            drawCursor();
        }

        private void deactivate() {
            this.col = -1;
            this.row = -1;
            drawCursor();
        }


        private void typeLetter(char letter) {
            if (col == -1 && row == -1) {
                return;
            }

            int tileIndex = findOnRack(letter);
            if (tileIndex == -1) {
                return;
            }

            RackTile rackTile = rackTiles[tileIndex];
            placedTiles.push(rackTile);
            rackTile.moveOnBoard(col, row);

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
            if (!game.board.isValid(col, row)) {
                col = -1;
                row = -1;
            }
            else {
                while (!game.board.isEmpty(col, row)) {
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
                    if (!game.board.isValid(col, row)) {
                        col = -1;
                        row = -1;
                        break;
                    }
                }
            }
            drawCursor();
        }

        private void backspace() {
            RackTile rackTile;
            try {
                rackTile = placedTiles.pop();
            }
            catch (EmptyStackException e) {
                deactivate();
                return;
            }
            col = rackTile.getCol();
            row = rackTile.getRow();
            rackTile.moveToRack();
            drawCursor();
        }
    }

    public int findOnRack(char letter) {
        for (int i = 0; i < rackTiles.length; i++) {
            RackTile rackTile = rackTiles[i];
            if (rackTile.getCol() == -1 && rackTile.getRow() == -1) {
                if (rackTile.getData() == letter) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void placeTilesOnRack() {
        while (!placedTiles.isEmpty()) {
            RackTile rackTile = placedTiles.pop();
            rackTile.moveToRack();
        }
    }

    private void clearRackTiles() {
        for (int i = 0; i < rackTiles.length; i++) {
            RackTile rackTile = rackTiles[i];
            rackTiles[i] = null;
            rackTileGroup.getChildren().remove(rackTile);
        }
    }

    private void updateRack(int playerIndex) {
        clearRackTiles();
        char[] rack = game.getPlayerRack(playerIndex);
        for (int pos = 0; pos < Game.NUM_TILES_IN_RACK; pos++) {
            RackTile rackTile = new RackTile(rack[pos], pos);
            rackTiles[pos] = rackTile;
            rackTileGroup.getChildren().add(rackTile);
        }
    }

    private void placeTiles() {
        while (!placedTiles.isEmpty()) {
            RackTile rackTile = placedTiles.pop();
            game.placeTile(rackTile.getTile(), currPlayerIndex);
        }
        game.restockRack(currPlayerIndex);
        game.printRack(currPlayerIndex);
        updateRack(currPlayerIndex);
        renderBoard();
    }

    private void renderBoard() {
        for (int row = 0; row < Board.BOARD_WIDTH; row++) {
            for (int col = 0; col < Board.BOARD_WIDTH; col++) {
                BoardTile prevTile = boardTiles[row][col];
                if (prevTile != null) {
                    boardTileGroup.getChildren().remove(prevTile);
                }

                char tileChar = game.board.getCharAt(col, row);
                if (tileChar == Board.EMT || tileChar == Board.VOI) {
                    continue;
                }
                BoardTile newTile = new BoardTile(tileChar, col, row);
                boardTiles[row][col] = newTile;
                boardTileGroup.getChildren().add(newTile);
            }
        }
    }

    private static int calculateX(int col, int row) {
        return BOARD_X_OFFSET + (int)((SPACE_WIDTH-SPACE_OVERLAP)*col*(Math.sqrt(3)/2.0));
    }

    private static int calculateY(int col, int row) {
        return BOARD_Y_OFFSET + (int)((SPACE_WIDTH-SPACE_OVERLAP)*(row - col/2.0));
    }

    public static void main(String args[]) {
        launch(args);
    }
}
