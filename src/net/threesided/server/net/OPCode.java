package net.threesided.server.net;

/**
 * Network events OPCodes.
 */
public enum OPCode {
    PING(-37),
    LOGIN(0),
    FAILURE(1),
    SUCCESS(2),
    NEW_PLAYER(5),
    PLAYER_MOVE(6),
    MOVE_REQUEST(8),
    END_MOVE(9),
    LOGOUT(10),
    PLAYER_LOGGED_OUT(11),
    PLAYER_LIVES_CHANGED(12),
    CHAT_SEND(16),
    CHAT_RECEIVE(17),
    ROUND_WINNERS(20),
    ROUND_START(21),
    ROUND_START_COUNTDOWN(22);

    private final int value;

    /**
     * Creates a new OPCode which is directly associated with a numeric value.
     * @param value The value associated with the OPCode.
     */
    OPCode(final int value) {
        this.value = value;
    }

    /**
     * @return The value associated with the OPCode.
     */
    public int getValue() {
        return this.value;
    }

    /**
     * Finds the OPCode associated with the value.
     * @param value The value of the OPCode.
     * @return The OPCode associated with the given value.
     */
    public static OPCode getByValue(final int value) {
        for (final OPCode opCode : OPCode.values()) {
            if (opCode.value == value) {
                return opCode;
            }
        }
        throw new IllegalArgumentException("Value " + value + " is not associated with an OPCode.");
    }

}
