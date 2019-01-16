import {NetworkEvent} from "../NetworkEvent";
import {PositionedBuffer} from "../../../engine/net/PositionedBuffer";
import {OPCode} from "../NetworkEventBuffer";

/**
 * This event is sent from both client and server.
 *
 * The event's OPCode is the only important data for this event.
 * Therefore we neither read or write and additional bytes.
 */
export class PingEvent extends NetworkEvent {

    private static readonly BINARY_SIZE: number = 0;

    /**
     * @override
     */
    public getEventSize(): number {
        return PingEvent.BINARY_SIZE;
    }

    /**
     * @override
     */
    public getOPCode(): OPCode {
        return OPCode.PING;
    }

    /**
     * @override
     */
    public write(buffer: PositionedBuffer): void {}

}
