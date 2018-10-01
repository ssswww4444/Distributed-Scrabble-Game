import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class GameServant extends UnicastRemoteObject implements GameInterface {

    private Game game;


    /**
     * Constructor
     */
    public GameServant(Game game) throws RemoteException {
        this.game = game;
    }


    /**
     * Start voting
     */
    public void startVote(int startI, int startJ, int length, boolean horizontal) {  // horizontal = false --> vertical
        game.startVote(startI, startJ, length, horizontal);
    }


    /**
     * Pass the turn either before inserting letter or before voting
     */
    public void passTurn() {
        game.passTurn();
    }


    /**
     * Insert a letter to the board at coordinate (i,j): row i, col j
     */
    public void insertLetter(int i, int j, char letter) {
        game.insertLetter(i, j, letter);
    }


    /**
     * Vote for the word highlighted
     */
    public void vote(String username, boolean agree) {
        game.vote(username, agree);

        System.out.println(username + " " + agree);
    }


    /**
     * A player with this username has left the game
     */
    public void leaveGame(String username) {
        game.leaveGame(username);
    }


    @Override
    public ArrayList<String> addPlayer(String username) {
        game.getPlayers().add(username);
        return game.getPlayers();
    }
}
