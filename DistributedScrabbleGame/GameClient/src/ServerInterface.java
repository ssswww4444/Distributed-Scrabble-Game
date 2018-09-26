import java.rmi.Remote;
import java.rmi.RemoteException;


public interface ServerInterface extends Remote {
    void printMsg() throws RemoteException;
}
