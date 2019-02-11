import {Layer} from "../../../engine/game/Layer";
import {Vector2D} from "../../../engine/math/Vector2D";

export class InfoLayer extends Layer {

    private readonly timeRenderLocation: Vector2D;

    private roundSecondsRemaining: number|undefined;
    private isRoundCountingDown: boolean;

    /**
     * @param timeRenderLocation The location to render the round's remaining time.
     * @param zOrder The z-order of the layer.
     */
    constructor(timeRenderLocation: Vector2D, zOrder: number = 1) {
        super(zOrder);
        this.timeRenderLocation = timeRenderLocation;
        this.isRoundCountingDown = false;
    }

    /**
     * Sets the time remaining in the round to render.
     * @param secondsRemaining The time remaining in seconds.
     */
    public setRoundTimeRemaining(secondsRemaining: number|undefined): void {
        this.roundSecondsRemaining = secondsRemaining;
    }

    /**
     * @param countingDown If the round is counting down.
     */
    public setIsRoundCountingDown(countingDown: boolean): void {
        this.isRoundCountingDown = countingDown;
    }

    /**
     * @override
     */
    public render(ctx: CanvasRenderingContext2D): void {
        super.render(ctx);

        if (this.roundSecondsRemaining !== undefined) {
            this.renderRoundTimeRemaining(ctx, this.roundSecondsRemaining);
        }
    }

    /**
     * @param ctx The context to render onto.
     * @param seconds The time remaining in the round.
     */
    public renderRoundTimeRemaining(ctx: CanvasRenderingContext2D, seconds: number): void {
        // Render the round time remaining.
        ctx.fillStyle = 'red';
        const fontSize: number = 14;
        ctx.font = `${fontSize}px Arial`;


        const minutes: number = Math.floor(seconds / 60);
        seconds -= minutes * 60;
        const secondsTruncated: string = Math.ceil(seconds).toString();
        const prefix: string = this.isRoundCountingDown ? `Round Start In` : `Round Time Remaining`;
        const str: string = `${prefix}: ${minutes}:${secondsTruncated.length === 1 ? `0` + secondsTruncated : secondsTruncated}`;
        const metrics: TextMetrics = ctx.measureText(str);

        /*
         * See https://developer.mozilla.org/en-US/docs/Web/CSS/line-height#Values for an explanation.
         * 'Desktop browsers (including Firefox) use a default value of roughly 1.2, depending on the element's font-family.'
         * We multiply by '0.6' to halve the height.
         */
        const height = fontSize * 0.6;
        ctx.fillText(str, this.timeRenderLocation.x - metrics.width * 0.5, this.timeRenderLocation.y + height * 2);
    }

}