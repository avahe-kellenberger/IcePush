import {NetworkEvent} from "../NetworkEvent";
import {OPCode} from "../OPCode";
import {PositionedBuffer} from "../PositionedBuffer";

export class KeyEvent extends NetworkEvent {

    public readonly key: string;
    private readonly BINARY_SIZE: number;

    /**
     * @param key The key being sent.
     */
    constructor(key: string);
    constructor(buffer: PositionedBuffer, offset: number);
    constructor(bufferOrKey: PositionedBuffer|string, offset?: number) {
        super();
        if (typeof bufferOrKey === 'string') {
            this.key = bufferOrKey;
        } else if (offset !== undefined) {
            this.key = bufferOrKey.readString();
        } else {
            throw new Error('Malformed constructor.');
        }
        this.BINARY_SIZE = PositionedBuffer.getWriteSize(this.key);
    }

    /**
     * @override
     */
    public write(buffer: PositionedBuffer): void {
        buffer.writeString(this.key);
    }

    /**
     * @override
     */
    public getOPCode(): OPCode {
        return OPCode.KEY_PRESSED;
    }

    /**
     * @override
     */
    public getEventSize(): number {
        return this.BINARY_SIZE;
    }

}
