import {NetworkEvent} from "../NetworkEvent";
import {PositionedBuffer} from "../../../engine/net/PositionedBuffer";
import {OPCode} from "../NetworkEventBuffer";

/**
 * An event sent from the client to the server, indicating that the client has clicked on the screen
 * and wants to shoot a projectile in the indicated direction.
 */
export class ProjectileRequestEvent extends NetworkEvent {

    public readonly x: number;
    public readonly y: number;

    private readonly BINARY_SIZE: number = 4;

    /**
     * Creates an event with the given clicked coordinates.
     * @param x The x location clicked on the screen.
     * @param y The y location clicked on the screen.
     */
    constructor(x: number, y: number);

    /**
     * Reads the angle of movement from the buffer.
     * @param buffer The buffer to read from.
     */
    constructor(buffer: PositionedBuffer);

    /**
     * Overload constructor.
     */
    constructor(xOrBuffer: PositionedBuffer|number, y?: number) {
        super();
        if (xOrBuffer instanceof PositionedBuffer) {
            this.x = xOrBuffer.readInt16BE();
            this.y = xOrBuffer.readInt16BE();
        } else if (y !== undefined) {
            this.x = Math.round(xOrBuffer);
            this.y = Math.round(y);
        } else {
            throw new Error(`Malformed constructor:\n${xOrBuffer}\n${y}`);
        }
    }

    /**
     * @override
     */
    public write(buffer: PositionedBuffer): void {
        buffer.writeInt16BE(this.x);
        buffer.writeInt16BE(this.y);
    }

    /**
     * @override
     */
    public getOPCode(): OPCode {
        return OPCode.PROJECTILE_REQUEST;
    }

    /**
     * @override
     */
    public getEventSize(): number {
        return this.BINARY_SIZE;
    }

}
