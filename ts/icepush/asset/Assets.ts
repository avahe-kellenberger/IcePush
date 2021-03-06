import {Paths} from './Paths'
import {CanvasUtils} from '../../engine/util/CanvasUtils'

export class Assets {


    public static IMAGE_BACKGROUND: HTMLCanvasElement;
    public static IMAGE_SNOWMAN: HTMLCanvasElement;
    public static IMAGE_TREE: HTMLCanvasElement;
    public static IMAGE_PRESENT: HTMLCanvasElement;

    /**
     * Loads all of the game's assets.
     */
    public static load(): Promise<void> {
      const promises: Promise<any>[] = []

      promises.push(this.loadImage(Paths.IMAGE_BACKGROUND).then(image =>
        Assets.IMAGE_BACKGROUND = CanvasUtils.imageToCanvas(image)))

      promises.push(this.loadImage(Paths.IMAGE_SNOWMAN).then(image =>
        Assets.IMAGE_SNOWMAN = CanvasUtils.imageToCanvas(image)))

      promises.push(this.loadImage(Paths.IMAGE_TREE).then(image =>
        Assets.IMAGE_TREE = CanvasUtils.imageToCanvas(image)))

      promises.push(this.loadImage(Paths.IMAGE_PRESENT).then(image =>
        Assets.IMAGE_PRESENT = CanvasUtils.imageToCanvas(image)))

      // TODO: Add all assets to be loaded, here.

      return Promise.all(promises).then<void>()
    }

    /**
     * Loads an image with the given URL.
     * @param url The URL of the image.
     */
    private static loadImage(url: string): Promise<HTMLImageElement> {
      return new Promise((resolve, reject) => {
        const image: HTMLImageElement = new Image()
        image.onload = () => {
          resolve(image)
        }
        image.onerror = (e: ErrorEvent) => {
          reject(e)
        }
        image.src = url
      })
    }

    /**
     * Retrieves the image associated with the ID.
     * TODO: Map these IDs to the images.
     * @param id The ID of the image.
     */
    public static getImageByID(id: number): HTMLCanvasElement {
      switch (id) {
        case 0:
          return this.IMAGE_TREE
        case 1:
          return this.IMAGE_SNOWMAN
        case 2:
          return this.IMAGE_PRESENT
      }
      return Assets.IMAGE_BACKGROUND
    }

}
