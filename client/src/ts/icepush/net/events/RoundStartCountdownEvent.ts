import {RoundStartEvent} from "./RoundStartEvent";
import {OPCode} from "../NetworkEventBuffer";

export class RoundStartCountdownEvent extends RoundStartEvent {

    /**
     * @override
     */
    public getOPCode(): OPCode {
        return OPCode.ROUND_START_COUNTDOWN;
    }
}