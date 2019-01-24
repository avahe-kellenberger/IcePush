import {Paths} from "./Paths";
import {CanvasUtils} from "../../engine/util/CanvasUtils";

export class Assets {

    // region Images

    public static IMAGE_BACKGROUND: HTMLCanvasElement;
    public static IMAGE_SNOWMAN: HTMLCanvasElement;
    public static IMAGE_TREE: HTMLCanvasElement;

    // endregion

    /**
     * Loads all of the game's assets.
     */
    public static load(): Promise<void> {
        const promises: Promise<any>[] = [];

        // region Images

        promises.push(this.loadImage(Paths.IMAGE_BACKGROUND).then(image =>
            Assets.IMAGE_BACKGROUND = CanvasUtils.imageToCanvas(image)));
        promises.push(this.loadImage(Paths.IMAGE_SNOWMAN).then(image =>
            Assets.IMAGE_SNOWMAN = CanvasUtils.imageToCanvas(image)));
        promises.push(this.loadImage(Paths.IMAGE_TREE).then(image =>
            Assets.IMAGE_TREE = CanvasUtils.imageToCanvas(image)));

        // endregion

        // TODO: Add all assets to be loaded, here.

        return Promise.all(promises).then<void>();
    }

    /**
     * Loads an image with the given URL.
     * @param url The URL of the image.
     */
    private static loadImage(url: string): Promise<HTMLImageElement> {
        return new Promise((resolve, reject) => {
            const image: HTMLImageElement = new Image();
            image.onload = () => {
                resolve(image);
            };
            image.onerror = (e: ErrorEvent) => {
                reject(e);
            };
            image.src = url;
        });
    }

}