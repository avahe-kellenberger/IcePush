import {ClientAssets} from "./ts/icepush/asset/ClientAssets";
import {Game} from "./ts/engine/game/Game";
import {HomeScene} from "./ts/icepush/scene/HomeScene";

// Queue asset loading as soon as possible.
console.log('[IcePush] Loading assets...');
const assetPromise: Promise<void> = ClientAssets.load();

// Load the rendering context from the DOM's canvas.
console.log('[IcePush] Loading canvas...');
const canvas: HTMLCanvasElement = document!.getElementById('canvas') as HTMLCanvasElement;
const ctx: CanvasRenderingContext2D|null = canvas.getContext('2d');
if (ctx == null) {
    throw Error('Failed to load canvas rendering context!');
}

// Assets loaded successfully.
assetPromise.then(() => {
    console.log('[IcePush] Assets loaded.');
    const icepush: Game = new Game(ctx);
    icepush.setScene(new HomeScene(icepush));
    icepush.start();
    console.log('[IcePush] Game Started.');
});

// Assets failed to load.
assetPromise.catch(reason => {
    console.error(`Failed to load assets!\n${reason}`);
});