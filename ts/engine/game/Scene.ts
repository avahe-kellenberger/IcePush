import {Entity} from './entity/Entity'
import {Game} from './Game'
import {EventHandler, KeyHandler} from '../input/InputHandler'
import {Layer} from './Layer'

/**
 * Represents a Scene being displayed in a game.
 */
export class Scene implements Entity {

    protected readonly game: Game;
    private readonly layers: Array<Layer>;
    private layersAreSorted: boolean;

    private keyHandlers: Set<KeyHandler>|undefined;
    private eventHandlers: Set<EventHandler>|undefined;

    /**
     * Creates a new `Scene` which manages `Entity` objects.
     */
    constructor(game: Game) {
      this.game = game
      this.layers = []
      this.layersAreSorted = false
    }

    /**
     * Adds a KeyHandler.
     * This handler is automatically removed when the Game's `Scene` changes, unless `persist` is set to `true`.
     *
     * @param handler The callback invoked when a key event occurs.
     * @param persist If the handler should not be removed automatically when the `Scene` changes.
     * @return If the handler was added.
     */
    public addKeyHandler(handler: KeyHandler, persist: boolean = false): boolean {
      if (!persist) {
        if (this.keyHandlers === undefined) {
          this.keyHandlers = new Set()
        }
        this.keyHandlers.add(handler)
      }
      return this.game.inputHandler.addKeyHandler(handler)
    }

    /**
     * @param handler The handler to remove.
     * @return If the handler was removed.
     */
    public removeKeyHandler(handler: KeyHandler): boolean {
      if (this.keyHandlers !== undefined) {
        this.keyHandlers.delete(handler)
      }
      return this.game.inputHandler.removeKeyHandler(handler)
    }

    /**
     * Adds an EventHandler.
     * This handler is automatically removed when the Game's `Scene` changes, unless `persist` is set to `true`.
     *
     * @param handler The callback invoked when an event occurs.
     * @param persist If the handler should not be removed automatically when the `Scene` changes.
     */
    public addEventHandler(handler: EventHandler, persist: boolean = false): void {
      if (!persist) {
        if (this.eventHandlers === undefined) {
          this.eventHandlers = new Set()
        }
        this.eventHandlers.add(handler)
      }
      this.game.inputHandler.addEventHandler(handler)
    }

    /**
     * @param handler The handler to remove.
     * @return If the handler was removed.
     */
    public removeEventHandler(handler: EventHandler): void {
      if (this.eventHandlers !== undefined) {
        this.eventHandlers.delete(handler)
      }
      this.game.inputHandler.removeEventHandler(handler)
    }

    /**
     * Invoked when the `Game`'s current `Scene` is set as `this` scene.
     */
    public onSwitchedToCurrent(): void {}

    /**
     * Invoked when the `Game`'s current `Scene` is switched from `this` scene to another.
     */
    public onSwitchedFromCurrent(): void {
      if (this.keyHandlers !== undefined) {
        this.keyHandlers.forEach(handler => {
          this.game.inputHandler.removeKeyHandler(handler)
        })
        this.keyHandlers.clear()
      }

      if (this.eventHandlers !== undefined) {
        this.eventHandlers.forEach(handler => {
          this.game.inputHandler.removeEventHandler(handler)
        })
      }
    }

    /**
     * @return The game to which this Scene belongs.
     */
    public getGame(): Game {
      return this.game
    }

    /**
     * Adds a `Layer` to the scene.
     * @param layer The layer to add.
     * @return If the layer was added successfully.
     */
    public addLayer(layer: Layer): boolean {
      const added: boolean = this.layers.length !== this.layers.push(layer)
      if (added) {
        this.layersAreSorted = false
      }
      return added
    }

    /**
     * Checks if the scene contains the layer.
     * @param layer The layer to check.
     * @return If the scene contains the layer.
     */
    public containsLayer(layer: Layer): boolean {
      return this.layers.indexOf(layer) >= 0
    }

    /**
     * Removes a `Layer` from the scene.
     * @return If the layer was removed successfully.
     * @param layer The layer to remove.
     */
    public removeLayer(layer: Layer): boolean {
      const layerIndex: number = this.layers.indexOf(layer)
      if (layerIndex < 0) {
        return false
      }
      this.layers.splice(layerIndex, 1)
      return true
    }

    /**
     * Removes all layers from the scene.
     */
    public removeLayers(): void {
      this.layers.length = 0
    }

    /**
     * Sorts the scene's layers according to their z-orders.
     * @return The scene's underlying layers, after sorting.
     */
    protected sortLayers(): Array<Layer> {
      if (!this.layersAreSorted) {
        this.layers.sort((layerA, layerB) => layerA.getZOrder() - layerB.getZOrder())
        this.layersAreSorted = true
      }
      return this.layers
    }

    /**
     * @override
     */
    public update(delta: number): void {
      this.layers.forEach(e => e.update(delta))
    }

    /**
     * @override
     */
    public render(ctx: CanvasRenderingContext2D): void {
      this.sortLayers().forEach(e => e.render(ctx))
    }

}
