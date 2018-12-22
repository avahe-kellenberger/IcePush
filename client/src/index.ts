import {IcePush} from "./ts/icepush/IcePush";
import {ClientAssets} from "./ts/icepush/asset/ClientAssets";

console.log('[IcePush] Loading canvas...');

const canvas: HTMLCanvasElement = document!.getElementById('canvas') as HTMLCanvasElement;
const ctx: CanvasRenderingContext2D|null = canvas.getContext('2d');

if (ctx == null) {
    throw Error('Failed to load canvas rendering context!');
}

const assetPromise: Promise<void> = ClientAssets.load();
assetPromise.then(() => {
    const icepush: IcePush = new IcePush(ctx);
    icepush.start();
    console.log('[IcePush] Game Started.');
});
