import {NetworkEvent} from "../NetworkEvent";
import {PositionedBuffer} from "../../../engine/net/PositionedBuffer";
import {OPCode} from "../NetworkEventBuffer";

/**
 * Sent from the server to the client upon login, if the login failed (e.g. username in use).
 */
export class FailureEvent extends NetworkEvent {

    private readonly BINARY_SIZE: number;

    public readonly message: string;

    /**
     * Constructs an event containing a message explaining the failure.
     * @param message The failure message.
     */
    constructor(message: string);

    /**
     * Creates a FailureEvent by reading the message from the buffer.
     * @param buffer The buffer containing the failure message.
     */
    constructor(buffer: PositionedBuffer);

    /**
     * Overload constructor.
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