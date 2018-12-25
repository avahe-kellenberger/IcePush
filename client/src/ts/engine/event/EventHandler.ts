/**
 * Handles events fired from the DOM.
 */
export interface EventHandler {
    /**
     * The event type.
     */
    readonly type: keyof GlobalEventHandlersEventMap;

    /**
     * The function which handles the event.
     */
    readonly listener: EventListener;
}