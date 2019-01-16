
export class PositionedBuffer {

    public static readonly STRING_ENCODING: BufferEncoding = 'utf8';

    public readonly buffer: Buffer;
    public readonly pos: BufferPosition;

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
     * Sets the position in the buffer.
     * @param position The buffers new position.
     */
    public setPosition(position: number): void {
        this.pos(position, true);
    }

    /**
     * @return The length of the buffer.
     */
    public getLength(): number {
        return this.buffer.byteLength;
    }

    /**
     * @return The underlying buffer.
     */
    public getBuffer(): Buffer {
        return this.buffer;
    }

    /**
     * Reads a `utf8` string from the buffer.
     */
    public readString(): string {
        const length: number = this.readInt16BE();
        return this.buffer.toString(PositionedBuffer.STRING_ENCODING, this.pos(length), this.pos());
    }

    /**
     * Writes a `utf8` string to the buffer.
     * @param s The string to write to the buffer.
     * @return The position in the buffer after writing.
     */
    public writeString(s: string): number {
        this.writeInt16BE(s.length);
        for (let i = 0; i < s.length; i++) {
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
     * @see Buffer.readUInt8
     */
    public readUInt8(): number {
        return this.buffer.readUInt8(this.pos(1));
    }

    /**
     * @see Buffer.writeInt8
     */
    public writeInt8(value: number): number {
        return this.buffer.writeInt8(value, this.pos(1));
    }

    /**
     * @see Buffer.writeUInt8
     */
    public writeUInt8(value: number): number {
        return this.buffer.writeUInt8(value, this.pos(1));
    }

    /**
     * @see Buffer.readInt16BE
     */
    public readInt16BE(): number {
        return (0xff & (this.readInt8())) + (this.readInt8() << 8);
    }

    /**
     * @see Buffer.writeInt16BE
     */
    public writeInt16BE(value: number): number {
        this.writeInt8(value);
        return this.writeInt8(value >> 8);
    }

    /**
     * @see Buffer.readInt32BE
     */
    public readInt32BE(): number {
        return (0xff & this.readInt8())
            + ((0xff & this.readInt8()) << 8)
            + ((0xff & this.readInt8()) << 16)
            + ((0xff & this.readInt8()) << 24);
    }

    /**
     * @see Buffer.writeInt32BE
     */
    public writeInt32BE(value: number): number {
        this.writeInt8(value & 0xff);
        this.writeInt8(value >> 8);
        this.writeInt8(value >> 16);
        return this.writeInt8(value >> 24);
    }

    // region Static Methods

    /**
     * @param s The string to write.
     * @return The number of bytes used to write the string to the buffer.
     */
    public static getStringWriteSize(s: string): number {
        return s.length + 2;
    }

    // endregion

}

/**
 * A position counter for a buffer.
 */
export interface BufferPosition {

    /**
     * @returns The current value of the position.
     */
    (): number;

    /**
     * Increments the position by the given relative value.
     * @param relative The value to increment by.
     * @returns The original position's value.
     */
    (relative: number): number;

    /**
     * Sets the value of the position if `absolute` is `true`, otherwise the positive is incremented.
     * @param relativeOrAbsolute The value to set or increment depending on the value of `absolute`.
     * @param absolute Whether to set or increment the position.
     * @returns The original position's value.
     */
    (relativeOrAbsolute: number, absolute: boolean): number;

}

export namespace BufferPosition {

    /**
     * @param initialPosition The initial value of the position. Defaults to 0.
     * @returns A position counter for a buffer.
     */
    export function create(initialPosition: number = 0): BufferPosition {
        return (relativeOrAbsolute?: number, absolute?: boolean) => {
            const oldPosition = initialPosition;
            if (relativeOrAbsolute != null) {
                if (absolute) {
                    initialPosition = relativeOrAbsolute;
                } else {
                    initialPosition += relativeOrAbsolute;
                }
            }
            return oldPosition;
        };
    }

}