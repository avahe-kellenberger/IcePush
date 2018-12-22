import {IcePush} from "./ts/icepush/IcePush";

console.log('[IcePush] Loading canvas...');

const canvas: HTMLCanvasElement = document!.getElementById('canvas') as HTMLCanvasElement;
const ctx: CanvasRenderingContext2D|null = canvas.getContext('2d');

if (ctx == null) {
    throw Error('Failed to load canvas rendering context!');
}

const icepush: IcePush = new IcePush(ctx);
icepush.start();
console.log('[IcePush] Game Started.');