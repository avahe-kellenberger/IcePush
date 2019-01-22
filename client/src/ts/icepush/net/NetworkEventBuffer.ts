import {PositionedBuffer} from "../../engine/net/PositionedBuffer";
import {NetworkEvent} from "./NetworkEvent";
import {PingEvent} from "./events/PingEvent";
import {NewPlayerEvent} from "./events/NewPlayerEvent";
import {PlayerMovedEvent} from "./events/PlayerMovedEvent";
import {ChatReceiveEvent} from "./events/ChatEvent";
import {TimeRemainingEvent} from "./events/TimeRemainingEvent";
import {FailureEvent} from "./events/FailureEvent";
import {SuccessEvent} from "./events/SuccessEvent";
import {PlayerLoggedOutEvent} from "./events/PlayerLoggedOutEvent";
import {PlayerLivesChangedEvent} from "./events/PlayerLivedChangedEvent";
import {RoundWinnersEvent} from "./events/RoundWinnersEvent";

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
    UPDATE_TIME = 18,
    ROUND_WINNERS = 20
}

// Maps the OPCodes to their respective NetworkEvents.
function mapOPCodeEvents(): Map<number, new (...args: any[]) => NetworkEvent> {
    // @ts-ignore
    return new Map([
        [OPCode.PING, PingEvent],
        [OPCode.FAILURE, FailureEvent],
        [OPCode.SUCCESS, SuccessEvent],
        [OPCode.NEW_PLAYER, NewPlayerEvent],
        [OPCode.PLAYER_MOVE, PlayerMovedEvent],
        [OPCode.PLAYER_LOGGED_OUT, PlayerLoggedOutEvent],
        [OPCode.PLAYER_LIVES_CHANGED, PlayerLivesChangedEvent],
        [OPCode.CHAT_RECEIVE, ChatReceiveEvent],
        [OPCode.UPDATE_TIME, TimeRemainingEvent],
        [OPCode.ROUND_WINNERS, RoundWinnersEvent]
    ]);
}

/**
 * A `NetworkEvent` oriented implementation of `PositionedBuffer`.
 */
export class NetworkEventBuffer extends PositionedBuffer {

    /**
     *
     */
    private static opcodeEventMap: Map<number, new (...args: any[]) => NetworkEvent>;

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
        // Lazy OPCode mapping (written because of a TypeScript compiler issue; cannot be done statically).
        if (NetworkEventBuffer.opcodeEventMap === undefined) {
            NetworkEventBuffer.opcodeEventMap = mapOPCodeEvents();
        }
        const event: (new (...args: any[]) => NetworkEvent)|undefined = NetworkEventBuffer.opcodeEventMap.get(opcode);
            if (event === undefined) {
            throw new Error(`Unsupported event type!\nOPCode: ${opcode}`);
        }
        return new event(this);
    }

}
