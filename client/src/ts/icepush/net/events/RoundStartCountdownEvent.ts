import {RoundStartedEvent} from "./RoundStartedEvent";
import {OPCode} from "../NetworkEventBuffer";

export class RoundStartCountdownEvent extends RoundStartedEvent {

    /**
     * @override
     */
    public getOPCode(): OPCode {
        return OPCode.ROUND_START_COUNTDOWN;
    }
}