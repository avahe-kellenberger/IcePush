import {NetworkEvent} from "../NetworkEvent";
import {Vector2D} from "../../../engine/math/Vector2D";
import {PositionedBuffer} from "../../../engine/net/PositionedBuffer";
import {OPCode} from "../NetworkEventBuffer";

/**
 * Sent from server to client, indicating an object in the game has moved to a new location.
 */
export class ObjectMovedEvent extends NetworkEvent {

    private static readonly BINARY_SIZE: number = 6;

    public readonly id: number;
    public readonly location: Vector2D;

    /**
     * Reads the object's location information from the buffer.
     * @param buffer The buffer to read from.
     */
    constructor(buffer: PositionedBuffer);

    /**
     * Overload constructor.
     */
    constructor(buffer: PositionedBuffer) {
        super();
        this.id = buffer.readInt16BE();
        this.location = new Vector2D(buffer.readInt16BE(), buffer.readInt16BE());
    }

    /**
     * @override
     */
    public getEventSize(): number {
        return ObjectMovedEvent.BINARY_SIZE;
    }

    /**
     * @override
     */
    public getOPCode(): OPCode {
        return OPCode.OBJECT_MOVED;
    }

    /**
     * @override
     */
    public write(buffer: PositionedBuffer): void {
        buffer.writeInt16BE(this.id);
        buffer.writeInt16BE(this.location.x);
        buffer.writeInt16BE(this.location.y);
    }

}
