public class Player {

    private String username;
    private ClientInterface clientServant;

    // and remote object (servant) of the client ....

    /**
     * Constructor
     * @param username
     */
    public Player(String username, ClientInterface clientServant) {
        this.username = username;
        this.clientServant = clientServant;
    }

    /**
     * Accessors
     */
    public String getUsername() { return username; }
    public ClientInterface getClientServant() {
        return clientServant;
    }
}
