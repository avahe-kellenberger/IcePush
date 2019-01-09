import {NetworkEvent} from "../NetworkEvent";
import {OPCode} from "../OPCode";
import {PositionedBuffer} from "../PositionedBuffer";

export class LoginEvent extends NetworkEvent {

    public readonly clientVersion: number;
    public readonly playerName: string;

    private readonly BINARY_SIZE: number;

    /**
     * @param clientVersion The version of the running client.
     * @param playerName The player's name.
     */
    constructor(clientVersion: number, playerName: string);
    constructor(buffer: PositionedBuffer);
    constructor(bufferOrVersion: PositionedBuffer|number, playerName?: string) {
        super();
        if (bufferOrVersion instanceof PositionedBuffer) {
            this.clientVersion = bufferOrVersion.readInt8();
            const length: number = bufferOrVersion.readInt8();
            this.playerName = bufferOrVersion.readString(length);
        } else if (playerName !== undefined) {
            this.clientVersion = bufferOrVersion;
            this.playerName = playerName;
        } else {
            throw new Error(`Malformed constructor:\n${bufferOrVersion}\n${playerName}`);
        }
        this.BINARY_SIZE = 2 + this.playerName.length;
    }

    /**
     * @override
     */
    public getEventSize(): number {
        return this.BINARY_SIZE;
    }

    /**
     * @override
     */
    public getOPCode(): OPCode {
        return OPCode.LOGIN;
    }

    /**
     * @override
     */
    public write(buffer: PositionedBuffer): void {
        buffer.writeInt8(this.clientVersion);
        buffer.writeInt8(this.playerName.length);
        for (let i = 0; i < this.playerName.length; i++) {
            buffer.writeInt8(this.playerName.charCodeAt(i));
        }
    }

}