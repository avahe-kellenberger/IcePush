import {GameObject} from "../../engine/game/entity/GameObject";
import {Vector2D} from "../../engine/math/Vector2D";
import {Assets} from "../asset/Assets";
import {CanvasUtils} from "../../engine/util/CanvasUtils";

/**
 *
 */
type LivesListener  = (livesRemaining: number) => void;

export class Player extends GameObject {

    protected static readonly defaultFontColor = 'red';
    protected static readonly localFontColor = '#db32db';

    private readonly name: string;
    private nameCanvas: HTMLCanvasElement|undefined;

    private readonly sprite: HTMLCanvasElement;
    private readonly type: Player.Type;

    private readonly livesListeners: Set<LivesListener>;
    private lives: number;
    private livesCanvas: HTMLCanvasElement|undefined;

    private fontColor: string;

    /**
     * Creates a new player.
     * @param uid The player's unique ID number.
     * @param name The player's name.
     * @param type The player's type.
     * @param lives The initial number of lives the player has.
     */
    constructor(uid: number, name: string, type: Player.Type, lives: number) {
        super(uid);
        this.name = name;
        this.type = type;
        this.lives = lives;
        this.livesListeners = new Set();
        this.sprite = Player.getImage(type);
        this.fontColor = Player.defaultFontColor;
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
        this.livesListeners.forEach(listener => listener(lives));
    }

    /**
     * Adds a `LivesListener` to the player.
     * @param listener The listener to add.
     * @return If the listener was added.
     */
    public addLivesListener(listener: LivesListener): boolean {
        return this.livesListeners.size !== this.livesListeners.add(listener).size;
    }

    /**
     * @param listener The listener to check.
     * @return If the player contains the lister.
     */
    public containsLivesListener(listener: LivesListener): boolean {
        return this.livesListeners.has(listener);
    }

    /**
     * Removes a `LivesListener` from the player.
     * @param listener The listener to remove.
     * @return If the listener was removed.
     */
    public removeLivesListener(listener: LivesListener): boolean {
        return this.livesListeners.delete(listener);
    }

    /**
     * @return The player's `PlayerType`.
     */
    public getType(): Player.Type {
        return this.type;
    }

    /**
     * @return The current font color used for rendering information.
     */
    public getFontColor(): string {
        return this.fontColor;
    }

    /**
     * @param color The font color used for rendering information.
     * @return If the color was changed.
     */
    public setFontColor(color: string): boolean {
        if (color === this.fontColor) {
            return false;
        }
        this.fontColor = color;
        return true;
    }

    /**
     * @override
     */
    public render(ctx: CanvasRenderingContext2D): void {
        const center: Vector2D = this.getLocation();
        const top: number = center.y - this.sprite.height * 0.5;
        ctx.drawImage(this.sprite, center.x - this.sprite.width * 0.5, top);

        if (this.nameCanvas === undefined) {
            this.nameCanvas = CanvasUtils.stringToCanvas(this.name, '14px Arial', this.fontColor);
        }
        ctx.drawImage(this.nameCanvas, center.x - this.nameCanvas.width * 0.5, top - this.nameCanvas.height * 2);


        if (this.livesCanvas === undefined) {
            this.livesCanvas = CanvasUtils.stringToCanvas(`Lives: ${this.lives}`, '14px Arial', this.fontColor);
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
