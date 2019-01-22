import {NetworkEvent} from "../NetworkEvent";
import {PositionedBuffer} from "../../../engine/net/PositionedBuffer";
import {OPCode} from "../NetworkEventBuffer";

/**
 * Base class for chat events sent to and from the server.
 */
export abstract class ChatEvent extends NetworkEvent {

    public readonly chatMessage: string;
    private readonly BINARY_SIZE: number;

    /**
     * Prepares a ChatEvent to be sent with a message.
     * @param message The message to send.
     */
    constructor(message: string);

    /**
     * Creates a ChatEvent by reading the message from the buffer.
     * @param buffer The buffer containing the chat message.
     */
    constructor(buffer: PositionedBuffer);

    /**
     * Overload constructor.
     */
    constructor(bufferOrMessage: PositionedBuffer|string) {
        super();
        if (typeof bufferOrMessage === 'string') {
            this.chatMessage = bufferOrMessage;
        } else {
            this.chatMessage = bufferOrMessage.readString();
        }
        this.BINARY_SIZE = PositionedBuffer.getStringWriteSize(this.chatMessage);
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
    public getEventSize(): number {
        return this.BINARY_SIZE;
    }

}

/**
 * An event used to send a chat message to the server.
 */
export class ChatSendEvent extends ChatEvent {
    /**
     * @override
     */
    public getOPCode(): OPCode {
        return OPCode.CHAT_SEND;
    }
}

/**
 * An event sent by the server containing a message.
 */
export class ChatReceiveEvent extends ChatEvent {
    /**
     * @override
     */
    public getOPCode(): OPCode {
        return OPCode.CHAT_RECEIVE;
    }
}
