import {NetworkEvent} from "../NetworkEvent";
import {OPCode} from "../OPCode";

export class KeyEvent extends NetworkEvent {

    public readonly key: string;
    private readonly BINARY_SIZE: number;

    /**
     * @param key The key being sent.
     */
    constructor(key: string);
    constructor(buffer: Buffer, offset: number);
    constructor(bufferOrKey: Buffer|string, offset?: number) {
        super();
        if (typeof bufferOrKey === 'string') {
            this.key = bufferOrKey;
            this.BINARY_SIZE = 1 + Buffer.byteLength(this.key);
        } else if (offset !== undefined) {
            const length: number = bufferOrKey.readUInt8(offset);
            this.key = bufferOrKey.toString('UTF-8', offset + 1, length);
            this.BINARY_SIZE = 1 + length;
        } else {
            throw new Error('Malformed constructor.');
        }
    }

    /**
     * @override
     */
    public write(buffer: Buffer, offset: number): number {
        offset += buffer.writeUInt8(Buffer.byteLength(this.key), offset);
        offset += buffer.write(this.key, offset);
        return offset;
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
