import {NetworkEvent} from "../NetworkEvent";
import {PositionedBuffer} from "../../../engine/net/PositionedBuffer";
import {OPCode} from "../NetworkEventBuffer";

/**
 * OPCode.SUCCESS is the only important data for this event.
 * Therefore we neither read or write and additional bytes.
 */
export class SuccessEvent extends NetworkEvent {

    private readonly BINARY_SIZE: number = 1;

    public readonly playerID: number;

    /**
     * @param bufferOrPlayerID
     */
    constructor(bufferOrPlayerID: number|PositionedBuffer) {
        super();
        if (typeof bufferOrPlayerID === 'number') {
            this.playerID = bufferOrPlayerID;
        } else {
            this.playerID = bufferOrPlayerID.readUInt8();
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
        return OPCode.SUCCESS;
    }

    /**
     * @override
     */
    public write(buffer: PositionedBuffer): void {
        buffer.writeUInt8(this.playerID);
    }

}