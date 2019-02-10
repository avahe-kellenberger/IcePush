import {Player} from "./Player";

export class LocalPlayer extends Player {

    /**
     * Creates a new player.
     * @param uid The player's unique ID number.
     * @param name The player's name.
     * @param type The player's type.
     * @param lives The initial number of lives the player has.
     */
    constructor(uid: number, name: string, type: Player.Type, lives: number) {
        super(uid, name, type, lives);
        this.setFontColor(Player.localFontColor);
    }


}