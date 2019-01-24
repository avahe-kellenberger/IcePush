import {Assets} from "./icepush/asset/Assets";
import {IcePush} from "./icepush/IcePush";
import {CanvasUtils} from "./engine/util/CanvasUtils";

// Queue asset loading as soon as possible.
console.log('[IcePush] Loading assets...');
const assetPromise: Promise<void> = Assets.load();

// Load the rendering context from the DOM's canvas.
console.log('[IcePush] Loading canvas...');
const canvas: HTMLCanvasElement = document!.getElementById('canvas') as HTMLCanvasElement;
const ctx: CanvasRenderingContext2D|null = CanvasUtils.getContext(canvas);

// Assets loaded successfully.
assetPromise.then(() => {
    console.log('[IcePush] Assets loaded.');
    const icepush: IcePush = new IcePush(ctx);
    icepush.showHomeScene();
    icepush.start();
    console.log('[IcePush] Game Started.');
});

// Assets failed to load.
assetPromise.catch(reason => {
    console.error(`Failed to load assets!\n${reason}`);
});