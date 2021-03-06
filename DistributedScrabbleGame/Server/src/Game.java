import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;

public class Game {

    private int turn;
    private ArrayList<ArrayList<Cell>> board;
    private static final int boardHeight = 20;
    private static final int boardWidth = 20;
    private ArrayList<Player> players;

    public HashMap<String, Integer> getScores() {
        return scores;
    }

    private HashMap<String, Integer> scores;
    private int voteAgreeNum;
    private int voteTotalNum;

    private int currStartRow;
    private int currStartCol;
    private int currWordLength;  // current voting word
    private boolean currHorizontal;
    private int currInsertedRow;
    private int currInsertedCol;
    private String currInsertedLetter;
    private int letterNum;

    private enum GameStatus {
        INSERTING, VOTING
    }

    private GameStatus currStatus;
    private boolean hasInserted;  // insertion made in this turn
    private int passCount;

    /**
     * Constructor
     */
    public Game(ArrayList<Player> players, int roomID) {
        this.players = players;

        //initGame();
        //registerGame(roomID);   // create & bind game servant
        //notifyGameStart();

        // init scores
        scores = new HashMap<>();
        for (Player player : players) {
            scores.put(player.getUsername(), 0);
        }

        // init pass count
        passCount = 0;

        // init turn 1
        turn = 1;
        hasInserted = false;
        letterNum = 0;

    }

    /**
     * Bind the game to registry for clients lookup
     */
    private void registerGame(int roomID) {
        try {
            Registry registry = LocateRegistry.getRegistry();  // get registry of the server
            //ServerServant gameServant = new ServerServant(this);   // create servant instance
            //registry.rebind(Integer.toString(roomID), gameServant);   // bind to register
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialise a game
     */
    private void initGame() {
        // init board
        for (int i = 0; i < boardHeight; i++) {
            ArrayList<Cell> row = new ArrayList<>();
            for (int j = 0; j < boardWidth; j++) {
                Cell cell = new Cell();
                row.add(cell);
            }
            board.add(row);
        }

        // init scores
        for (Player player : players) {
            scores.put(player.getUsername(), 0);
        }

        // init pass count
        passCount = 0;

        // init turn 1
        turn = 1;
        hasInserted = false;

    }

    /**
     * Notify all players that game has started
     */
    private void notifyGameStart() {
        for (Player player : players) {
            //ClientInterface clientServant = player.getClientServant();
            Player currPlayer = players.get(turn - 1);  // will be p1

            // Obsolete if using MQTT
            /*try {
                clientServant.notifyGameStart();
                clientServant.notifyTurn(currPlayer.getUsername());  // starts with turn = 1 (p1)
            } catch (RemoteException e) {
                e.printStackTrace();
            }*/
        }
        currStatus = GameStatus.INSERTING; // p1 inserting letter
    }

    /**
     * Current player passed this turn
     */
    public boolean passTurn() {
        passCount++;
        if(passCount == players.size()){
            endGame();
            return true;
        }else{
            nextTurn();
            return false;
        }

        // notify

        //Obsolete if using MQTT
        /*for (Player player: players) {
            ClientInterface clientServant = player.getClientServant();
            Player currPlayer = players.get(turn - 1);
            try {
                clientServant.notifyTurnPassed(currPlayer.getUsername());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }*/

        // switch turn
    }

    /**
     * Switch turn and notify all players
     */
    public void nextTurn() {

        if(turn == players.size()){     // The last player has finished his turn
            turn = 1;       // Go back to player No.1
        }else{
            this.turn++;
        }
        if(hasInserted){
            this.passCount = 0;
        }

        currStatus = GameStatus.INSERTING;
        hasInserted = false;  // reset boolean

        // Obsolete if using MQTT
        /*for (Player player: players) {
            ClientInterface clientServant = player.getClientServant();
            Player currPlayer = players.get(turn - 1);
            try {
                clientServant.notifyTurn(currPlayer.getUsername());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }*/
    }

    /**
     * Place a letter to the board at (i,j)
     *
     * @return success or fail
     */
    public void placeLetter(int i, int j, String letter) {
        hasInserted = true;

        // get cell
        //Cell targetCell = board.get(i).get(j);

        currInsertedRow = i;
        currInsertedCol = j;
        currInsertedLetter = letter;
        this.letterNum++;

        //targetCell.setLetter(letter);

        // Obsolete if using MQTT
        /*for (Player player: players) {
            ClientInterface clientServant = player.getClientServant();
            try {
                clientServant.notifyInsertLetter(i, j, letter);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }*/
    }

    /**
     * Initialise a vote and notify all clients
     */
    public void startVote(int startI, int startJ, int length, boolean horizontal) {

        // init vote
        currStatus = GameStatus.VOTING;
        voteAgreeNum = 0;
        voteTotalNum = 0;

        currWordLength = length;
        currStartRow = startI;
        currStartCol = startJ;
        currHorizontal = horizontal;

        // Obsolete if using MQTT
        /*for (Player player: players) {
            ClientInterface clientServant = player.getClientServant();
            currWordLength = length;
            try {
                clientServant.notifyStartVote(startI, startJ, length, horizontal);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }*/
    }

    /**
     * Get voting response from a player, and notify all, if all voted, notify result: success,fail
     */
    public int vote(String username, boolean agree) {

        // update vote response
        if (agree) {
            voteAgreeNum++;
        }
        voteTotalNum++;

        System.out.println("vote total number: " + voteTotalNum);

        // check if all voted
        if (voteTotalNum == players.size()-1) {
            if (voteAgreeNum == voteTotalNum) {  // all agree
                voteSuccess();

                System.out.println("vote success: " + scores.get(players.get(turn - 1).getUsername()));
                return 1;
            } else {
                voteFail();

                return 0;
            }
        }

        return 2;

        // Obsolete if using MQTT
        /*for (Player player: players) {
            ClientInterface clientServant = player.getClientServant();
            try {
                clientServant.notifyVote(username, agree);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }*/

    }

    /**
     * Notify all clients the voting results
     */
    private void notifyVoteResult(boolean success) {
        //Obsolete if using MQTT
        /*for (Player player: players) {
            ClientInterface clientServant = player.getClientServant();
            try {
                clientServant.notifyVoteResult(success);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }*/
    }


    public String getCurrPlayer() {
        Player player = players.get(turn-1);
        return player.getUsername();
    }


    /**
     * If success, gives score to current player
     */
    private void voteSuccess() {
        // notify voting result
        notifyVoteResult(true);

        // increment score
        Player currPlayer = players.get(turn - 1);

        System.out.println("Word length: " + currWordLength);
        Integer newScore = scores.get(currPlayer.getUsername()) + currWordLength;
        scores.put(currPlayer.getUsername(), newScore);

        // notify score change, Obsolete if using MQTT
        /*for (Player player: players) {
            ClientInterface clientServant = player.getClientServant();
            try {
                clientServant.notifyScoreChange(currPlayer.getUsername(), newScore);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }*/

        nextTurn();

    }

    /**
     * If failed, notify and switch turn
     */
    private void voteFail() {
        // notify
        notifyVoteResult(false);

        nextTurn();
    }

    /**
     * Notify all clients who has left the game, and end game
     */
    public void leaveGame(String username) {

        // notify who has left, Obsolete if using MQTT
        /*for (Player player: players) {
            ClientInterface clientServant = player.getClientServant();
            try {
                clientServant.notifyLeaveGame(username);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }*/

        // end game
        endGame();
    }

    /**
     * End the game
     */
    private void endGame() {

        // notify game has ended, Obsolete if using MQTT
        /*for (Player player: players) {
            ClientInterface clientServant = player.getClientServant();
            try {
                clientServant.notifyEndGame();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }*/
    }


    public ArrayList<String> getPlayers() {
        ArrayList<String> pList = new ArrayList<String>();
        for (int i = 0; i < players.size(); i++) {
            pList.add(players.get(i).getUsername());

        }
        return pList;
    }

    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
    }


    public int getVoteAgreeNum() {
        return voteAgreeNum;
    }

    public void setVoteAgreeNum(int voteAgreeNum) {
        this.voteAgreeNum = voteAgreeNum;
    }

    public int getVoteTotalNum() {
        return voteTotalNum;
    }

    public void setVoteTotalNum(int voteTotalNum) {
        this.voteTotalNum = voteTotalNum;
    }

    public int getCurrStartRow(){
        return this.currStartRow;
    }

    public int getCurrStartCol(){
        return this.currStartCol;
    }

    public int getCurrWordLength(){
        return this.currWordLength;
    }

    public String getCurrInsertedLetter(){
        return this.currInsertedLetter;
    }

    public boolean getCurrHorizontal() {
        return this.currHorizontal;
    }

    public int getCurrInsertedRow(){
        return this.currInsertedRow;
    }
    public int getCurrInsertedCol(){
        return this.currInsertedCol;
    }
}
