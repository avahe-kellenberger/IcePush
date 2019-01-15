import {NetworkEvent} from "../NetworkEvent";
import {PositionedBuffer} from "../../../engine/net/PositionedBuffer";
import {OPCode} from "../NetworkEventBuffer";

/**
 * OPCode.FAILURE is the only important data for this event.
 * Therefore we neither read or write and additional bytes.
 */
export class FailureEvent extends NetworkEvent {

    private readonly BINARY_SIZE: number;

    public readonly message: string;

    /**
     * @param messageOrBuffer
     */
    constructor(messageOrBuffer: string|PositionedBuffer) {
       super();
       if (typeof messageOrBuffer === 'string') {
           this.message = messageOrBuffer;
       } else {
           this.message = messageOrBuffer.readString();
       }
       this.BINARY_SIZE = PositionedBuffer.getStringWriteSize(this.message);
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
        return OPCode.FAILURE;
    }

    /**
     * @override
     */
    public write(buffer: PositionedBuffer): void {
        buffer.writeString(this.message);
    }

}