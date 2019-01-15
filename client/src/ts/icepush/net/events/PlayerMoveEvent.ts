import {NetworkEvent} from "../NetworkEvent";
import {Vector2D} from "../../../engine/math/Vector2D";
import {PositionedBuffer} from "../../../engine/net/PositionedBuffer";
import {OPCode} from "../NetworkEventBuffer";

export class PlayerMoveEvent extends NetworkEvent {

    public readonly playerID: number;
    public readonly location: Vector2D;

    private readonly BINARY_SIZE: number = 6;

    /**
     * @param playerID The player's ID.
     * @param position The player's position.
     */
    constructor(playerID: number, position: Vector2D);
    constructor(buffer: PositionedBuffer);
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
        return this.BINARY_SIZE;
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