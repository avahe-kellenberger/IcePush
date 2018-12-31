import {NetworkEvent} from "../NetworkEvent";
import {OPCode} from "../OPCode";
import {PositionedBuffer} from "../PositionedBuffer";

/**
 * OPCode.FAILURE is the only important data for this event.
 * Therefore we neither read or write and additional bytes.
 */
export class FailureEvent extends NetworkEvent {

    private BINARY_SIZE: number = 0;

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
        return OPCode.FAILURE;
    }

    /**
     * @override
     */
    public write(buffer: PositionedBuffer): void { }

}