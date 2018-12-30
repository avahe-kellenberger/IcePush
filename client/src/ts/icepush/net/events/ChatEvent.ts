import {NetworkEvent} from "../NetworkEvent";
import {OPCode} from "../OPCode";

export class ChatEvent extends NetworkEvent {

    public readonly chatMessage: string;
    private readonly BINARY_SIZE: number;

    /**
     * @param chatMessage The message being sent.
     */
    constructor(chatMessage: string);
    constructor(buffer: Buffer, offset: number);
    constructor(bufferOrMessage: Buffer|string, offset?: number) {
        super();
        if (typeof bufferOrMessage === 'string') {
            this.chatMessage = bufferOrMessage;
            this.BINARY_SIZE = 1 + Buffer.byteLength(this.chatMessage);
        } else if (offset !== undefined) {
            const length: number = bufferOrMessage.readUInt8(offset);
            this.chatMessage = bufferOrMessage.toString('UTF-8', offset + 1, length);
            this.BINARY_SIZE = 1 + length;
        } else {
            throw new Error('Malformed constructor.');
        }
    }

    /**
     * @override
     */
    public write(buffer: Buffer, offset: number): number {
        offset += buffer.writeUInt8(Buffer.byteLength(this.chatMessage), offset);
        offset += buffer.write(this.chatMessage, offset);
        return offset;
    }

    /**
     * @override
     */
    public getOPCode(): OPCode {
        return OPCode.CHAT;
    }

    /**
     * @override
     */
    public getEventSize(): number {
        return this.BINARY_SIZE;
    }

}
