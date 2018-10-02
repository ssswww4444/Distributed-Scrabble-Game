public class Constants {

    // mqtt topic names
    public static final String MQTT_TOPIC = "mqtt";
    public static final String SERVER_TOPIC = "server";
    public static final String CLIENT_TOPIC = "client";
    public static final String ROOM_TOPIC = "room";
    public static final String TEST_TOPIC = "mqtt/room1";

    // max number of players
    public static final int ROOM_MAX_PLAYER = 4;

    // action types
    public static final String GAME_START = "GameStart";
    public static final String VOTE = "Vote";
    public static final String GAME_OVER = "GameOver";
    public static final String LOGIN = "Login";
    public static final String INVITATION = "Invitation";
    public static final String JOIN_ROOM = "JoinRoom";

    // player status
    public static final String STATUS_AVAILABLE = "available";
    public static final String STATUS_ROOM = "room";
}
