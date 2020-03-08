import {Layer} from '../../../engine/game/Layer'
import {Vector2D} from '../../../engine/math/Vector2D'
import {DOMUtils} from '../../../engine/util/DOMUtils'

export class DOMLayer extends Layer {

    private readonly canvas: HTMLCanvasElement;

    private readonly btnLogout: HTMLButtonElement;
    private readonly chatBox: HTMLTextAreaElement;
    private readonly chatInputBox: HTMLInputElement;
    private readonly domElements: ReadonlySet<HTMLElement>;

    constructor(canvas: HTMLCanvasElement, zOrder: number = 1) {
      super(zOrder)
      this.canvas = canvas

      this.btnLogout = document.createElement('button')
      this.btnLogout.className = 'on-canvas unfocusable'
      this.btnLogout.innerHTML = 'Logout'
      this.btnLogout.style.top = '0%'
      this.btnLogout.style.left = '100%'
      this.btnLogout.style.transform = 'translate(-100%, 0%)'

      this.chatBox = document.createElement('textarea')
      this.chatBox.className = 'on-canvas unfocusable'
      this.chatBox.id = 'chatbox'
      this.chatBox.style.padding = '0'
      this.chatBox.style.margin = '0'
      this.chatBox.style.top = '0'
      this.chatBox.style.left = '50%'
      this.chatBox.style.width = '65%'
      this.chatBox.style.height = '25%'
      this.chatBox.style.transform = 'translate(-50%, 0%)'
      this.chatBox.style.overflow = 'hidden'
      this.chatBox.disabled = true
      this.chatBox.readOnly = true

      this.chatInputBox = document.createElement('input')
      this.chatInputBox.className = 'on-canvas unfocusable'
      this.chatInputBox.id = 'chat-input'
      this.chatInputBox.style.padding = '0'
      this.chatInputBox.style.margin = '0'
      this.chatInputBox.style.top = '25%'
      this.chatInputBox.style.left = '50%'
      this.chatInputBox.style.width = '65%'
      this.chatInputBox.style.transform = 'translate(-50%, 0%)'
      this.chatInputBox.disabled = true
      this.chatInputBox.readOnly = true

      // Ensure all elements are in this list.
      this.domElements = new Set([this.btnLogout, this.chatBox, this.chatInputBox])
    }

    /**
     * Finds DOM elements which contain the given point.
     * @param point The point to check.
     * @param filter The filter of elements to accept.
     * @return The DOM elements which contains the given point.
     */
    public findClickedDOMElements(point: Vector2D, filter?: (e: HTMLElement) => boolean): HTMLElement[] {
      const elements: HTMLElement[] = []
      const boundingRect: ClientRect|DOMRect = this.canvas.getBoundingClientRect()
      const offset: Vector2D = new Vector2D(boundingRect.left, boundingRect.top)
      for (const element of this.domElements) {
        if (DOMUtils.containsPoint(element, point, offset)) {
          if (filter && filter(element)) {
            elements.push(element)
          }
        }
      }
      return elements
    }

    /**
     * Finds non-chat related DOM elements which contain the given point.
     * @param point The point to check.
     * @return The non-chat related DOM elements which contains the given point.
     */
    public findClickedDOMElementNotChatRelated(point: Vector2D): HTMLElement[] {
      return this.findClickedDOMElements(point, e => {
        return e !== this.chatInputBox && e !== this.chatBox
      })
    }

    /**
     * Invoked the callback when the logout button is clicked.
     * @param callback The callback to invoke.
     */
    public addLogoutClickListener(callback: () => void): void {
      this.btnLogout.addEventListener('click', callback.bind(this))
    }

    /**
     * Adds a chat message to the chatbox.
     * @param message The message to add.
     */
    public addChatMessage(message: string): void {
      this.chatBox.value += `${message}\n`
      this.chatBox.scrollTop = this.chatBox.scrollHeight - this.chatBox.clientHeight
    }

    /**
     * Sets the text of the chat input box.
     * @param str The text to set.
     */
    public setChatInputText(str: string): void {
      this.chatInputBox.value = str
    }

    /**
     * Appends text to the chat input box.
     * @param str The string to append.
     */
    public appendToChatInputText(str: string): void {
      this.chatInputBox.value += str
    }

    /**
     * @return The text in the chat input box.
     */
    public getChatInputText(): string {
      return this.chatInputBox.value
    }

    /**
     * Clears the text of the chat input box.
     */
    public clearChatInputText(): void {
      this.chatInputBox.value = ''
    }

    /**
     * @return All DOM elements of the layer.
     */
    public getDOMElements(): ReadonlySet<HTMLElement> {
      return this.domElements
    }

    /**
     * @return The chat box.
     */
    public getChatbox(): HTMLTextAreaElement {
      return this.chatBox
    }

    /**
     * @return The chat input box.
     */
    public getChatInputBox(): HTMLInputElement {
      return this.chatInputBox
    }

}
