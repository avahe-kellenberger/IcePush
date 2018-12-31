/**
 * A position counter for a buffer.
 */
export interface BufferPosition {

    /**
     * Gets the current value of the position.
     * @returns {number} The current value of the position.
     */
    (): number;

    /**
     * Increments the position by the given relative value.
     * @param {number} relative The value to increment by.
     * @returns {number} The original position's value.
     */
    (relative: number): number;

    /**
     * Sets the value of the position if <param>absolute</param> is <code>true</code>, otherwise the positive is incremented.
     * @param {number} relativeOrAbsolute The value to set or increment depending on the value of <param>absolute</param>.
     * @param {boolean} absolute Whether to set or increment the position.
     * @returns {number} The original position's value.
     */
    (relativeOrAbsolute: number, absolute: boolean): number;

}

export namespace BufferPosition {

    /**
     * Creates a position counter for a buffer.
     * @param {number} initialPosition The initial value of the position. Defaults to 0.
     * @returns {BufferPosition}
     */
    export function create(initialPosition?: number): BufferPosition {
        let position: number = initialPosition || 0;
        return (relativeOrAbsolute?: number, absolute?: boolean) => {
            const oldPosition = position;
            if (relativeOrAbsolute != null) {
                if (absolute) {
                    position = relativeOrAbsolute;
                } else {
                    position += relativeOrAbsolute;
                }
            }
            return oldPosition;
        };
    }

}