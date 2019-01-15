import {NetworkEvent} from "../NetworkEvent";
import {PositionedBuffer} from "../../../engine/net/PositionedBuffer";
import {OPCode} from "../NetworkEventBuffer";

export class PlayerLoggedOutEvent extends NetworkEvent {

    public readonly playerID: number;
    private readonly BINARY_SIZE: number = 2;

    /**
     * @param playerID The ID of the player which logged out.
     */
    constructor(playerID: number);
    constructor(buffer: PositionedBuffer);
    constructor(bufferOrID: PositionedBuffer|number) {
        super();
        this.playerID = typeof bufferOrID === 'number' ? bufferOrID : bufferOrID.readInt16BE();
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
        return OPCode.PLAYER_LOGGED_OUT;
    }

    /**
     * @override
     */
    public write(buffer: PositionedBuffer): void {
        buffer.writeInt16BE(this.playerID);
    }

}