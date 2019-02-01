import {NetworkEvent} from "../NetworkEvent";
import {PositionedBuffer} from "../../../engine/net/PositionedBuffer";
import {OPCode} from "../NetworkEventBuffer";

/**
 * Sent from server to client, informing of the amount of time remaining in the game.
 */
export class RoundStartEvent extends NetworkEvent {

    private static readonly BINARY_SIZE: number = 2;

    public readonly time: number;

    /**
     * Creates a buffer with the given time remaining.
     * @param time The amount of time left.
     */
    constructor(time: number);

    /**
     * Reads the remaining time from the buffer.
     * @param buffer The buffer to read from.
     */
    constructor(buffer: PositionedBuffer);

    /**
     * Overload constructor.
     */
    constructor(timeOrBuffer: PositionedBuffer|number, time?: number) {
        super();
        if (timeOrBuffer instanceof PositionedBuffer) {
            this.time = timeOrBuffer.readUInt16BE();
        } else if (time !== undefined) {
            this.time = time;
        } else {
            throw new Error(`Malformed constructor:\n${timeOrBuffer}`);
        }
    }

    /**
     * @override
     */
    public getEventSize(): number {
        return RoundStartEvent.BINARY_SIZE;
    }

    /**
     * @override
     */
    public getOPCode(): OPCode {
        return OPCode.ROUND_START;
    }

    /**
     * @override
     */
    public write(buffer: PositionedBuffer): void {
        buffer.writeUInt16BE(this.time);
    }

}