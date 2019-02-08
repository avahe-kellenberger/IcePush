import {NetworkEvent} from "../NetworkEvent";
import {Vector2D} from "../../../engine/math/Vector2D";
import {PositionedBuffer} from "../../../engine/net/PositionedBuffer";
import {OPCode} from "../OPCode";

/**
 * Sent from server to client, giving another player's new position (after being moved).
 */
export class PlayerMovedEvent extends NetworkEvent {

    private static readonly BINARY_SIZE: number = 6;

    public readonly playerID: number;
    public readonly location: Vector2D;

    /**
     * Constructs an event indicating a player's new position.
     * @param playerID The player's ID.
     * @param position The player's position.
     */
    constructor(playerID: number, position: Vector2D);

    /**
     * Reads the player's location information from the buffer.
     * @param buffer The buffer to read from.
     */
    constructor(buffer: PositionedBuffer);

    /**
     * Overload constructor.
     */
    constructor(idOrBuffer: PositionedBuffer|number, location?: Vector2D) {
        super();
        if (idOrBuffer instanceof PositionedBuffer) {
            this.playerID = idOrBuffer.readInt16BE();
            this.location = new Vector2D(idOrBuffer.readInt16BE(), idOrBuffer.readInt16BE());
        } else if (location !== undefined) {
            this.playerID = idOrBuffer;
            this.location = location;
        } else {
            throw new Error(`Malformed constructor:\n${idOrBuffer}\n${location}`);
        }
    }

    /**
     * @override
     */
    public getEventSize(): number {
        return PlayerMovedEvent.BINARY_SIZE;
    }

    /**
     * @override
     */
    public getOPCode(): OPCode {
        return OPCode.PLAYER_MOVE;
    }

    /**
     * @override
     */
    public write(buffer: PositionedBuffer): void {
        buffer.writeInt16BE(this.playerID);
        buffer.writeInt16BE(this.location.x);
        buffer.writeInt16BE(this.location.y);
    }

}