import {RoundStartEvent} from "./RoundStartEvent";
import {OPCode} from "../OPCode";

export class RoundStartCountdownEvent extends RoundStartEvent {

    /**
     * @override
     */
    public getOPCode(): OPCode {
        return OPCode.ROUND_START_COUNTDOWN;
    }
}