import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientInterface extends Remote {
    /** Need to update Client GUI for all of the following methods **/

    public void notifyGameStart() throws RemoteException;  // start of game
    public void notifyTurn(int turn) throws RemoteException;  // when turn switched
    public void notifyStartVote(int startI, int startJ, int length, boolean horizontal) throws RemoteException;  // some player has started vote
    public void notifyVote(String username, boolean agree) throws RemoteException;  // some player has started vote
    public void notifyVoteResult(boolean success) throws RemoteException;  // notify if the voting is succeed
    public void notifyInsertLetter(int i, int j, Character letter) throws RemoteException;  // notify current user has inserted the letter
    public void notifyScoreChange(String currUsername, Integer newScore) throws RemoteException; // notify score change of current player
    public void notifyLeaveGame(String username) throws RemoteException; // notify all clients who left the game
    public void notifyEndGame() throws RemoteException;  // notify game has ended
    public void notifyTurnPassed(String username) throws RemoteException;  // notify currPlayer passed the turn (only show message that who passed the turn)
}
