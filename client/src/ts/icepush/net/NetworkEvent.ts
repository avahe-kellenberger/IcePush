import {OPCode} from "./OPCode";

/**
 *
 */
export abstract class NetworkEvent {

    /**
     * Writes all event data to the buffer at the given offset.
     * @param buffer The buffer to write to.
     * @param offset the position in the buffer to begin writing.
     */
    abstract write(buffer: Buffer, offset: number): number;

    /**
     * @return The size of the event in bytes.
     */
    abstract getEventSize(): number;

    /**
     * @return The OPCode associated with the event.
     */
    abstract getOPCode(): OPCode;

}
