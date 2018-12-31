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
            this.playerName = bufferOrVersion.readStringOld();
        } else if (playerName !== undefined) {
            this.clientVersion = bufferOrVersion;
            this.playerName = playerName;
        } else {
            throw new Error(`Malformed constructor:\n${bufferOrVersion}\n${playerName}`);
        }
        // TODO: Special case with the old server.
        // This should be changed to (1 + Buffer.getWriteSize(this.playerName)) when the code is updated.
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

        // TODO: This is a special case with the old server.
        // We should only have to use buffer.writeString(this.playerName)
        buffer.writeInt8(this.playerName.length);
        for (let i = 0; i < this.playerName.length; i++) {
            buffer.writeInt8(this.playerName.charCodeAt(i));
        }
    }

}