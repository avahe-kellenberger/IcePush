import {NetworkEvent} from "../NetworkEvent";
import {OPCode} from "../OPCode";
import {PositionedBuffer} from "../PositionedBuffer";

export class PingEvent extends NetworkEvent {

    private readonly BINARY_SIZE: number = 0;

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
        return OPCode.PING;
    }

    /**
     * @override
     */
    public write(buffer: PositionedBuffer): void {}

}
