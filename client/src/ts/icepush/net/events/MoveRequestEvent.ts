import {NetworkEvent} from "../NetworkEvent";
import {PositionedBuffer} from "../../../engine/net/PositionedBuffer";
import {OPCode} from "../NetworkEventBuffer";

/**
 * An event sent from the client to the server, indicating that the client is now attempting to move in a new direction.
 */
export class MoveRequestEvent extends NetworkEvent {

    public readonly angle: number;
    private readonly BINARY_SIZE: number = 1;

    /**
     * Creates an event with the given angle.
     * The angle currently required by the server is a value between 0 and 255,
     * with 0 at the bottom of the circle, rotating counter-clockwise to reach 255.
     *
     * @param angle The angle of player movement.
     */
    constructor(angle: number);

    /**
     * Reads the angle of movement from the buffer.
     * @param buffer The buffer to read from.
     */
    constructor(buffer: PositionedBuffer);

    /**
     * Overload constructor.
     */
    constructor(bufferOrAngle: PositionedBuffer|number) {
        super();
        if (typeof bufferOrAngle === 'number') {
            this.angle = bufferOrAngle;
        } else {
            this.angle = bufferOrAngle.readUInt8();
        }
    }

    /**
     * @override
     */
    public write(buffer: PositionedBuffer): void {
        buffer.writeUInt8(this.angle);
    }

    /**
     * @override
     */
    public getOPCode(): OPCode {
        return OPCode.MOVE_REQUEST;
    }

    /**
     * @override
     */
    public getEventSize(): number {
        return this.BINARY_SIZE;
    }

}
