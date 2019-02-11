import {Layer} from "../../../engine/game/Layer";
import {Vector2D} from "../../../engine/math/Vector2D";
import {Renderable} from "../../../engine/game/ui/Renderable";
import {ILocatable, Locatable} from "../../../engine/game/entity/Locatable";

export class InfoPane implements ILocatable, Locatable, Renderable {

    private image: HTMLImageElement;
    private location: Vector2D;

    /**
     * @param image The image to display.
     * @param location The location to render the pane.
     */
    constructor(image: HTMLImageElement, location: Vector2D = Vector2D.ZERO) {
        this.image = image;
        this.location = location;
    }

    /**
     * @override
     */
    public getLocation(): Vector2D {
        return this.location;
    }

    /**
     * @override
     */
    public setLocation(loc: Vector2D): boolean {
        if (this.location.equals(loc)) {
            return false;
        }
        this.location = loc;
        return true;
    }

    /**
     * @override
     */
    public translate(delta: Vector2D): void {
        this.setLocation(this.location.addVector(delta));
    }

    /**
     * @return The image to render.
     */
    public getImage(): HTMLImageElement {
        return this.image;
    }

    /**
     * Sets the image to be rendered.
     * @param image The image to set.
     */
    public setImage(image: HTMLImageElement): void {
        this.image = image;
    }

    /**
     * @override
     */
    public render(ctx: CanvasRenderingContext2D): void {
        ctx.drawImage(this.image, this.location.x - this.image.width * 0.5,
                                  this.location.y - this.image.height * 0.5);
    }

}

export class InfoLayer extends Layer {

    private readonly infoPaneMap: Map<number, InfoPane>;
    private readonly timeRenderLocation: Vector2D;

    private roundSecondsRemaining: number|undefined;
    private isRoundCountingDown: boolean;

    /**
     * @param timeRenderLocation The location to render the round's remaining time.
     * @param zOrder The z-order of the layer.
     */
    constructor(timeRenderLocation: Vector2D, zOrder: number = 1) {
        super(zOrder);
        this.infoPaneMap = new Map();
        this.timeRenderLocation = timeRenderLocation;
        this.isRoundCountingDown = false;
    }

    /**
     * Sets the pane associated with the object.
     * @param objectID The ID of the object associated with the info pane.
     * @param infoPane The InfoPane to associate with the objectID.
     */
    public setObjectInfoPane(objectID: number, infoPane: InfoPane): void {
        this.infoPaneMap.set(objectID, infoPane);
    }

    /**
     * Removes the info pane associated with the object ID.
     * @param objectID The ID of the object.
     * @return If the pane existed and was removed.
     */
    public removeObjectInfoPane(objectID: number): boolean {
        return this.infoPaneMap.delete(objectID);
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

        // Render the info panes.
        this.infoPaneMap.forEach(pane => pane.render(ctx));

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