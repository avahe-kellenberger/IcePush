import {NetworkEvent} from "../NetworkEvent";
import {PositionedBuffer} from "../../../engine/net/PositionedBuffer";
import {OPCode} from "../NetworkEventBuffer";

/**
 * Sent from server to client, notifying that another client has logged out.
 */
export class PlayerLoggedOutEvent extends NetworkEvent {

    private static readonly BINARY_SIZE: number = 2;

    public readonly playerID: number;

    /**
     * @param playerID The ID of the player which logged out.
     */
    constructor(playerID: number);

    /**
     * Reads the playerID from the buffer.
     * @param buffer The buffer to read from.
     */
    constructor(buffer: PositionedBuffer);

    /**
     * Overload constructor.
     */
    constructor(bufferOrID: PositionedBuffer|number) {
        super();
        this.playerID = typeof bufferOrID === 'number' ? bufferOrID : bufferOrID.readInt16BE();
    }

    /**
     * @override
     */
    public getEventSize(): number {
        return PlayerLoggedOutEvent.BINARY_SIZE;
    }

    /**
     * @override
     */
    public getOPCode(): OPCode {
        return OPCode.PLAYER_LOGGED_OUT;
    }

    /**
     * @override
     */
    public write(buffer: PositionedBuffer): void {
        buffer.writeInt16BE(this.playerID);
    }

}