import {GameObject} from "../../engine/game/entity/GameObject";
import {Vector2D} from "../../engine/math/Vector2D";
import {Assets} from "../asset/Assets";
import {CanvasUtils} from "../../engine/util/CanvasUtils";

export class Player extends GameObject {

    private readonly name: string;
    private nameCanvas: HTMLCanvasElement|undefined;

    private readonly sprite: HTMLCanvasElement;
    private readonly type: Player.Type;

    private lives: number;
    private livesCanvas: HTMLCanvasElement|undefined;

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
        this.livesCanvas = undefined;
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

        if (this.nameCanvas === undefined) {
            this.nameCanvas = CanvasUtils.stringToCanvas(this.name, '14px Arial', 'red');
        }
        ctx.drawImage(this.nameCanvas, center.x - this.nameCanvas.width * 0.5, top - this.nameCanvas.height * 2);


        if (this.livesCanvas === undefined) {
            this.livesCanvas = CanvasUtils.stringToCanvas(`Lives: ${this.lives}`, '14px Arial', 'red');
        }
        ctx.drawImage(this.livesCanvas, center.x - this.livesCanvas.width * 0.5, top - this.livesCanvas.height);
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
     * @return The canvased imaged related to the `Player.Type`
     */
    export function getImage(type: Player.Type): HTMLCanvasElement {
        switch (type) {
            case Player.Type.SNOWMAN:
                return Assets.IMAGE_SNOWMAN;
            case Player.Type.TREE:
                return Assets.IMAGE_TREE;
            default:
                throw new Error('Illegal player type.');
        }
    }

}
