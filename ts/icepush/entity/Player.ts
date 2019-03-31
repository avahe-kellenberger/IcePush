import {Sprite} from "../../engine/game/entity/Sprite";

/**
 * A listener which is invoked when there is a change to the number of lives of the player.
 */
type LivesListener  = (livesRemaining: number) => void;

export class Player extends Sprite {

    private readonly name: string;
    private readonly livesListeners: Set<LivesListener>;
    private lives: number;

    /**
     * Creates a new player.
     * @param uid The player's unique ID number.
     * @param name The player's name.
     * @param image The player's image.
     * @param lives The initial number of lives the player has.
     */
    constructor(uid: number, name: string, image: HTMLCanvasElement, lives: number) {
        super(uid, image);
        this.name = name;
        this.lives = lives;
        this.livesListeners = new Set();
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

}
