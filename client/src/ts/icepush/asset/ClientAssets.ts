import {Paths} from "./Paths";

export class ClientAssets {

    // region Images

    public static IMAGE_BACKGROUND: HTMLImageElement;
    public static IMAGE_SNOWMAN: HTMLImageElement;
    public static IMAGE_TREE: HTMLImageElement;

    // endregion

    /**
     * Loads all of the game's assets.
     */
    public static load(): Promise<void> {
        const promises: Promise<any>[] = [];

        // region Images
        promises.push(this.loadImage(Paths.IMAGE_BACKGROUND).then(image => ClientAssets.IMAGE_BACKGROUND = image));
        promises.push(this.loadImage(Paths.IMAGE_SNOWMAN).then(image => ClientAssets.IMAGE_SNOWMAN = image));
        promises.push(this.loadImage(Paths.IMAGE_TREE).then(image => ClientAssets.IMAGE_TREE = image));
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