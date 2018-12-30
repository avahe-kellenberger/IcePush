import {NetworkEvent} from "./NetworkEvent";

export class EventQueue {

    private readonly events: NetworkEvent[];

    constructor() {
        this.events = [];
    }

    /**
     * Enqueues a NetworkEvent.
     * @param event The event to enqueue.
     */
    public enqueue(event: NetworkEvent): void {
        this.events.push(event);
    }

}
