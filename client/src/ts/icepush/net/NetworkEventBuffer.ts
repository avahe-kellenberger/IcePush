import {PositionedBuffer} from "../../engine/net/PositionedBuffer";
import {NetworkEvent} from "./NetworkEvent";
import {PingEvent} from "./events/PingEvent";
import {NewPlayerEvent} from "./events/NewPlayerEvent";
import {PlayerMoveEvent} from "./events/PlayerMoveEvent";
import {PlayerDeathEvent} from "./events/PlayerDeathEvent";
import {LogoutEvent} from "./events/LogoutEvent";
import {ChatReceiveEvent} from "./events/ChatEvent";
import {TimeRemainingEvent} from "./events/TimeRemainingEvent";
import {FailureEvent} from "./events/FailureEvent";
import {SuccessEvent} from "./events/SuccessEvent";

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
    PLAYER_DEATH = 12,
    PROJECTILE_REQUEST = 15,
    CHAT_SEND = 16,
    CHAT_RECEIVE = 17,
    UPDATE_TIME = 18
}

/**
 * A `NetworkEvent` oriented implementation of `PositionedBuffer`.
 */
export class NetworkEventBuffer extends PositionedBuffer {

    private events: NetworkEvent[]|undefined;

    /**
     * @return The `NetworkEvents` parsed from the buffer.
     */
    public getEvents(): NetworkEvent[] {
        if (this.events === undefined) {
            this.events = this.readEvents();
        }
        return this.events;
    }

    /**
     * Reads all NetworkEvents from the buffer.
     */
    private readEvents(): NetworkEvent[] {
        const events: NetworkEvent[] = [];
        while (this.isBufferValid()) {
            const opcode: number = this.readInt8();
            events.push(this.readEvent(opcode));
        }
        return events;
    }

    /**
     * Checks if the buffer is in a valid read state.
     */
    private isBufferValid(): boolean {
        const bufferLength: number = this.getLength();
        if (bufferLength < 3 || this.getPosition() >= bufferLength - 1) {
            return false;
        }
        const packetSize: number = this.readInt16BE();
        if (packetSize < 0) {
            return false;
        }
        const packetEnd: number = this.getPosition() + packetSize - 2;
        return packetEnd <= this.getLength();
    }

    /**
     * Handles events received from the server.
     * @param opcode The event's OPCode.
     * @return The size of the event in bytes.
     */
    private readEvent(opcode: OPCode): NetworkEvent {
        switch (opcode) {
            case OPCode.PING:
                return new PingEvent();
            case OPCode.FAILURE:
                return new FailureEvent();
            case OPCode.SUCCESS:
                return new SuccessEvent();
            case OPCode.NEW_PLAYER:
                return new NewPlayerEvent(this);
            case OPCode.PLAYER_MOVE:
                return new PlayerMoveEvent(this);
            case OPCode.PLAYER_LOGGED_OUT:
                return new LogoutEvent(this);
            case OPCode.PLAYER_DEATH:
                return new PlayerDeathEvent(this);
            case OPCode.CHAT_RECEIVE:
                return new ChatReceiveEvent(this);
            case OPCode.UPDATE_TIME:
                return new TimeRemainingEvent(this);

            default:
                throw new Error(`Unsupported event type!\nOPCode: ${opcode}`);
        }
    }

}