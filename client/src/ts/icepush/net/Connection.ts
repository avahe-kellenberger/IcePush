import {NetworkEvent} from "./NetworkEvent";
import {PositionedBuffer} from "./PositionedBuffer";

export class Connection {

    private readonly socket: WebSocket;

    private readonly dataListeners: Array<(buffer: ArrayBuffer) => void>;

    /**
     * Connects via a websocket to the given address with the given protocol(s).
     * @param address See `WebSocket`
     * @param protocols See `WebSocket`
     */
    constructor(address: string, protocols?: string|string[]) {
        this.socket = new WebSocket(address, protocols);
        this.dataListeners = [];

        this.socket.binaryType = 'arraybuffer';
        this.addMessageListener((e: MessageEvent) => {
            if (!(e.data instanceof ArrayBuffer)) {
                throw new Error("Unsupported WebSocket data type: " + typeof e.data);
            }
            this.onReceived(e.data);
        });
        this.addErrorListener(this.onError);
        this.addCloseListener(this.onClose);
    }

    /**
     * Adds a listener to be invoked when the underlying WebSocket first opens.
     * @param listener The listener to be invoked.
     */
    public addOnOpenedListener(listener: ((e: Event) => any)): void {
        this.socket.addEventListener('open', listener);
    }

    /**
     * Removes the listener.
     * @param listener The listener to remove.
     */
    public removeOnOpenedListener(listener: (e: Event) => any): void {
        this.socket.removeEventListener('open', listener);
    }

    /**
     * Adds a listener to be invoked when the underlying WebSocket closes.
     * @param listener The listener to be invoked.
     */
    public addCloseListener(listener: ((e: CloseEvent) => any)): void {
        this.socket.addEventListener('close', listener);
    }

    /**
     * Removes the listener.
     * @param listener The listener to remove.
     */
    public removeCloseListener(listener: (e: CloseEvent) => any): void {
        this.socket.removeEventListener('close', listener);
    }

    /**
     * Adds a listener to be invoked when the underlying WebSocket receives a message.
     * @param listener The listener to be invoked.
     */
    public addMessageListener(listener: ((e: MessageEvent) => any)) {
        this.socket.addEventListener('message', listener);
    }

    /**
     * Removes the listener.
     * @param listener The listener to remove.
     */
    public removeMessageListener(listener: (e: MessageEvent) => any): void {
        this.socket.removeEventListener('message', listener);
    }

    /**
     * Adds a listener to be invoked when the underlying WebSocket throws an error.
     * @param listener The listener to be invoked.
     */
    public addErrorListener(listener: ((e: ErrorEvent) => any)): void {
        this.socket.addEventListener('error', listener);
    }

    /**
     * Removes the listener.
     * @param listener The listener to remove.
     */
    public removeErrorListener(listener: (e: ErrorEvent) => any): void {
        this.socket.removeEventListener('error', listener);
    }

    /**
     * Adds a listener to be invoked when the underlying WebSocket receives data.
     * @param listener The listener to be invoked.
     */
    public addDataReceivedListener(listener: ((buffer: ArrayBuffer) => any)): void {
        this.dataListeners.push(listener);
    }

    /**
     * Removes the listener.
     * @param listener The listener to remove.
     */
    public removeDataReceivedListener(listener: ((buffer: ArrayBuffer) => any)): void {
        const index: number = this.dataListeners.indexOf(listener);
        if (index >= 0) {
            this.dataListeners.splice(index, 1);
        }
    }

    /**
     * Notifies the listeners of incoming data.
     * @param buffer The data received by the socket.
     */
    private onReceived(buffer: ArrayBuffer): void {
        this.dataListeners.forEach(listener => listener(buffer));
    }

    /**
     *
     * @param e The CloseEvent triggered when the socket closed.
     */
    private onClose(e: CloseEvent): void {
        // TODO: Cleanup game/notify listeners
        console.log(`Connection closed with code ${e.code}`);
        if (e.reason.length > 0) {
            console.log(`Reason: ${e.reason}`);
        }
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
     * @return The underlying WebSocket's `readyState`.
     */
    public getState(): number {
        return this.socket.readyState;
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
