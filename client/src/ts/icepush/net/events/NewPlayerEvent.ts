import {NetworkEvent} from "../NetworkEvent";
import {OPCode} from "../OPCode";
import {Player} from "../../entity/Player";
import {PositionedBuffer} from "../PositionedBuffer";

export class NewPlayerEvent extends NetworkEvent {

    public readonly player: Player;
    private readonly BINARY_SIZE: number;

    /**
     *
     * @param player
     */
    constructor(player: Player);
    constructor(buffer: PositionedBuffer);
    constructor(playerOrBuffer: PositionedBuffer|Player) {
        super();
        if (playerOrBuffer instanceof Player) {
            this.player = playerOrBuffer;
        } else {
            const playerID: number = playerOrBuffer.readInt16BE();
            const type: number = playerOrBuffer.readInt8();
            const username: string = playerOrBuffer.readStringOld();
            this.player = new Player(playerID, username, type);

            const deaths: number = playerOrBuffer.readInt16BE();
            this.player.setDeathCount(deaths);
        }
        // Initial size of 5 bytes + write size of the player's name.
        this.BINARY_SIZE = 5 + PositionedBuffer.getWriteSizeOld(this.player.getName());
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
        buffer.writeInt16BE(this.player.getID());
        buffer.writeInt8(this.player.getType());
        buffer.writeStringOld(this.player.getName());
        buffer.writeInt16BE(this.player.getDeathCount());
    }

}