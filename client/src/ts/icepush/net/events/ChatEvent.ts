import {NetworkEvent} from "../NetworkEvent";
import {OPCode} from "../OPCode";
import {PositionedBuffer} from "../PositionedBuffer";

class ChatEvent extends NetworkEvent {

    public readonly chatMessage: string;
    private readonly BINARY_SIZE: number;

    /**
     * @param chatMessage The message being sent.
     */
    constructor(chatMessage: string);
    constructor(buffer: PositionedBuffer);
    constructor(bufferOrMessage: PositionedBuffer|string) {
        super();
        if (typeof bufferOrMessage === 'string') {
            this.chatMessage = bufferOrMessage;
        } else {
            this.chatMessage = bufferOrMessage.readStringOld();
        }
        this.BINARY_SIZE = PositionedBuffer.getWriteSizeOld(this.chatMessage);
    }

    /**
     * @override
     */
    public write(buffer: PositionedBuffer): void {
        buffer.writeStringOld(this.chatMessage);
    }

    /**
     * @override
     */
    public getOPCode(): OPCode {
        return OPCode.CHAT_SEND;
    }

    /**
     * @override
     */
    public getEventSize(): number {
        return this.BINARY_SIZE;
    }

}

export class ChatSendEvent extends ChatEvent {
    /**
     * @override
     */
    public getOPCode(): OPCode {
        return OPCode.CHAT_SEND;
    }
}

export class ChatReceiveEvent extends ChatEvent {
    /**
     * @override
     */
    public getOPCode(): OPCode {
        return OPCode.CHAT_RECEIVE;
    }
}
