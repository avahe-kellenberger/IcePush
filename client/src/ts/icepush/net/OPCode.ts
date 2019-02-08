/**
 * Network events OPCodes.
 * When a new OPCode is added which is to be received by the client, add a case to `readEvent`.
 */
export enum OPCode {
    PING = -37,
    LOGIN = 0,
    FAILURE = 1,
    SUCCESS = 2,
    NEW_PLAYER = 5,
    PLAYER_MOVE = 6,
    MOVE_REQUEST = 8,
    END_MOVE = 9,
    LOGOUT = 10,
    PLAYER_LOGGED_OUT = 11,
    PLAYER_LIVES_CHANGED = 12,
    CHAT_SEND = 16,
    CHAT_RECEIVE = 17,
    ROUND_WINNERS = 20,
    ROUND_START = 21,
    ROUND_START_COUNTDOWN = 22
}