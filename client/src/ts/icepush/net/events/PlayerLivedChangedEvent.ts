import {NetworkEvent} from "../NetworkEvent";
import {PositionedBuffer} from "../../../engine/net/PositionedBuffer";
import {OPCode} from "../OPCode";

/**
 * This event is the server informing the client of the number of lives a particular player has.
 */
export class PlayerLivesChangedEvent extends NetworkEvent {

    private static readonly BINARY_SIZE: number = 3;

    public readonly playerID: number;
    public readonly lives: number;

    /**
     * Creates a new event with the given data.
     * @param playerID The player's ID.
     * @param lives The number of lives the player should have.
     */
    constructor(playerID: number, lives: number);

    /**
     * Reads the playerID and lives from the buffer.
     * @param buffer The buffer to read from.
     */
    constructor(buffer: PositionedBuffer);

    /**
     * Overload constructor.
     */
    constructor(bufferOrID: PositionedBuffer|number, lives?: number) {
        super();
        if (bufferOrID instanceof PositionedBuffer) {
            this.playerID = bufferOrID.readUInt16BE();
            this.lives = bufferOrID.readUInt8();
        } else if (lives !== undefined) {
            this.playerID = bufferOrID;
            this.lives = lives;
        } else {
            throw new Error(`Malformed constructor:\n${bufferOrID}\n${lives}`);
        }
    }

    /**
     * @override
     */
    public getEventSize(): number {
        return PlayerLivesChangedEvent.BINARY_SIZE;
    }

    /**
     * @override
     */
    public getOPCode(): OPCode {
        return OPCode.PLAYER_LIVES_CHANGED;
    }

    /**
     * @override
     */
    public write(buffer: PositionedBuffer): void {
        buffer.writeUInt16BE(this.playerID);
        buffer.writeUInt8(this.lives);
    }

}