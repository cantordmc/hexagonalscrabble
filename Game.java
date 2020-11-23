import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Game {
    public static final char BLK = Board.BLK; // Blank
    public static final char VOI = Board.VOI; // No hexagon exists
    public static final char EMT = Board.EMT; // No tile exists
    public static final int NUM_TILES_IN_RACK = 6;

    private static final int[] TILE_DISTRIBUTION =
        { 2,7,2,2,3,9,1,2,2,6,1,1,3,2,4,5,2,1,4,4,4,3,1,1,1,1,1 };
        //?,A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z
    private static final String DEFAULT_LEXICON = "lexicons/nwl18.txt";
    private static final int DEFAULT_NUM_PLAYERS = 1;

    public String[] dictionary;
    public Board board;
    public List<Board> history;
    public int numPlayers;
    public int[] playerScores;
    public char[][] playerRacks;
    public int[] tileBag;
    public int numTiles;
    private Random rng;

    public Game() {
        this(DEFAULT_LEXICON, DEFAULT_NUM_PLAYERS);
    }
    public Game(String filename, int numPlayers) {
        // Random number generator
        rng = new Random();
        // Scan in dictionary
        Scanner sc;
        try{
            sc = new Scanner(new File(filename));
        }
        catch (FileNotFoundException e) {
            System.out.println("ERROR: "+filename+" doesn't exist.");
            return;
        }
        ArrayList<String> words = new ArrayList<String>();
        while (sc.hasNext()) {
            words.add(sc.next());
        }
        dictionary = words.toArray(new String[10]);

        // Initialize game board and tile bag with tile count
        board = new Board();
        history = new ArrayList<Board>();
        tileBag = TILE_DISTRIBUTION;
        numTiles = 0;
        for (int i = 0; i < tileBag.length; i++) {
            numTiles += tileBag[i];
        }

        // Initialize player scores
        this.numPlayers = numPlayers;
        playerScores = new int[numPlayers];
        playerRacks = new char[numPlayers][NUM_TILES_IN_RACK];
        for (int i = 0; i < numPlayers; i++) {
            restockRack(i);
        }
    }

    public char drawTile() {
        // Decrement tile count unless it is already at 0
        //  in that case return empty
        if (numTiles-- == 0) {
            return EMT;
        }

        // Generate random tile number
        int destIndex = rng.nextInt(numTiles);
        int index = 0;
        int letter = -1;
        while (index <= destIndex) {
            index += tileBag[++letter];
        }
        // Remove tile from bag
        tileBag[letter]--;
        // Return corresponding char
        if (letter == 0) {
            return BLK;
        }
        else {
            return (char)('@' + letter);
        }
    }

    public boolean placeTile(Board.Tile tile, int playerIndex) {
        int tileIndex = findOnPlayerRack(tile.getData(), playerIndex);
        if (tileIndex == -1) {
            return false;
        }
        if (board.placeTile(tile)) {
            playerRacks[playerIndex][tileIndex] = EMT;
            return true;
        }
        else {
            return false;
        }
    }

    public char[] getPlayerRack(int playerIndex) {
        return playerRacks[playerIndex];
    }

    public int findOnPlayerRack(char letter, int playerIndex) {
        for (int i = 0; i < NUM_TILES_IN_RACK; i++) {
            char tile = playerRacks[playerIndex][i];
            if (tile == letter) {
                return i;
            }
        }
        return -1;
    }

    public void printRack(int playerIndex) {
        char[] rack = playerRacks[playerIndex];
        for (int i = 0; i < rack.length; i++) {
            System.out.print(rack[i]);
        }
        System.out.println("");
    }

    public boolean restockRack(int playerIndex) {
        char[] rack = playerRacks[playerIndex];
        for (int i = 0; i < rack.length; i++) {
            if (rack[i] == '\0' || rack[i] == EMT) {
                if (numTiles == 0) {
                    Arrays.sort(rack); // Sorts the tiles in place
                    return false; // There are no more tiles left to take from
                }
                else {
                    rack[i] = drawTile();
                }
            }
        }
        Arrays.sort(rack);
        return true;
    }

    public char[] tileBagToCharArray() {
        char[] tileArray = new char[numTiles];
        int index = 0;
        for (int k = 0; k < tileBag[0]; k++) {
            tileArray[index++] = BLK;
        }
        for (int letter = 1; letter < tileBag.length; letter++) {
            for (int k = 0; k < tileBag[letter]; k++) {
                tileArray[index++] = (char)('@' + letter);
            }
        }
        return tileArray;
    }

    public boolean checkWord(String word) {
        word = word.toUpperCase();
        return checkWordBinarySearch(word, 0, dictionary.length - 1);
    }

    private boolean checkWordBinarySearch(String word, int startIndex, int endIndex) {
        if (endIndex < startIndex) {
            return false;
        }
        if (startIndex == endIndex) {
            return word.equals(dictionary[startIndex]);
        }
        int split = startIndex + (endIndex - startIndex)/2;
        String pivot = dictionary[split];
        // Check word length first
        if (word.length() < pivot.length()) {
            return checkWordBinarySearch(word, startIndex, split - 1);
        }
        else if (word.length() > pivot.length()) {
            return checkWordBinarySearch(word, split + 1, endIndex);
        }
        // If word length equal, compare lexicographically
        else {
            if (word.compareTo(pivot) < 0) {
                return checkWordBinarySearch(word, startIndex, split - 1);
            }
            else if (word.compareTo(pivot) > 0) {
                return checkWordBinarySearch(word, split + 1, endIndex);
            }
            else {
                return true;
            }
        }
    }
}
