/**
 * @author Avahe
 */
export class KeyListener {

    public readonly callback: (isDown: boolean) => void;
    public readonly notifyAll: boolean;

    /**
     * @param callback A callback invoked indicating if the state of the key is `down`.
     *                 This callback is invoked on `keydown`, `keyup`, and `blur` events.
     *                 In the case of a `blur` event, all keys are notified as "not down".
     * @param notifyAll If the listener should be notified of all `keyDown` events.
     *                  If false, the listener will only be notified when the state changes (down to up, or up to down).
     */
    constructor(callback: (isDown: boolean) => void, notifyAll: boolean = false) {
        this.notifyAll = notifyAll;
        this.callback = callback;
    }

}

/**
 * @author Avahe
 */
export class InputHandler {

    /**
     * Map<key, isDown>
     */
    private readonly keyMap: Map<string, boolean>;
    private readonly listenerMap: Map<string, Set<KeyListener>>;

    /**
     * Attaches the listener to the given document.
     * @param document The document on which to listen for input events.
     */
    constructor(document: HTMLDocument) {
        this.keyMap = new Map();
        this.listenerMap = new Map();

        /*
         * Attach `keydown` and `keyup` listeners to the canvas.
         */
        document.addEventListener('keydown', e => {
            const changed: boolean = this.isKeyDown(e.key) !== true;
            this.keyMap.set(e.key, true);
            this.notifyListeners(e.key, true, changed);
        });
        document.addEventListener('keyup', e => {
            const changed: boolean = this.isKeyDown(e.key) !== false;
            this.keyMap.set(e.key, false);
            this.notifyListeners(e.key, false, changed);
        });

        /*
         * If the canvas loses focus, it is the same as no keys being down on the game canvas.
         * We notify all listeners that the keys are "not down", to prevent strange in-game behavior.
         */
        document.addEventListener('blur', () => {
            this.keyMap.forEach((isDown, key) => {
               this.keyMap.set(key, false);
            });
            this.listenerMap.forEach((listeners: Set<KeyListener>) => {
                listeners.forEach(listener => listener.callback(false));
            });
        });
    }

    /**
     * Notifies all KeyStateListeners that the state of a key has changed.
     * @param key The key of which to notify the listeners.
     * @param isDown If the state of the key is currently `down`.
     * @param changed If the state of the key changed.
     */
    private notifyListeners(key: string, isDown: boolean, changed: boolean): void {
        const listeners: Set<KeyListener>|undefined = this.listenerMap.get(key);
        if (listeners !== undefined) {
            listeners.forEach(listener => {
                if (listener.notifyAll || changed) {
                    listener.callback(isDown);
                }
            });
        }
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
     * Adds a KeyListener.
     * @param key The key of which to listen for state changes.
     * @param listener The callback invoked when the state of the key changes.
     * @return If the listener was added.
     */
    public addKeyListener(key: string, listener: KeyListener): boolean {
        let listeners: Set<KeyListener>|undefined = this.listenerMap.get(key);
        if (listeners === undefined) {
            listeners = new Set();
            listeners.add(listener);
            this.listenerMap.set(key, listeners);
            return true;
        }
        return listeners.size !== listeners.add(listener).size;
    }

    /**
     * Removes a KeyListener.
     * @param key The key of which to listen for state changes.
     * @param listener The callback invoked when a key event occurs.
     * @return If the listener was removed.
     */
    public removeKeyListener(key: string, listener: KeyListener): boolean {
        const listeners: Set<KeyListener>|undefined = this.listenerMap.get(key);
        return listeners === undefined || listeners.delete(listener);
    }

}
