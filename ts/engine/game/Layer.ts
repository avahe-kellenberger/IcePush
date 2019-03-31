import {Updatable} from "./entity/Updatable";
import {Renderable} from "./ui/Renderable";
import {ZOrder} from "./entity/ZOrder";
import {Entity} from "./entity/Entity";

/**
 *
 */
export abstract class Layer implements ZOrder, Updatable, Renderable {

    private readonly objects: Map<number, Entity>;
    private zOrder: number;

    /**
     * Creates a new `Scene` that manages objects which are `Updatable & Renderable`.
     */
    constructor(zOrder: number = 0) {
        this.objects = new Map();
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
     * @param id The object's ID.
     * @return The object associated with the ID, or `undefined` if it does not exist.
     */
    public getObject(id: number): Entity|undefined {
        return this.objects.get(id);
    }

    /**
     * Checks if the scene contains the object.
     * @param id The object's ID.
     * @return If the scene contains the object.
     */
    public containsObject(id: number): boolean {
        return this.objects.get(id) !== undefined;
    }

    /**
     * Adds an object to the game.
     * @param id The object's ID.
     * @param object The object to add.
     * @return If the object was added successfully.
     */
    public setObject(id: number, object: Entity): boolean {
        return this.objects.size !== this.objects.set(id, object).size;
    }

    /**
     * Removes an object from the game.
     * @return If the object was removed successfully.
     * @param id The ID of the object.
     */
    public removeObject(id: number): boolean {
        return this.objects.delete(id);
    }

    /**
     * Removes all objects from the scene.
     */
    public removeAllEntities(): void {
        this.objects.clear();
    }

    /**
     * Invokes a callback on each object in the scene.
     * @param callback The callback to invoke.
     */
    public forEachEntity(callback: (e?: Entity) => void): void {
        this.objects.forEach(callback);
    }

    /**
     * @param callback The callback to invoke on each object in the scene.
     * @param callback:e The current object being processed.
     * @param callback:id The object's ID.
     * @param callback:thisArg The value to use as `this` when invoking the callback.
     * @return If the callback function returns a true for any object in the scene.
     */
    public someEntity(callback: (e: Entity, id?: number, thisArg?: this) => boolean): boolean {
        const objectIterator: IterableIterator<[number, Entity]> = this.objects.entries();
        let e: IteratorResult<[number, Entity]>;
        while ((e = objectIterator.next()) && !e.done) {
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
        this.objects.forEach(e => e.update(delta));
    }

    /**
     * @override
     */
    public render(ctx: CanvasRenderingContext2D): void {
        this.objects.forEach(e => e.render(ctx));
    }

}
