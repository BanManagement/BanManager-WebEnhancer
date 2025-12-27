import { TestBot, createBot, sendCommand, disconnectRcon, sleep } from './helpers'

describe('Denied Pin Placeholder', () => {
  let bannedBot: TestBot | null = null
  const BANNED_USERNAME = 'DeniedPinPlayer'

  afterEach(async () => {
    try {
      await sendCommand(`unban ${BANNED_USERNAME}`)
    } catch (e) {}
    await bannedBot?.disconnect()
    bannedBot = null
  })

  afterAll(async () => {
    await disconnectRcon()
  })

  test('[pin] placeholder in ban message is replaced with actual pin', async () => {
    const banResponse = await sendCommand(`ban ${BANNED_USERNAME} Testing pin placeholder`)
    console.log(`Ban response: ${banResponse}`)
    await sleep(1000)

    try {
      bannedBot = await createBot(BANNED_USERNAME)
      throw new Error('Player was able to connect, ban did not work')
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : String(error)
      console.log(`Player was denied as expected: ${errorMessage}`)

      expect(errorMessage).not.toContain('[pin]')

      const pinMatch = errorMessage.match(/pin is (\d{6})/)
      expect(pinMatch).not.toBeNull()

      if (pinMatch != null) {
        const pin = pinMatch[1]
        console.log(`Extracted pin from ban message: ${pin}`)
        expect(pin).toMatch(/^\d{6}$/)
      }
    }
  }, 30000)

  test('[pin] placeholder in tempban message is replaced with actual pin', async () => {
    const tempbanResponse = await sendCommand(`tempban ${BANNED_USERNAME} 1h Testing tempban pin placeholder`)
    console.log(`Tempban response: ${tempbanResponse}`)
    await sleep(1000)

    try {
      bannedBot = await createBot(BANNED_USERNAME)
      throw new Error('Player was able to connect, tempban did not work')
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : String(error)
      console.log(`Player was denied (tempban) as expected: ${errorMessage}`)

      expect(errorMessage).not.toContain('[pin]')

      // The tempban message format is: "Your appeal pin is [pin]" - match any 6-digit number
      const pinMatch = errorMessage.match(/(\d{6})/)
      expect(pinMatch).not.toBeNull()

      if (pinMatch != null) {
        const pin = pinMatch[1]
        console.log(`Extracted pin from tempban message: ${pin}`)
        expect(pin).toMatch(/^\d{6}$/)
      }
    }
  }, 30000)
})
