import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientInterface extends Remote {
    public void notifyGameStart() throws RemoteException;
    public void notifyTurn(int turn) throws RemoteException;  // when turn switched
    public void notifyStartVote() throws RemoteException;  // some player has started vote
    public void notifyVote(String username, boolean agree) throws RemoteException;  // some player has started vote

}
