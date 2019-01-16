import {NetworkEvent} from "../NetworkEvent";
import {PositionedBuffer} from "../../../engine/net/PositionedBuffer";
import {OPCode} from "../NetworkEventBuffer";

/**
 * TODO: The currently server implementation uses bytes for the number of player deaths.
 *
 * This event is sent from server to client, indicating a player has died.
 */
export class PlayerDeathEvent extends NetworkEvent {

    private static readonly BINARY_SIZE: number = 3;

    public readonly playerID: number;
    public readonly deathCount: number;

    /**
     * Creates a new event with the given data.
     * @param playerID The player's ID.
     * @param deathCount The number of player deaths.
     */
    constructor(playerID: number, deathCount: number);

    /**
     * Reads the playerID and deathCount from the buffer.
     * @param buffer The buffer to read from.
     */
    constructor(buffer: PositionedBuffer);

    /**
     * Overload constructor.
     */
    constructor(bufferOrID: PositionedBuffer|number, deathCount?: number) {
        super();
        if (bufferOrID instanceof PositionedBuffer) {
            this.playerID = bufferOrID.readInt16BE();
            this.deathCount = bufferOrID.readUInt8();
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
        return PlayerDeathEvent.BINARY_SIZE;
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
        buffer.writeUInt8(this.deathCount);
    }

}