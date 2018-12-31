import {BufferPosition} from "../../engine/util/BufferPosition";

export class PositionedBuffer {

    public static readonly STRING_ENCODING: BufferEncoding = 'utf8';

    private readonly buffer: Buffer;
    private readonly pos: BufferPosition;

    /**
     * Reads from the given buffer and automatically keeps track of its position.
     * @param buffer The buffer to read from.
     * @param position The position from within the buffer to begin reading.
     */
    constructor(buffer: Buffer, position?: number) {
        this.buffer = buffer;
        this.pos = BufferPosition.create(position);
    }

    /**
     * @return The current position in the buffer.
     */
    public getPosition(): number {
        return this.pos();
    }

    /**
     * @return The underlying buffer.
     */
    public getBuffer(): Buffer {
        return this.buffer;
    }

    /**
     * Reads a `utf8` string from the buffer.
     * The string size is determined by first calling `PositionedBuffer.readInt16BE`,
     * then using that number as the number of bytes to read into a string.
     */
    public readString(): string {
        const size: number = this.readInt16BE();
        return this.buffer.toString(PositionedBuffer.STRING_ENCODING, this.pos(size), this.pos());
    }

    /**
     * Reads a `utf8` string from the buffer.
     *
     * NOTE: This function is a duplication of the functionality of the old client.
     * This method is sloppy and uses more space than needed.
     * It will be replaced by PositionedBuffer.readString() in the future.
     */
    public readStringOld(): string {
        const length: number = this.readInt16BE();
        const chars: string[] = [];
        for (let i = 0; i < length; i++) {
            chars.push(String.fromCharCode(this.readInt8()));
        }
        return chars.join();
    }

    /**
     * Writes a `utf8` string to the buffer.
     * @param s The string to write to the buffer.
     * @return The position in the buffer after writing.
     */
    public writeString(s: string): number {
        const stringSize: number = Buffer.byteLength(s);
        this.writeInt16BE(stringSize);
        this.pos(this.buffer.write(s, this.pos(), stringSize, PositionedBuffer.STRING_ENCODING), true);
        return this.pos();
    }

    /**
     * Writes a `utf8` string to the buffer.
     *
     * NOTE: This function is a duplication of the functionality of the old client.
     * This method is sloppy and uses more space than needed.
     * It will be replaced by PositionedBuffer.writeString() in the future.
     *
     * @param s The string to write to the buffer.
     * @return The position in the buffer after writing.
     */
    public writeStringOld(s: string): number {
        const length = s.length;
        this.writeInt16BE(length);
        for (let i = 0; i < length; i++) {
            this.writeInt8(s.charCodeAt(i));
        }
        return this.pos();
    }

    /**
     * @see Buffer.readInt8
     */
    public readInt8(): number {
        return this.buffer.readInt8(this.pos(1));
    }

    /**
     * @see Buffer.writeInt8
     */
    public writeInt8(value: number): number {
        return this.buffer.writeInt8(value, this.pos(1));
    }

    /**
     * @see Buffer.readInt16BE
     */
    public readInt16BE(): number {
        return this.buffer.readInt16BE(this.pos(2));
    }

    /**
     * @see Buffer.writeInt16BE
     */
    public writeInt16BE(value: number): number {
        return this.buffer.writeInt16BE(value, this.pos(2));
    }

    /**
     * @see Buffer.readInt32BE
     */
    public readInt32BE(): number {
        return this.buffer.readInt32BE(this.pos(4));
    }

    /**
     * @see Buffer.writeInt32BE
     */
    public writeInt32BE(value: number): number {
        return this.buffer.writeInt32BE(value, this.pos(4));
    }

    // region Static Methods

    /**
     * @param s The string that would be written.
     * @return The number of bytes that would be used to write the string.
     */
    public static getWriteSize(s: string): number {
        // + 2 is for the two bytes needed to write the size of the string.
        return Buffer.byteLength(s) + 2;
    }

    /**
     * NOTE: This function is a duplication of the functionality of the old client.
     * This method is sloppy and uses more space than needed.
     * It will be replaced by PositionedBuffer.getWriteSize() in the future.
     *
     * @param s The string that would be written.
     * @return The number of bytes that would be used to write the string.
     */
    public static getWriteSizeOld(s: string): number {
        return s.length + 2;
    }

    // endregion

}
