/**
 * Handles events fired from the DOM.
 */
export class EventHandler {

    public readonly type: keyof GlobalEventHandlersEventMap;
    public readonly listener: EventListener;

    /**
     *
     * @param type The event type.
     * @param listener The function which handles the event.
     */
    constructor(type: keyof GlobalEventHandlersEventMap, listener: EventListener) {
        this.type = type;
        this.listener = listener;
    }

}