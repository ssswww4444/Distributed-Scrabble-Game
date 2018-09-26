import java.util.ArrayList;
import java.util.HashMap;

public class Game {

    private int turn;
    private ArrayList<ArrayList<Cell>> board;
    private static final int boardHeight = 20;
    private static final int boardWidth = 20;
    private ArrayList<Player> players;
    private HashMap<Player, Integer> scores;

    /**
     * Constructor
     */
    public Game(ArrayList<Player> players) {
        this.players = players;
        initGame();
    }

    /**
     * Initialise a game
     */
    private void initGame() {
        // init board
        for (int i=0; i < boardHeight; i++) {
            ArrayList<Cell> row = new ArrayList<>();
            for (int j=0; j < boardWidth; j++) {
                Cell cell = new Cell();
                row.add(cell);
            }
            board.add(row);
        }

        // init scores
        for (Player player: players) {
            scores.put(player, 0);
        }
    }

    /**
     * Switching turn
     */
    private void nextTurn() {
        turn += 1;
        if (turn > players.size()) {
            turn = 1;
        }
    }

}
