public class Constants {

    public static final String EMPTY_BUTTON_TEXT = "<Click to invite>";

    // mqtt topic names
    public static final String MQTT_TOPIC = "mqtt";
    public static final String SERVER_TOPIC = "server";
    public static final String CLIENT_TOPIC = "client";
    public static final String ROOM_TOPIC = "room";

    // max and min number of players
    public static final int ROOM_MAX_PLAYER = 4;
    public static final int GAME_MIN_PLAYER = 2;

    // server actions
    public static final String PLAYER_LIST_UPDATE = "playerListUpdate";  // notify to update player list when logout/login/leaveRoom/joinRoom

    // room actions
    public static final String GAME_START = "GameStart";
    public static final String JOIN_ROOM = "JoinRoom";
    public static final String DISMISS_ROOM = "DismissRoom";
    public static final String LEAVE_ROOM = "LeaveRoom";
    public static final String READY = "Ready";

    // game actions
    public static final String SYNCHRONIZE_GAME = "SynchronizeGame";
    public static final String NO_WORD = "NoWord";
    public static final String PLACE_LETTER = "PlaceLetter";
    public static final String PASS = "Pass";
    public static final String END_GAME = "EndGame";
    public static final String VOTE = "Vote";
    public static final String VOTE_RESULT = "VoteResult";
    public static final String GAME_OVER = "GameOver";

    // client actions
    public static final String INVITATION = "Invitation";

    // player status
    public static final String STATUS_AVAILABLE = "available";
    public static final String STATUS_ROOM = "room";
    public static final int NOT_IN_ROOM_ID = -1;
}
