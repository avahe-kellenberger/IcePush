import {GameObject} from "../../engine/game/entity/GameObject";
import {Vector2D} from "../../engine/math/Vector2D";
import {ClientAssets} from "../asset/ClientAssets";

export class Player extends GameObject {

    private readonly name: string;
    private readonly sprite: HTMLImageElement;
    private readonly type: Player.Type;

    private lives: number;

    /**
     * Creates a new player.
     * @param name The player's name.
     * @param type The player's type.
     * @param lives The initial number of lives the player has.
     */
    constructor(name: string, type: Player.Type, lives: number) {
        super();
        this.name = name;
        this.type = type;
        this.lives = lives;
        this.sprite = Player.getImage(type);
    }

    /**
     * @return The name of the player.
     */
    public getName(): string {
        return this.name;
    }

    /**
     * @return The number of times the player has died.
     */
    public getLives(): number {
        return this.lives;
    }

    /**
     * @param lives The number of lives the player should have.
     */
    public setLives(lives: number): void {
        this.lives = lives;
    }

    /**
     * @return The player's `PlayerType`.
     */
    public getType(): Player.Type {
        return this.type;
    }

    /**
     * @override
     */
    public render(ctx: CanvasRenderingContext2D): void {
        const center: Vector2D = this.getLocation();
        const top: number = center.y - this.sprite.height * 0.5;
        ctx.drawImage(this.sprite, center.x - this.sprite.width * 0.5, top);

        ctx.fillStyle = 'red';
        const fontSize: number = 14;
        /*
         * See https://developer.mozilla.org/en-US/docs/Web/CSS/line-height#Values for an explanation.
         * 'Desktop browsers (including Firefox) use a default value of roughly 1.2, depending on the element's font-family.'
         * We multiply by '0.6' to halve the height.
         */
        const fontHeight = fontSize * 0.6;
        ctx.font = `${fontSize}px Arial`;

        // Render Name
        const nameFontMetrics: TextMetrics = ctx.measureText(this.name);
        ctx.fillText(this.name, center.x - nameFontMetrics.width * 0.5, top - fontHeight * 2);


        // Render Lives
        const lives: string = `Lives: ${this.lives}`;
        const livesFontMetrics: TextMetrics = ctx.measureText(lives);
        ctx.fillText(lives, center.x - livesFontMetrics.width * 0.5, top - fontHeight + 4);
    }

}

export namespace Player {

    /**
     * Player types.
     */
    export enum Type {
        TREE = 0,
        SNOWMAN = 1
    }

    /**
     * @param type The player's type.
     * @return The image related to the `Player.Type`
     */
    export function getImage(type: Player.Type): HTMLImageElement {
        switch (type) {
            case Player.Type.SNOWMAN:
                return ClientAssets.IMAGE_SNOWMAN;
            case Player.Type.TREE:
                return ClientAssets.IMAGE_TREE;
            default:
                throw new Error('Illegal player type.');
        }
    }

}
