import {NetworkEvent} from "../NetworkEvent";
import {OPCode} from "../OPCode";
import {PositionedBuffer} from "../PositionedBuffer";

export class ChatEvent extends NetworkEvent {

    public readonly chatMessage: string;
    private readonly BINARY_SIZE: number;

    /**
     * @param chatMessage The message being sent.
     */
    constructor(chatMessage: string);
    constructor(buffer: PositionedBuffer, offset: number);
    constructor(bufferOrMessage: PositionedBuffer|string, offset?: number) {
        super();
        if (typeof bufferOrMessage === 'string') {
            this.chatMessage = bufferOrMessage;
        } else if (offset !== undefined) {
            this.chatMessage = bufferOrMessage.readString();
        } else {
            throw new Error('Malformed constructor.');
        }
        this.BINARY_SIZE = PositionedBuffer.getWriteSize(this.chatMessage);
    }

    /**
     * @override
     */
    public write(buffer: PositionedBuffer): void {
        buffer.writeString(this.chatMessage);
    }

    /**
     * @override
     */
    public getOPCode(): OPCode {
        // TODO: Difference between CHAT and NEW_CHAT_MESSAGE?
        return OPCode.CHAT;
    }

    /**
     * @override
     */
    public getEventSize(): number {
        return this.BINARY_SIZE;
    }

}
