import {NetworkEvent} from "../NetworkEvent";
import {Player} from "../../entity/Player";
import {PositionedBuffer} from "../../../engine/net/PositionedBuffer";
import {OPCode} from "../NetworkEventBuffer";

export class NewPlayerEvent extends NetworkEvent {

    public readonly playerID: number;
    public readonly type: Player.Type;
    public readonly username: string;
    public readonly deathCount: number;
    private readonly BINARY_SIZE: number;

    /**
     *
     * @param playerID
     * @param player
     */
    constructor(playerID: number, player: Player);
    constructor(buffer: PositionedBuffer);
    constructor(bufferOrID: PositionedBuffer|number, player?: Player) {
        super();
        if (bufferOrID instanceof PositionedBuffer) {
            this.playerID = bufferOrID.readInt16BE();
            this.type = bufferOrID.readInt8();
            this.username = bufferOrID.readString();
            this.deathCount = bufferOrID.readInt16BE();
        } else if (player !== undefined) {
            this.playerID = bufferOrID;
            this.type = player.getType();
            this.username = player.getName();
            this.deathCount = player.getDeathCount();
        } else {
            throw new Error(`Malformed constructor:\n${bufferOrID}\n${player}`);
        }
        this.BINARY_SIZE = 7 + this.username.length;
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
        return OPCode.NEW_PLAYER;
    }

    /**
     * @override
     */
    public write(buffer: PositionedBuffer): void {
        buffer.writeInt16BE(this.playerID);
        buffer.writeInt8(this.type);
        buffer.writeString(this.username);
        buffer.writeInt16BE(this.deathCount);
    }

}