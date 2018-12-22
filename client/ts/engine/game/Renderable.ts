export interface Renderable {

    /**
     * Renders to the context.
     * @param ctx The rendering context.
     */
    render(ctx: CanvasRenderingContext2D): void;

}