/**
 * @author Avahe
 * Handles events fired from the DOM.
 */
import {Vector2D} from "../math/Vector2D";

export class EventHandler {

    public readonly type: keyof GlobalEventHandlersEventMap;
    public readonly listener: EventListener;

    /**
     * @param type The event type.
     * @param listener The function which handles the event.
     */
    constructor(type: keyof GlobalEventHandlersEventMap, listener: EventListener) {
        this.type = type;
        this.listener = listener;
    }

}

/**
 * @author Avahe
 */
export class KeyHandler {

    public readonly callback: (key: string, isDown: boolean) => void;
    public readonly filter: ((key: string, isDown: boolean) => boolean)|undefined;
    public readonly notifyAll: boolean;

    /**
     * @param callback A callback invoked indicating if the state of the key is `down`.
     *                 This callback is invoked on `keydown`, `keyup`, and `blur` events.
     * @param filter The key filter. If this returns true or is undefined, the callback will be notified.
     * @param notifyAll If the handler should be notified of all `keyDown` events.
     *                  If false, the handler will only be notified when the state changes (down to up, or up to down).
     */
    constructor(callback: (key: string, isDown: boolean) => void, filter?: ((key: string, isDown: boolean) => boolean), notifyAll: boolean = false) {
        this.callback = callback;
        this.filter = filter;
        this.notifyAll = notifyAll;
    }

}

/**
 * @author Avahe
 */
export class InputHandler {

    private readonly document: Document;
    private readonly keyMap: Map<string, boolean>;
    private readonly keyHandlers: Set<KeyHandler>;

    /**
     * Attaches the handler to the given document.
     * @param document The document on which to listen for input events.
     */
    constructor(document: HTMLDocument) {
        this.document = document;
        this.keyMap = new Map();
        this.keyHandlers = new Set();

        /*
         * Attach `keydown` and `keyup` handlers to the canvas.
         */
        this.document.addEventListener('keydown', e => {
            const changed: boolean = !this.isKeyDown(e.key);
            this.keyMap.set(e.key, true);
            this.notifyKeyHandlers(e.key, true, changed);
        });
        this.document.addEventListener('keyup', e => {
            const changed: boolean = this.isKeyDown(e.key);
            this.keyMap.set(e.key, false);
            this.notifyKeyHandlers(e.key, false, changed);
        });

        /*
         * If the canvas loses focus, it is the same as no keys being down on the game canvas.
         */
        this.document.addEventListener('blur', () => {
            this.keyMap.forEach((isDown, key) => {
               this.keyMap.set(key, false);
            });

            // TODO: Focus listeners.
        });
    }

    // region EventHandlers

    /**
     * Adds an `EventHandler` to the DOM.
     * @param handler The event handler.
     */
    public addEventHandler(handler: EventHandler): void {
        this.document.addEventListener(handler.type, handler.listener);
    }

    /**
     * Removes an `EventHandler` from the DOM.
     * @param handler The event handler.
     */
    public removeEventHandler(handler: EventHandler): void {
        this.document.removeEventListener(handler.type, handler.listener);
    }

    // endregion

    // region KeyHandlers

    /**
     * Notifies all KeyHandlers of key events.
     * @param key The key of which to notify the handlers.
     * @param isDown If the state of the key is currently `down`.
     * @param changed If the state of the key changed.
     */
    private notifyKeyHandlers(key: string, isDown: boolean, changed: boolean): void {
        this.keyHandlers.forEach(handler => {
            if (handler.filter === undefined || handler.filter(key, isDown)) {
                if (handler.notifyAll || changed) {
                    handler.callback(key, isDown);
                }
            }
        });
    }

    /**
     * Checks if the key is currently down.
     * @param key The key to check.
     */
    public isKeyDown(key: string): boolean {
        const isDown: boolean|undefined = this.keyMap.get(key);
        return isDown !== undefined && isDown;
    }

    /**
     * @param handler The callback invoked when a key event occurs.
     * @return If the handler was added.
     */
    public addKeyHandler(handler: KeyHandler): boolean {
        return this.keyHandlers.size !== this.keyHandlers.add(handler).size;
    }

    /**
     * @param handler The callback invoked when a key event occurs.
     * @return If the handler was removed.
     */
    public removeKeyHandler(handler: KeyHandler): boolean {
        return this.keyHandlers === undefined || this.keyHandlers.delete(handler);
    }

    // endregion

    // region Static Methods

    /**
     * Translates the location of the `MouseEvent` to be relative to the `canvas`' bounding client rect.
     * @param event The `MouseEvent` from which to derive the translated location.
     * @param canvas The canvas to which the location is translated.
     * @see HTMLCanvasElement.getBoundingClientRect
     */
    public static translateMouseEventLocationToCanvas(event: MouseEvent, canvas: HTMLCanvasElement): Vector2D {
        const boundingRect: ClientRect|DOMRect = canvas.getBoundingClientRect();
        return new Vector2D(event.clientX - boundingRect.left, event.clientY - boundingRect.top);
    }

    // endregion

}
