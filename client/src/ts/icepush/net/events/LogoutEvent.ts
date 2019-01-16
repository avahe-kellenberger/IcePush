import {NetworkEvent} from "../NetworkEvent";
import {PositionedBuffer} from "../../../engine/net/PositionedBuffer";
import {OPCode} from "../NetworkEventBuffer";

/**
 * Nothing is written to the buffer for LogoutEvents, aside from its OPCode.
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
