import {PositionedBuffer} from "../../engine/net/PositionedBuffer";
import {OPCode} from "./OPCode";

/**
 * NetworkEvents are dispatched via a `Buffer`.
 * Data of each NetworkEvent should be prefixed by its opcode (see `NetworkEvent.getOPCode()`).
 */
export abstract class NetworkEvent {

    constructor(buffer?: PositionedBuffer) {}

    /**
     * @return The size of the event in bytes.
     */
    abstract getEventSize(): number;

    /**
     * @return The OPCode associated with the event.
     */
    abstract getOPCode(): OPCode;

    /**
     * Writes all event data to the buffer.
     * This method does not write the event's OPCode.
     * @param buffer The buffer to write to.
     */
    abstract write(buffer: PositionedBuffer): void;

}
