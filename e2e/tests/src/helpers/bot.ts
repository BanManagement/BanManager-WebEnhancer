import mineflayer, { Bot } from 'mineflayer'

const SERVER_HOST = process.env.SERVER_HOST ?? 'localhost'
const SERVER_PORT = parseInt(process.env.SERVER_PORT ?? '25565', 10)
// Specify version for proxy compatibility (avoids version mismatch errors)
const MC_VERSION = process.env.MC_VERSION ?? undefined

/**
 * Extract text from a kick reason (string on Bukkit, ChatMessage object on Fabric)
 */
function extractKickReason (reason: unknown): string {
  if (typeof reason === 'string') {
    return reason
  }
  if (reason != null && typeof reason === 'object') {
    const reasonObj = reason as Record<string, unknown>
    if (typeof reasonObj.toString === 'function') {
      const result = reasonObj.toString()
      if (result !== '[object Object]') {
        return result
      }
    }
    if (typeof reasonObj.toMotd === 'function') {
      return reasonObj.toMotd() as string
    }
    if ('text' in reasonObj && typeof reasonObj.text === 'string') {
      return reasonObj.text
    }
    if ('extra' in reasonObj && Array.isArray(reasonObj.extra)) {
      return reasonObj.extra.map((e: unknown) => {
        if (typeof e === 'object' && e != null && 'text' in e) {
          return (e as Record<string, unknown>).text ?? ''
        }
        return ''
      }).join('')
    }
    return JSON.stringify(reason)
  }
  return String(reason)
}

export interface ChatMessage {
  username: string
  message: string
  timestamp: number
}

export interface SystemMessage {
  message: string
  timestamp: number
}

export class TestBot {
  private bot: Bot | null = null
  private chatMessages: ChatMessage[] = []
  private systemMessages: SystemMessage[] = []
  private readonly _username: string

  constructor (username: string) {
    this._username = username
  }

  get username (): string {
    return this._username
  }

  async connect (): Promise<void> {
    return await new Promise((resolve, reject) => {
      console.log(`Connecting bot ${this._username} to ${SERVER_HOST}:${SERVER_PORT}`)

      this.bot = mineflayer.createBot({
        host: SERVER_HOST,
        port: SERVER_PORT,
        username: this._username,
        auth: 'offline',
        hideErrors: false,
        version: MC_VERSION
      })

      const timeout = setTimeout(() => {
        reject(new Error('Bot connection timeout'))
      }, 30000)

      this.bot.once('spawn', () => {
        clearTimeout(timeout)
        console.log(`Bot ${this._username} spawned successfully`)
        resolve()
      })

      this.bot.once('error', (err) => {
        clearTimeout(timeout)
        reject(err)
      })

      this.bot.once('kicked', (reason) => {
        clearTimeout(timeout)
        const reasonText = extractKickReason(reason)
        console.log(`Bot ${this._username} was kicked: ${reasonText}`)
        reject(new Error(`Bot ${this._username} was kicked: ${reasonText}`))
      })

      this.bot.on('chat', (username, message) => {
        this.chatMessages.push({
          username,
          message,
          timestamp: Date.now()
        })
        console.log(`Chat: <${username}> ${message}`)
      })

      this.bot.on('message', (jsonMsg) => {
        const text = jsonMsg.toString()
        if (text.trim() !== '') {
          this.systemMessages.push({
            message: text,
            timestamp: Date.now()
          })
          console.log(`[${this._username}] System: ${text}`)
        }
      })
    })
  }

  async disconnect (): Promise<void> {
    if (this.bot != null) {
      const bot = this.bot
      this.bot = null

      await new Promise<void>((resolve) => {
        const timeout = setTimeout(() => resolve(), 2000)

        bot.once('end', () => {
          clearTimeout(timeout)
          resolve()
        })

        bot.removeAllListeners('chat')
        bot.removeAllListeners('message')
        bot.removeAllListeners('error')
        bot.quit()
      })

      await this.sleep(200)
      console.log(`Bot ${this._username} disconnected`)
    }
  }

  async sendChat (message: string): Promise<void> {
    if (this.bot == null) {
      throw new Error('Bot not connected')
    }
    console.log(`Bot ${this._username} sending: ${message}`)
    this.bot.chat(message)
  }

  clearChatHistory (): void {
    this.chatMessages = []
  }

  getChatMessages (): ChatMessage[] {
    return [...this.chatMessages]
  }

  clearSystemMessages (): void {
    this.systemMessages = []
  }

  getSystemMessages (): SystemMessage[] {
    return [...this.systemMessages]
  }

  /**
   * Wait for a system message containing specific text
   */
  async waitForSystemMessage (
    containsText: string,
    timeoutMs: number = 5000
  ): Promise<SystemMessage> {
    const startTime = Date.now()
    const startIndex = this.systemMessages.length

    return await new Promise((resolve, reject) => {
      const checkInterval = setInterval(() => {
        for (let i = startIndex; i < this.systemMessages.length; i++) {
          const msg = this.systemMessages[i]
          if (msg.message.includes(containsText)) {
            clearInterval(checkInterval)
            resolve(msg)
            return
          }
        }

        if (Date.now() - startTime > timeoutMs) {
          clearInterval(checkInterval)
          reject(new Error(`Timeout waiting for system message containing "${containsText}"`))
        }
      }, 100)
    })
  }

  /**
   * Assert that no system message containing the text is received within a timeout period
   * Useful for testing denied notification exemptions
   */
  async expectNoSystemMessage (containsText: string, timeoutMs: number = 3000): Promise<void> {
    const startIndex = this.systemMessages.length

    await this.sleep(timeoutMs)

    for (let i = startIndex; i < this.systemMessages.length; i++) {
      const msg = this.systemMessages[i]
      if (msg.message.includes(containsText)) {
        throw new Error(`Unexpected system message containing "${containsText}": "${msg.message}"`)
      }
    }
  }

  /**
   * Wait for a chat message from a specific user containing specific text
   */
  async waitForChat (
    fromUsername: string,
    containsText: string,
    timeoutMs: number = 5000
  ): Promise<ChatMessage> {
    const startTime = Date.now()
    const startIndex = this.chatMessages.length

    return await new Promise((resolve, reject) => {
      const checkInterval = setInterval(() => {
        for (let i = startIndex; i < this.chatMessages.length; i++) {
          const msg = this.chatMessages[i]
          if (msg.username === fromUsername && msg.message.includes(containsText)) {
            clearInterval(checkInterval)
            resolve(msg)
            return
          }
        }

        if (Date.now() - startTime > timeoutMs) {
          clearInterval(checkInterval)
          reject(new Error(`Timeout waiting for chat from ${fromUsername} containing "${containsText}"`))
        }
      }, 100)
    })
  }

  /**
   * Wait for any chat message from a specific user
   */
  async waitForAnyChat (fromUsername: string, timeoutMs: number = 5000): Promise<ChatMessage> {
    const startTime = Date.now()
    const startIndex = this.chatMessages.length

    return await new Promise((resolve, reject) => {
      const checkInterval = setInterval(() => {
        for (let i = startIndex; i < this.chatMessages.length; i++) {
          const msg = this.chatMessages[i]
          if (msg.username === fromUsername) {
            clearInterval(checkInterval)
            resolve(msg)
            return
          }
        }

        if (Date.now() - startTime > timeoutMs) {
          clearInterval(checkInterval)
          reject(new Error(`Timeout waiting for chat from ${fromUsername}`))
        }
      }, 100)
    })
  }

  /**
   * Assert that no chat is received within a timeout period
   * Useful for testing mutes - expecting the chat to be blocked
   */
  async expectNoChatFrom (fromUsername: string, timeoutMs: number = 3000): Promise<void> {
    const startIndex = this.chatMessages.length

    await this.sleep(timeoutMs)

    for (let i = startIndex; i < this.chatMessages.length; i++) {
      const msg = this.chatMessages[i]
      if (msg.username === fromUsername) {
        throw new Error(`Unexpected chat from ${fromUsername}: "${msg.message}"`)
      }
    }
  }

  private async sleep (ms: number): Promise<void> {
    return await new Promise((resolve) => setTimeout(resolve, ms))
  }

  isConnected (): boolean {
    return this.bot !== null
  }
}

export async function createBot (username: string): Promise<TestBot> {
  const bot = new TestBot(username)
  await bot.connect()
  return bot
}
