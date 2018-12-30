export class Connection {

    private readonly socket: WebSocket;

    /**
     * TODO:
     * @param address
     */
    constructor(address: string) {
        this.socket = new WebSocket(address);
        this.socket.binaryType = 'arraybuffer';

        this.socket.addEventListener("message", (e: MessageEvent) => {
            if (!(e instanceof Buffer)) {
                throw new Error("Unsupported WebSocket data type: " + typeof e);
            }
            this.onReceived(e);
        });
        this.socket.addEventListener('error', this.onError);
        this.socket.addEventListener("close", this.onClose);
    }

    /**
     * TODO:
     * @param buffer
     */
    private onReceived(buffer: Buffer): void {
        // TODO: Interpret the event into NetworkEvents.
        console.log(buffer);
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
        console.error("Connection Error: " + e);
    }

    /**
     * Sends the array.
     * @param arr The array to send.
     */
    public send(arr: Uint8Array): void {
        this.socket.send(new Uint8Array());
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
        } else if (typeof errorOrCode === "boolean") {
            errorOrCode = errorOrCode ? 4000 : 1000;
        }
        this.socket.close(errorOrCode);
    }

}