import {NetworkEvent} from "../NetworkEvent";
import {Vector2D} from "../../../engine/math/Vector2D";
import {PositionedBuffer} from "../../../engine/net/PositionedBuffer";
import {OPCode} from "../NetworkEventBuffer";

/**
 * This event is sent from the server to the client indicating a new object has been added to the game.
 */
export class NewObjectEvent extends NetworkEvent {

    private static readonly BINARY_SIZE: number = 7;

    public readonly id: number;
    public readonly type: number;
    public readonly location: Vector2D;

    /**
     * Constructs an event indicating a new object.
     * @param id The object's ID.
     * @param type The object's type.
     * @param location The object's location.
     */
    constructor(id: number, type: number, location: Vector2D);

    /**
     * Reads the object's data from the buffer.
     * @param buffer The buffer to read from.
     */
    constructor(buffer: PositionedBuffer);

    /**
     * Overload constructor.
     */
    constructor(idOrBuffer: PositionedBuffer|number, type?: number, location?: Vector2D) {
        super();
        if (idOrBuffer instanceof PositionedBuffer) {
            this.id = idOrBuffer.readInt16BE();
            this.type = idOrBuffer.readInt8();
            this.location = new Vector2D(idOrBuffer.readInt16BE(), idOrBuffer.readInt16BE());
        } else if (type !== undefined && location !== undefined) {
            this.id = idOrBuffer;
            this.type = type;
            this.location = location;
        } else {
            throw new Error(`Malformed constructor:\n${idOrBuffer}\n${location}\n${location}`);
        }
    }

    /**
     * @override
     */
    public getEventSize(): number {
        return NewObjectEvent.BINARY_SIZE;
    }

    /**
     * @override
     */
    public getOPCode(): OPCode {
        return OPCode.NEW_OBJECT;
    }

    /**
     * @override
     */
    public write(buffer: PositionedBuffer): void {
        buffer.writeInt16BE(this.id);
        buffer.writeInt8(this.type);
        buffer.writeInt16BE(this.location.x);
        buffer.writeInt16BE(this.location.y);
    }

}
