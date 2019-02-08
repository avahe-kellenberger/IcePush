package net.threesided.server.net.event;

/**
 * Network events OPCodes.
 */
public enum OPCode {
    PING((byte) -37),
    LOGIN((byte) 0),
    FAILURE((byte) 1),
    SUCCESS((byte) 2),
    NEW_PLAYER((byte) 5),
    PLAYER_MOVE((byte) 6),
    MOVE_REQUEST((byte) 8),
    END_MOVE((byte) 9),
    LOGOUT((byte) 10),
    PLAYER_LOGGED_OUT((byte) 11),
    PLAYER_LIVES_CHANGED((byte) 12),
    CHAT_SEND((byte) 16),
    CHAT_RECEIVE((byte) 17),
    ROUND_WINNERS((byte) 20),
    ROUND_START((byte) 21),
    ROUND_START_COUNTDOWN((byte) 22);

    private final byte value;

    /**
     * Creates a new OPCode which is directly associated with a numeric value.
     * @param value The value associated with the OPCode.
     */
    OPCode(final byte value) {
        this.value = value;
        System.out.println(value);
    }

    /**
     * @return The value associated with the OPCode.
     */
    public byte getValue() {
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
        return null;
        //throw new IllegalArgumentException("Value " + value + " is not associated with an OPCode.");
    }

}
