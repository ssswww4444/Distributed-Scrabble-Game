import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GameInterface extends Remote {
    public void startVote(int startI, int startJ, int length, boolean horizontal) throws RemoteException;
    public void passTurn() throws RemoteException;
    public void insertLetter(int x, int y, char letter) throws RemoteException;  // ps: should check if cell empty at client GUI
    public void vote(String username, boolean agree) throws RemoteException;
    public void leaveGame(String username) throws RemoteException;
}
