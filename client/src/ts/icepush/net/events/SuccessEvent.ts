import {NetworkEvent} from "../NetworkEvent";
import {PositionedBuffer} from "../../../engine/net/PositionedBuffer";
import {OPCode} from "../OPCode";

/**
 * Sent from the server to the client upon login, indicating that the login succeeded.
 * The playerID given by the server is to be used as the local player's ID number.
 */
export class SuccessEvent extends NetworkEvent {

    private static readonly BINARY_SIZE: number = 1;

    public readonly playerID: number;

    /**
     * Constructs the event with the player's server-assigned ID.
     * @param playerID The player's ID.
     */
    constructor(playerID: number);

    /**
     * Reads the player's assigned ID from the buffer.
     * @param buffer The buffer to read from.
     */
    constructor(buffer: PositionedBuffer);

    /**
     * Overload constructor.
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
        return SuccessEvent.BINARY_SIZE;
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