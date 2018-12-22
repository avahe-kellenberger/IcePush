import {Game} from "../engine/game/Game";

export class IcePush extends Game {

    /**
     *
     */
    constructor(ctx: CanvasRenderingContext2D) {
        super(ctx);
        console.log('IcePush game loaded.');
    }

}


// TODO:

/**
 /*const canvas: HTMLCanvasElement = document!.getElementById('canvas') as HTMLCanvasElement;
 const ctx: CanvasRenderingContext2D|null = canvas.getContext('2d');
 if (ctx === null) {
    throw Error('Failed to get context from canvas!');
}*/