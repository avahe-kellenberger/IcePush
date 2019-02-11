import {Updatable} from "./entity/Updatable";
import {Renderable} from "./ui/Renderable";
import {ZOrder} from "./entity/ZOrder";
import {Entity} from "./entity/Entity";

/**
 *
 */
export abstract class Layer implements ZOrder, Updatable, Renderable {

    private readonly entities: Map<number, Entity>;
    private zOrder: number;

    /**
     * Creates a new `Scene` which manages `Entity` objects.
     */
    constructor(zOrder: number = 0) {
        this.entities = new Map();
        this.zOrder = zOrder;
    }

    // region ZOrder

    /**
     * @override
     */
    public getZOrder(): number {
        return this.zOrder;
    }

    /**
     * @override
     */
    public setZOrder(zOrder: number): void {
        this.zOrder = zOrder;
    }

    // endregion

    // region Entity

    /**
     * Adds an `Entity` to the game.
     * @param id The entity's ID.
     * @param entity The Entity to add.
     * @return If the entity was added successfully.
     */
    public addEntity(id: number, entity: Entity): boolean {
        return this.entities.size !== this.entities.set(id, entity).size;
    }

    /**
     * Checks if the scene contains the entity.
     * @param id The entity's ID.
     * @return If the scene contains the entity.
     */
    public containsEntity(id: number): boolean {
        return this.entities.get(id) !== undefined;
    }

    /**
     * @param id The entity's ID.
     * @return The entity associated with the ID, or `undefined` if it does not exist.
     */
    public getEntity(id: number): Entity|undefined {
        return this.entities.get(id);
    }

    /**
     * Removes an `Entity` from the game.
     * @return If the entity was removed successfully.
     * @param id The ID of the entity.
     */
    public removeEntity(id: number): boolean {
        return this.entities.delete(id);
    }

    /**
     * Removes all entities from the scene.
     */
    public removeAllEntities(): void {
        this.entities.clear();
    }

    /**
     * Invokes a callback on each `Entity` in the scene.
     * @param callback The callback to invoke.
     */
    public forEachEntity(callback: (e?: Entity) => void): void {
        this.entities.forEach(callback);
    }

    /**
     * @param callback The callback to invoke on each `Entity` in the `Scene.`
     * @param callback:e The current `Entity` being processed.
     * @param callback:id The entity's ID.
     * @param callback:thisArg The value to use as `this` when invoking the callback.
     * @return If the callback function returns a true for any `Entity` in the `Scene`.
     */
    public someEntity(callback: (e: Entity, id?: number, thisArg?: this) => boolean): boolean {
        const entityIterator: IterableIterator<[number, Entity]> = this.entities.entries();
        let e: IteratorResult<[number, Entity]>;
        while ((e = entityIterator.next()) && !e.done) {
            if (callback(e.value[1], e.value[0])) {
                return true;
            }
        }
        return false;
    }

    // endregion

    /**
     * @override
     */
    public update(delta: number): void {
        this.entities.forEach(e => e.update(delta));
    }

    /**
     * @override
     */
    public render(ctx: CanvasRenderingContext2D): void {
        this.entities.forEach(e => e.render(ctx));
    }

}