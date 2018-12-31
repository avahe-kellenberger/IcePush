import {GameObject} from "../../engine/game/entity/GameObject";
import {Vector2D} from "../../engine/math/Vector2D";
import {ClientAssets} from "../asset/ClientAssets";

export class Player extends GameObject {

    private readonly name: string;
    private readonly sprite: HTMLImageElement;
    private readonly type: number;
    private readonly id: number;

    private deathCount: number;
    private dead: boolean;

    /**
     * Creates a new player.
     * @param id The player's ID.
     * @param name The player's name.
     * @param type The
     */
    constructor(id: number, name: string, type: Player.Type) {
        super();
        this.id = id;
        this.name = name;
        this.type = type;
        this.deathCount = 0;
        this.dead = false;

        // TODO: Clean up this player type system when the client/server protocol is reworked.
        switch (type) {
            case Player.Type.SNOWMAN:
                this.sprite = ClientAssets.IMAGE_SNOWMAN;
                break;
            case Player.Type.TREE:
                this.sprite = ClientAssets.IMAGE_TREE;
                break;
            default:
                throw new Error('Illegal player type.');
        }
    }

    /**
     * @return The name of the player.
     */
    public getName(): string {
        return this.name;
    }

    /**
     * @return If the player is currently dead.
     */
    public isDead(): boolean {
        return this.dead;
    }

    /**
     * Set if the player is currently dead.
     * @param dead If the player is dead.
     * @return If the player's death state was changed.
     */
    public setIsDead(dead: boolean): boolean {
        if (this.dead == dead) {
            return false;
        }
        this.dead = dead;
        return true;
    }

    /**
     * @return The player's ID.
     */
    public getID(): number {
        return this.id;
    }

    /**
     * @return The number of times the player has died.
     */
    public getDeathCount(): number {
        return this.deathCount;
    }

    /**
     * Set the number of times the player has died.
     * @param deaths The number of times the player died.
     */
    public setDeathCount(deaths: number): void {
        this.deathCount = deaths;
    }

    /**
     * @return The player's `Player.Type`.
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
        ctx.font = `${fontSize}px Arial`;
        const metrics: TextMetrics = ctx.measureText(this.name);

        /*
         * See https://developer.mozilla.org/en-US/docs/Web/CSS/line-height#Values for an explanation.
         * 'Desktop browsers (including Firefox) use a default value of roughly 1.2, depending on the element's font-family.'
         * We multiply by '0.6' to halve the height.
         */
        const height = fontSize * 0.6;
        ctx.fillText(this.name, center.x - metrics.width * 0.5, top - height);
    }

}

export namespace Player {

    /**
     * TODO: Check if these values are correct with the old client version.
     */
    export enum Type {
        SNOWMAN = 0,
        TREE = 1
    }

}
