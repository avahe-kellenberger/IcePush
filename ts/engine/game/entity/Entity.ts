import {Updatable} from "./Updatable";
import {Renderable} from "../ui/Renderable";

/**
 * @author Prestige
 *
 * Represents any in-game object that will be updated and rendered.
 */
export interface Entity extends Updatable, Renderable {}