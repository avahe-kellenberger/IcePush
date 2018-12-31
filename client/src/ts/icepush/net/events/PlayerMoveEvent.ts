import {NetworkEvent} from "../NetworkEvent";
import {OPCode} from "../OPCode";
import {PositionedBuffer} from "../PositionedBuffer";
import {Vector2D} from "../../../engine/math/Vector2D";

export class PlayerMoveEvent extends NetworkEvent {

    public readonly playerID: number;
    public readonly position: Vector2D;

    private readonly BINARY_SIZE: number = 6;

    /**
     * @param playerID The player's ID.
     * @param position The player's position.
     */
    constructor(playerID: number, position: Vector2D);
    constructor(buffer: PositionedBuffer);
    constructor(idOrBuffer: PositionedBuffer|number, position?: Vector2D) {
        super();
        if (idOrBuffer instanceof PositionedBuffer) {
            this.playerID = idOrBuffer.readInt16BE();
            this.position = new Vector2D(idOrBuffer.readInt16BE(), idOrBuffer.readInt16BE());
        } else if (position !== undefined) {
            this.playerID = idOrBuffer;
            this.position = position;
        } else {
            throw new Error(`Malformed constructor:\n${idOrBuffer}\n${position}`);
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
        buffer.writeInt16BE(this.position.x);
        buffer.writeInt16BE(this.position.y);
    }

}