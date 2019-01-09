import {NetworkEvent} from "../NetworkEvent";
import {OPCode} from "../OPCode";
import {PositionedBuffer} from "../PositionedBuffer";

/**
 * Nothing is written to the buffer for LogoutEvents, aside from its OPCode.
 */
export class LogoutEvent extends NetworkEvent {

    private readonly BINARY_SIZE: number = 0;

    public readonly playerID: number;

    /**
     * @param buffer The buffer, if reading from the server.
     * If writing the event to the server, the buffer should NOT be given.
     */
    constructor(buffer?: PositionedBuffer) {
        super();
        if (buffer instanceof PositionedBuffer) {
            this.playerID = buffer.readInt16BE();
        } else {
            // The playerID does not need to be sent to the server when logging out.
            this.playerID = -1;
        }
    }

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
