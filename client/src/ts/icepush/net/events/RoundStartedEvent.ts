import {NetworkEvent} from "../NetworkEvent";
import {PositionedBuffer} from "../../../engine/net/PositionedBuffer";
import {OPCode} from "../NetworkEventBuffer";

/**
 * Sent from server to client, informing of the amount of timeMilliseconds remaining in the game.
 */
export class RoundStartedEvent extends NetworkEvent {

    private static readonly BINARY_SIZE: number = 2;

    public readonly timeMilliseconds: number;

    /**
     * Creates a buffer with the given time in milliseconds remaining.
     * @param timeMilliseconds The amount of time in milliseconds left.
     */
    constructor(timeMilliseconds: number);

    /**
     * Reads the remaining time in milliseconds from the buffer.
     * @param buffer The buffer to read from.
     */
    constructor(buffer: PositionedBuffer);

    /**
     * Overload constructor.
     */
    constructor(timeOrBuffer: PositionedBuffer|number, timeMilliseconds?: number) {
        super();
        if (timeOrBuffer instanceof PositionedBuffer) {
            this.timeMilliseconds = timeOrBuffer.readUInt16BE();
        } else if (timeMilliseconds !== undefined) {
            this.timeMilliseconds = timeMilliseconds;
        } else {
            throw new Error(`Malformed constructor:\n${timeOrBuffer}`);
        }
    }

    /**
     * @override
     */
    public getEventSize(): number {
        return RoundStartedEvent.BINARY_SIZE;
    }

    /**
     * @override
     */
    public getOPCode(): OPCode {
        return OPCode.ROUND_STARTED;
    }

    /**
     * @override
     */
    public write(buffer: PositionedBuffer): void {
        buffer.writeUInt16BE(this.timeMilliseconds);
    }

}