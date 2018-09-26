import java.rmi.RemoteException;

public class ServerMethods implements ServerInterface {
    @Override
    public void printMsg() throws RemoteException {
        System.out.println("Hello world. ");
    }
}
