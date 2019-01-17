import {NetworkEvent} from "../NetworkEvent";
import {Player} from "../../entity/Player";
import {PositionedBuffer} from "../../../engine/net/PositionedBuffer";
import {OPCode} from "../NetworkEventBuffer";

/**
 * This event is sent from the server to the client indicating a new player has been added to the game.
 */
export class NewPlayerEvent extends NetworkEvent {

    private readonly BINARY_SIZE: number;

    public readonly playerID: number;
    public readonly type: Player.Type;
    public readonly username: string;
    public readonly lives: number;

    /**
     * Creates an event from the given player and playerID.
     *
     * @param playerID The player's ID.
     * @param player The player object.
     */
    constructor(playerID: number, player: Player);

    /**
     * Reads the new player's data from the buffer.
     * @param buffer The buffer to read from.
     */
    constructor(buffer: PositionedBuffer);

    /**
     * Overload constructor.
     */
    constructor(bufferOrID: PositionedBuffer|number, player?: Player) {
        super();
        if (bufferOrID instanceof PositionedBuffer) {
            this.playerID = bufferOrID.readInt16BE();
            this.type = bufferOrID.readInt8();
            this.username = bufferOrID.readString();
            this.lives = bufferOrID.readUInt8();
        } else if (player !== undefined) {
            this.playerID = bufferOrID;
            this.type = player.getType();
            this.username = player.getName();
            this.lives = player.getLives();
        } else {
            throw new Error(`Malformed constructor:\n${bufferOrID}\n${player}`);
        }
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
    public write(buffer: PositionedBuffer): void {
        buffer.writeUInt16BE(this.playerID);
        buffer.writeUInt8(this.type);
        buffer.writeString(this.username);
        buffer.writeUInt8(this.lives);
    }

}