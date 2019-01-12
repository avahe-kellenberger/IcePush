import {NetworkEvent} from "../NetworkEvent";
import {OPCode} from "../OPCode";
import {PositionedBuffer} from "../PositionedBuffer";

export class MoveRequestEvent extends NetworkEvent {

    public readonly angle: number;
    private readonly BINARY_SIZE: number = 1;

    /**
     * @param angle The angle of player movement.
     */
    constructor(angle: number);
    constructor(buffer: PositionedBuffer);
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
