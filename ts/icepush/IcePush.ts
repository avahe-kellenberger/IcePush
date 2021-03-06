import {Game} from '../engine/game/Game'
import {HomeScene} from './scene/HomeScene'
import {GameScene} from './scene/game/GameScene'
import {Connection} from './net/Connection'
import {LogoutEvent} from './net/events/LogoutEvent'
import {LoginEvent} from './net/events/LoginEvent'
import {NetworkEventBuffer} from './net/NetworkEventBuffer'
import {FailureEvent} from './net/events/FailureEvent'
import {SuccessEvent} from './net/events/SuccessEvent'
import {NetworkEvent} from './net/NetworkEvent'

export class IcePush extends Game {

    public static readonly CLIENT_VERSION: number = 110;
    private static readonly LOGIN_TIMEOUT: number = 3000;

    public static readonly SERVER_ADDRESS: string = 'ws://icepush.threesided.net:2345';

    private connection: Connection|undefined;
    private homeScene: HomeScene|undefined;
    private playerID: number|undefined;

    /**
     * Attempts to log in.
     * @param username The username to log in with.
     */
    public tryLogin(username: string): void {
      // Already connected or connecting; do nothing.
      if (this.isConnectionOpenOrConnecting()) {
        return
      }

      this.connection = new Connection(IcePush.SERVER_ADDRESS)
      const connection: Connection = this.connection

      // Await for a message indicating the login succeeded.
      const loginListener = ((buffer: NetworkEventBuffer): void => {
        clearTimeout(timeoutID)
        connection.removeErrorListener(errorListener)

        const events: NetworkEvent[] = buffer.getEvents()
        const event: SuccessEvent|undefined = events.find(e => e instanceof SuccessEvent) as SuccessEvent
        if (event !== undefined) {
          connection.removeDataReceivedListener(loginListener)
          this.onLoginSucceeded(event.playerID)
        } else {
          // Login unsuccessful; close connection and display error.
          connection.close()
          const failureEvent: FailureEvent|undefined = events.find(e => e instanceof FailureEvent) as FailureEvent
          alert(failureEvent !== undefined && failureEvent.message !== undefined ?
            failureEvent.message : 'An unknown error has occurred.')
        }
      })

      // Wait for the connection to time out.
      const timeoutID: NodeJS.Timeout = setTimeout(() => {
        alert('Connection timed out.')
      }, IcePush.LOGIN_TIMEOUT
      )
      this.connection.addDataReceivedListener(loginListener)

      // If the connection closes, cancel enqueued timeouts.
      this.connection.addCloseListener(() => {
        clearTimeout(timeoutID)
        // Go back to the login screen if the connection closes.
        if (this.currentScene instanceof GameScene) {
          this.showHomeScene()
        }
      })

      // Send a login event when the connect first opens.
      this.connection.addOnOpenedListener(() =>
        connection.send(new LoginEvent(IcePush.CLIENT_VERSION, username)))

      // If an error is thrown, the login failed.
      const errorListener = ((): void => {
        connection.removeErrorListener(errorListener)
        alert('Connection error!')
      })
      this.connection.addErrorListener(errorListener)
    }

    /**
     * Checks if the connection's readyState is OPEN or CONNECTING.
     */
    private isConnectionOpenOrConnecting(): boolean {
      if (this.connection !== undefined) {
        const readyState: number = this.connection.getState()
        return readyState === WebSocket.OPEN || readyState === WebSocket.CONNECTING
      }
      return false
    }

    /**
     * Logs out of the game and closes the connection.
     */
    public logout(): void {
      if (this.connection !== undefined) {
        this.connection.send(new LogoutEvent())
        this.showHomeScene()
        this.connection.close()
        this.connection = undefined
      }
    }

    /**
     * Invoked when the login is successful.
     * @param playerID The local player's ID number.
     */
    private onLoginSucceeded(playerID: number): void {
      this.playerID = playerID
      this.showGameScene()
    }

    /**
     * @return The game's connection to the server.
     */
    public getConnection(): Connection|undefined {
      return this.connection
    }

    /**
     * Shows the `HomeScene`.
     * @return If the scene was switched.
     */
    public showHomeScene(): boolean {
      if (this.homeScene === undefined) {
        this.homeScene = new HomeScene(this)
      } else if (this.currentScene === this.homeScene) {
        return false
      }
      this.setScene(this.homeScene)
      return true
    }

    /**
     * Shows the `GameScene`.
     * @return If switching to the game scene was successful.
     */
    public showGameScene(): boolean {
      if (this.playerID === undefined || this.currentScene instanceof GameScene) {
        return false
      }
      this.setScene(new GameScene(this, this.playerID))
      return true
    }

    /**
     * @return The DOM element containing the game's canvas.
     */
    public static getDOMContainer(): HTMLElement {
      const container: HTMLElement|null = document.getElementById('canvas-container')
      if (container === null) {
        throw new Error('Failed to get DOM element')
      }
      return container
    }

    /**
     * @override
     */
    public update(delta: number): void {
      super.update(delta)
      if (this.connection !== undefined && this.connection.isConnected()) {
        // Send all events that have been enqueued this update.
        this.connection.flushEventQueue()
      }
    }

}
