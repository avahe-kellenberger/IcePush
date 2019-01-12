import {NetworkEvent} from "../NetworkEvent";
import {OPCode} from "../OPCode";
import {PositionedBuffer} from "../PositionedBuffer";

export class TimeUpdateEvent extends NetworkEvent {

    public readonly time: number;
    private readonly BINARY_SIZE: number = 3;

    /**
     * @param time
     */
    constructor(time: number);
    constructor(buffer: PositionedBuffer);
    constructor(timeOrBuffer: PositionedBuffer|number, time?: number) {
        super();
        if (timeOrBuffer instanceof PositionedBuffer) {
            // TODO: Unused byte
            const unread: number = timeOrBuffer.readInt8();
            this.time = timeOrBuffer.readInt16BE();
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
        return this.BINARY_SIZE;
    }

    /**
     * @override
     */
    public getOPCode(): OPCode {
        return OPCode.UPDATE_TIME;
    }

    /**
     * @override
     */
    public write(buffer: PositionedBuffer): void {
        // TODO: Unused byte
        buffer.writeInt8(0);
        buffer.writeInt16BE(this.time);
    }

}