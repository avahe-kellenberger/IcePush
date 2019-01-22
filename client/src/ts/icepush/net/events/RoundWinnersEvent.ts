import {NetworkEvent} from "../NetworkEvent";
import {PositionedBuffer} from "../../../engine/net/PositionedBuffer";
import {OPCode} from "../NetworkEventBuffer";

/**
 * This event is sent from the server to the client indicating a round has been won.
 */
export class RoundWinnersEvent extends NetworkEvent {

    private readonly BINARY_SIZE: number;

    public readonly winnerIDs: number[];

    /**
     * @param winnerIDs The winners of the round (empty array if no winners).
     */
    constructor(winnerIDs: number[]);

    /**
     * @param buffer The buffer from which to read the data.
     */
    constructor(buffer: PositionedBuffer);

    /**
     * Overload constructor.
     */
    constructor(bufferOrWinnerIDs: PositionedBuffer|number[]) {
        super();
        if (bufferOrWinnerIDs instanceof PositionedBuffer) {
            const numWinners: number = bufferOrWinnerIDs.readUInt8();
            this.winnerIDs = new Array(numWinners);
            for (let i = 0; i < numWinners; i++) {
                this.winnerIDs.push(bufferOrWinnerIDs.readUInt8());
            }
        } else {
            this.winnerIDs = bufferOrWinnerIDs;
        }
        this.BINARY_SIZE = 1 + this.winnerIDs.length;
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
        return OPCode.ROUND_WINNERS;
    }

    /**
     * @override
     */
    public write(buffer: PositionedBuffer): void {
        buffer.writeUInt8(this.winnerIDs.length);
        for (const id of this.winnerIDs) {
            buffer.writeUInt8(id);
        }
    }

}