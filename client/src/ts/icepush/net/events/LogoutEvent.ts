import {NetworkEvent} from "../NetworkEvent";
import {PositionedBuffer} from "../../../engine/net/PositionedBuffer";
import {OPCode} from "../NetworkEventBuffer";

/**
 * LogoutEvent is sent by the client to the server, informing the server that the client wants to log out.
 *
 * The event's OPCode is the only important data for this event.
 * Therefore we neither read or write and additional bytes.
 */
export class LogoutEvent extends NetworkEvent {

    private readonly BINARY_SIZE: number = 0;

    /**
     * @override
     */
    public getEventSize(): number {
        return this.BINARY_SIZE;
    }

    /**
     * @override
     */
    public getOPCode(): OPCode {
        return OPCode.LOGOUT;
    }

    /**
     * @override
     */
    public write(buffer: PositionedBuffer): void {}

}
