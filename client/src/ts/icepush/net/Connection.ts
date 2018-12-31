import {NetworkEvent} from "./NetworkEvent";
import {PositionedBuffer} from "./PositionedBuffer";

export class Connection {

    private readonly socket: WebSocket;

    /**
     * Connects via a websocket to the given address with the given protocol(s).
     * @param address See `WebSocket`
     * @param protocols See `WebSocket`
     */
    constructor(address: string, protocols?: string|string[]) {
        this.socket = new WebSocket(address, protocols);
        this.socket.binaryType = 'arraybuffer';

        this.socket.addEventListener("message", (e: MessageEvent) => {
            if (!(e.data instanceof ArrayBuffer)) {
                throw new Error("Unsupported WebSocket data type: " + typeof e.data);
            }
            this.onReceived(e.data);
        });
        this.socket.addEventListener('error', this.onError);
        this.socket.addEventListener("close", this.onClose);
        this.socket.onopen
    }

    /**
     * Adds a listener to be invoked when the underlying WebSocket first opens.
     * @param onopen See `WebSocket`
     */
    public addOnOpenListener(onopen: ((ev: Event) => any)): void {
        this.socket.onopen = onopen;
    }

    /**
     * TODO:
     * @param buffer
     */
    private onReceived(buffer: ArrayBuffer): void {
        // TODO: Interpret the event into NetworkEvents.
        // const decoder: TextDecoder = new TextDecoder();
        // decoder.decode(buffer) => Find NetworkEvent type and parse.
    }

    /**
     *
     * @param e The CloseEvent triggered when the socket closed.
     */
    private onClose(e: CloseEvent): void {
        // TODO: Cleanup game/notify listeners
        console.log(`Connection closed with code ${e.code}\nReason: ${e.reason}`);
    }

    /**
     * @param e The ErrorEvent fired by the socket.
     */
    private onError(e: ErrorEvent): void {
        // TODO: Cleanup.
        console.error(`Connection Error: ${e}`);
    }

    /**
     * Sends an individual event.
     * @param event The event to send.
     */
    public send(event: NetworkEvent): void {
        const size: number = 1 + event.getEventSize();
        const buffer: PositionedBuffer = new PositionedBuffer(new Buffer(size));
        buffer.writeInt8(event.getOPCode());
        event.write(buffer);
        this.socket.send(buffer.getBuffer());
    }

    /**
     * @return If the socket is connected.
     */
    public isConnected(): boolean {
        return this.socket.readyState == WebSocket.OPEN;
    }

    /**
     * Closes the connection with the given error code.
     * If the code is not given, it will default to 1000 (unknown reason).
     * @param errorOrCode
     */
    public close(errorOrCode?: boolean|number): void {
        if (errorOrCode == null) {
            errorOrCode = 1000;
        } else if (typeof errorOrCode === 'boolean') {
            errorOrCode = errorOrCode ? 4000 : 1000;
        }
        this.socket.close(errorOrCode);
    }

}