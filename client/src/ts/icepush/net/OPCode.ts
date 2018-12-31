/**
 *
 */
export enum OPCode {
    FAILURE = 1,
    SUCCESS = 2,
    NEW_PLAYER = 5,
    PLAYER_MOVE = 6,
    MOVE_REQUEST = 8,
    END_MOVE = 9,
    LOGOUT = 10,
    CHAT_SEND = 16,
    PLAYER_LOGGED_OUT = 11,
    PLAYER_DEATH = 12,
    PROJECTILE_REQUEST = 15,
    CHAT_RECEIVE = 17,
    UPDATE_TIME = 18
}
