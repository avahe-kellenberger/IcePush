import {NetworkEvent} from "../NetworkEvent";
import {PositionedBuffer} from "../../../engine/net/PositionedBuffer";
import {OPCode} from "../NetworkEventBuffer";

/**
 * TODO: The currently server implementation uses signed bytes for the number of player deaths.
 * This needs to be fixed rather immediately.
 */
export class PlayerDeathEvent extends NetworkEvent {

    public readonly playerID: number;
    public readonly deathCount: number;
    private readonly BINARY_SIZE: number = 1;

    /**
     *
     * @param playerID The player's ID.
     * @param deathCount The number of player deaths.
     */
    constructor(playerID: number, deathCount: number);
    constructor(buffer: PositionedBuffer);
    constructor(bufferOrID: PositionedBuffer|number, deathCount?: number) {
        super();
        if (bufferOrID instanceof PositionedBuffer) {
            this.playerID = bufferOrID.readInt16BE();
            this.deathCount = bufferOrID.readInt8();
        } else if (deathCount !== undefined) {
            this.playerID = bufferOrID;
            this.deathCount = deathCount;
        } else {
            throw new Error(`Malformed constructor:\n${bufferOrID}\n${deathCount}`);
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
        return OPCode.PLAYER_DEATH;
    }

    /**
     * @override
     */
    public write(buffer: PositionedBuffer): void {
        buffer.writeInt16BE(this.playerID);
        buffer.writeInt8(this.deathCount);
    }

}