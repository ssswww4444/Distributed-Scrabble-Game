import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientInterface extends Remote {
    public void notifyGameStart() throws RemoteException;
    public void notifyTurn(int turn) throws RemoteException;  // when turn switched
}
