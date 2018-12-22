export interface Updatable {

    /**
     * Updates the object.
     * @param delta The amount of time in seconds elapsed since the last update.
     */
    update(delta: number): void;

}