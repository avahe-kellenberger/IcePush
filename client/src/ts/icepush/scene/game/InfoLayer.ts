import {Layer} from "../../../engine/game/Layer";
import {Vector2D} from "../../../engine/math/Vector2D";
import {Renderable} from "../../../engine/game/ui/Renderable";
import {ILocatable, Locatable} from "../../../engine/game/entity/Locatable";
import {CanvasUtils} from "../../../engine/util/CanvasUtils";
import {Updatable} from "../../../engine/game/entity/Updatable";

export class InfoPane implements ILocatable, Locatable, Updatable, Renderable {

    public static readonly DEFAULT_FONT_COLOR = '#FF0000';
    public static readonly LOCAL_PLAYER_FONT_COLOR = '#db32db';
    private static readonly defaultFont = '14px Arial';

    private renderedInfo: string;
    private fontColor: string;
    private location: Vector2D;

    /**
     * @param renderedInfo The information to render.
     * @param fontColor The font color used to render the info.
     * @param location The location to render the info.
     */
    constructor(renderedInfo: string, fontColor: string, location: Vector2D = Vector2D.ZERO) {
        this.renderedInfo = renderedInfo;
        this.fontColor = fontColor;
        this.location = location;
    }

    /**
     * @return The current font color used for rendering information.
     */
    public getFontColor(): string {
        return this.fontColor;
    }

    /**
     * @param fontColor The font color used for rendering information.
     * @return If the color was changed.
     */
    public setFontColor(fontColor: string): boolean {
        if (fontColor === this.fontColor) {
            return false;
        }
        this.fontColor = fontColor;
        return true;
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
     * @return The information to render.
     */
    public getRenderingInfo(): string {
        return this.renderedInfo;
    }

    /**
     * Sets the info to be rendered.
     * @param renderedInfo The info to set.
     */
    public setRenderingInfo(renderedInfo: string): void {
        this.renderedInfo = renderedInfo;
    }

    /**
     * @override
     */
    public update(delta: number): void {
        // No implementation currently needed.
    }

    /**
     * @override
     */
    public render(ctx: CanvasRenderingContext2D): void {
        ctx.font = InfoPane.defaultFont;
        ctx.fillStyle = this.fontColor;
        CanvasUtils.fillTextMultiline(ctx, this.renderedInfo, this.location.x, this.location.y, 14);
    }

}

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
     * Sets if the round is counting down to be started,
     * which is used for rendering the round time remaining property.
     *
     * @param countingDown If the round is counting down.
     */
    public setIsRoundCountingDown(countingDown: boolean): void {
        this.isRoundCountingDown = countingDown;
    }

    /**
     * @override
     */
    public getObject(id: number): InfoPane|undefined {
        return super.getObject(id) as InfoPane;
    }

    /**
     * @override
     */
    public setObject(id: number, object: InfoPane): boolean {
        return super.setObject(id, object);
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