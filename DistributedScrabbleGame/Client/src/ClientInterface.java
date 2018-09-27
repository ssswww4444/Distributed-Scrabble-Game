import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientInterface extends Remote {
    /** Need to update Client GUI for all of the following methods **/

    void notifyGameStart() throws RemoteException;  // start of game
    void notifyTurn(String username) throws RemoteException;  // when turn switched
    void notifyStartVote(int startI, int startJ, int length, boolean horizontal) throws RemoteException;  // some player has started vote
    void notifyVote(String username, boolean agree) throws RemoteException;  // some player has started vote
    void notifyVoteResult(boolean success) throws RemoteException;  // notify if the voting is succeed
    void notifyInsertLetter(int i, int j, Character letter) throws RemoteException;  // notify current user has inserted the letter
    void notifyScoreChange(String currUsername, Integer newScore) throws RemoteException; // notify score change of current player
    void notifyLeaveGame(String username) throws RemoteException; // notify all clients who left the game
    void notifyEndGame() throws RemoteException;  // notify game has ended
    void notifyTurnPassed(String username) throws RemoteException;  // notify currPlayer passed the turn (only show message that who passed the turn)
}
