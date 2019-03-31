export class CanvasUtils {

    /**
     * @param canvas The canvas from which to retrieve the rendering context.
     * @return The `CanvasRenderingContext2D` derived from `canvas`.
     */
    public static getContext(canvas: HTMLCanvasElement): CanvasRenderingContext2D {
        const ctx: CanvasRenderingContext2D|null = canvas.getContext("2d");
        if (ctx == null) {
            throw new Error("Unable to get context from canvas.");
        }
        return ctx;
    }

    /**
     * @param width The width of the canvas.
     * @param height The height of the canvas.
     * @param render An optional callback to render to the canvas before it is returned.
     * @return An `HTMLCanvasElement` with the given size.
     */
    public static create(width: number, height: number, render?: (ctx: CanvasRenderingContext2D) => void): HTMLCanvasElement {
        const canvas = document.createElement("canvas");
        canvas.width = width;
        canvas.height = height;
        if (render != null) {
            render(this.getContext(canvas));
        }
        return canvas;
    }

    /**
     * Turns an image into a canvas.
     *
     * @param {CanvasImageSource} imageSource The image being transformed into a canvas.
     * @returns {HTMLCanvasElement} The canvas representation of the image.
     */
    public static imageToCanvas(imageSource: CanvasImageSource): HTMLCanvasElement {
        return this.create(this.toPixels(imageSource.width), this.toPixels(imageSource.height),
            ctx => ctx.drawImage(imageSource, 0, 0));
    }

    /**
     * Converts the given size object to a pixel value.
     * @param size
     */
    public static toPixels(size: number|SVGAnimatedLength): number {
        return typeof size === "number" ? size : size.animVal.value;
    }

    /**
     * @param str The string to render.
     * @param font The font of the text. NOTE: Does not work with non-pixel units.
     * @param fillStyle The style of the filled text.
     * @param strokeStyle The style of the stroke.
     * @returns An `HTMLCanvasElement` form of the given string.
     */
    public static stringToCanvas(str: string, font: string, fillStyle?: string|null, strokeStyle?: string|null): HTMLCanvasElement {
        const canvas = document.createElement("canvas");
        const ctx = this.getContext(canvas);
        ctx.font = font;
        const width: number = ctx.measureText(str).width * 2;
        const height: number = parseInt(font) * 2;
        // Important: Setting width and height of canvas automatically resets canvas state.
        canvas.width = width;
        canvas.height = height;
        // Setting font again because canvas state was reset upon size change.
        ctx.font = font;
        ctx.textAlign = "center";
        ctx.textBaseline = "middle";
        if (fillStyle != null) {
            ctx.fillStyle = fillStyle;
            ctx.fillText(str, width * 0.5, height * 0.5);
        }
        if (strokeStyle != null) {
            ctx.strokeStyle = strokeStyle;
            ctx.strokeText(str, width * 0.5, height * 0.5);
        }
        return CanvasUtils.trimTransparentPixels(canvas);
    }

    /**
     * Renders multiple lines of text from within one string, separated by the CR character.
     * @param ctx The context to render upon.
     * @param str The string to render.
     * @param x The x location to render.
     * @param y The y location to render.
     * @param fontSize The size of the font.
     */
    public static fillTextMultiline(ctx: CanvasRenderingContext2D, str: string, x: number, y: number, fontSize: number): void {
        // Cache old canvas settings.
        const oldTextAlign = ctx.textAlign;
        const oldTextBaseline = ctx.textBaseline;

        ctx.textAlign = "center";
        ctx.textBaseline = "middle";

        const lines: string[] = str.split('\n');
        const lineHeight: number = fontSize * 1.2;
        for (const line of lines) {
            ctx.fillText(line, x, y);
            y += lineHeight;
        }

        // Restore old canvas settings.
        ctx.textAlign = oldTextAlign;
        ctx.textBaseline = oldTextBaseline;
    }

    /**
     * @param canvas The canvas to trim.
     * @returns The given canvas with the sides trimmed of all completely transparent pixels.
     */
    public static trimTransparentPixels(canvas: HTMLCanvasElement): HTMLCanvasElement {
        const ctx = this.getContext(canvas);

        const width: number = canvas.width;
        const height: number = canvas.height;

        const map = ctx.getImageData(0, 0, width, height);
        const imageData = map.data;

        const stride: number = 4;
        const scanLine: number = width * stride;

        let minX: number = 0;
        let minY: number = 0;
        let maxX: number = width;
        let maxY: number = height;

        trimming: {
            // Iterate down from the top until a non-transparent row is found.
            top: {
                for (let i = 3; minY < height; minY++) {
                    // 'i' should be at the alpha byte of the first pixel on the left of the current row.
                    const nextRow: number = i + scanLine;
                    // Process current row.
                    for (let xIndex = i; xIndex < nextRow; xIndex += stride) {
                        if (imageData[xIndex] != 0) {
                            // Found non-transparent row.
                            break top;
                        }
                    }
                    // Move to next row.
                    i = nextRow;
                }
                // Terminate early: Canvas is completely transparent.
                minX = 0;
                maxX = 0;
                minY = 0;
                maxY = 0;
                break trimming;
            }

            // Iterate up from the bottom until a non-transparent row is found.
            bottom: {
                for (let i = (height * scanLine) - 1; maxY >= 0; maxY--) {
                    // 'i' should be at the alpha byte of the first pixel on the right of the current row.
                    const previousRow: number = i - scanLine;
                    // Process current row.
                    for (let xIndex = i; xIndex > previousRow; xIndex -= stride) {
                        if (imageData[xIndex] != 0) {
                            // Found non-transparent row.
                            break bottom;
                        }
                    }
                    // Move to previous row.
                    i = previousRow;
                }
            }

            const remainingHeight: number = maxY - minY;
            const remainingOffset: number = remainingHeight * scanLine;

            // Iterate from the left until a non-transparent column is found.
            left: {
                for (let i = (minY * scanLine) + 3; minX < width; i += stride, minX++) {
                    // 'i' should be at the alpha byte of the pixel at the top of the current column.
                    const maxColumnIndex: number = i + remainingOffset;
                    // Process current column.
                    for (let yIndex = i; yIndex < maxColumnIndex; yIndex += scanLine) {
                        if (imageData[yIndex] != 0) {
                            // Found non-transparent column.
                            break left;
                        }
                    }
                }
            }

            // Iterate from the right until a non-transparent column is found.
            right: {
                for (let i = ((minY + 1) * scanLine) - 1; maxX >= 0; i -= stride, maxX--) {
                    // 'i' should be at the alpha byte of the pixel at the top of the current column.
                    const maxColumnIndex: number = i + remainingOffset;
                    // Process current column.
                    for (let yIndex = i; yIndex < maxColumnIndex; yIndex += scanLine) {
                        if (imageData[yIndex] != 0) {
                            // Found non-transparent column.
                            break right;
                        }
                    }
                }
            }
        }
        // Crop the canvas according to the calculated min and max x and y values.
        return CanvasUtils.getSubCanvas(canvas, minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * @param canvas The parent canvas from which to create a sub canvas.
     * @param x The x location of the upper-left corner of the rectangular region to copy.
     * @param y The y location of the upper-left corner of the rectangular region to copy.
     * @param width The width of the region to copy.
     * @param height The height of the region to copy.
     * @returns A sub-canvas from a source canvas.
     */
    public static getSubCanvas(canvas: HTMLCanvasElement, x: number, y: number, width: number, height: number): HTMLCanvasElement {
        return this.create(width, height, ctx => {
            ctx.drawImage(canvas, x, y, width, height, 0, 0, width, height);
        });
    }

}