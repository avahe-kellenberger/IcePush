import {GameObject} from "../../engine/game/entity/GameObject";
import {Vector2D} from "../../engine/math/Vector2D";
import {Assets} from "../asset/Assets";

/**
 * A listener which is invoked when there is a change to the number of lives of the player.
 */
type LivesListener  = (livesRemaining: number) => void;

export class Player extends GameObject {

    private readonly name: string;
    private readonly sprite: HTMLCanvasElement;
    private readonly type: Player.Type;
    private readonly livesListeners: Set<LivesListener>;
    private lives: number;

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
     * @return The player's sprite.
     */
    public getSprite(): HTMLCanvasElement {
        return this.sprite;
    }

    /**
     * @override
     */
    public render(ctx: CanvasRenderingContext2D): void {
        const center: Vector2D = this.getLocation();
        const top: number = center.y - this.sprite.height * 0.5;
        ctx.drawImage(this.sprite, center.x - this.sprite.width * 0.5, top);
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
