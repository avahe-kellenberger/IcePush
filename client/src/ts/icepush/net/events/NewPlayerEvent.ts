import {NetworkEvent} from "../NetworkEvent";
import {PositionedBuffer} from "../../../engine/net/PositionedBuffer";
import {OPCode} from "../NetworkEventBuffer";

/**
 * This event is sent from the server to the client indicating a new player has been added to the game.
 */
export class NewPlayerEvent extends NetworkEvent {

    private readonly BINARY_SIZE: number;

    public readonly playerID: number;
    public readonly type: number;
    public readonly username: string;
    public readonly lives: number;

    /**
     * Reads the new player's data from the buffer.
     * @param buffer The buffer to read from.
     */
    constructor(buffer: PositionedBuffer) {
        super();
        this.playerID = buffer.readInt16BE();
        this.type = buffer.readInt8();
        this.username = buffer.readString();
        this.lives = buffer.readUInt8();
        this.BINARY_SIZE = 4 + PositionedBuffer.getStringWriteSize(this.username);
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
        return OPCode.NEW_PLAYER;
    }

    /**
     * @override
     */
    public write(buffer: PositionedBuffer): void {}

}
