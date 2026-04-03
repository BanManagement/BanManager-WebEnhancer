import { TestBot, createBot, sendCommand, disconnectRcon, sleep } from './helpers'

afterAll(async () => {
  await disconnectRcon()
})

describe('Denied Pin Placeholder', () => {
  let bannedBot: TestBot | null = null
  const BANNED_USERNAME = 'DeniedPinPlayer'
  const TEST_TIMEOUT_MS = 60000

  const expectDeniedConnection = async (): Promise<string> => {
    let lastError: Error | null = null

    // Ban/tempban enforcement can be async across worker threads; retry denied connect checks.
    for (let attempt = 1; attempt <= 3; attempt++) {
      try {
        bannedBot = await createBot(BANNED_USERNAME)
        await bannedBot.disconnect()
        bannedBot = null
        lastError = new Error(`Attempt ${attempt}: player connected while expected to be denied`)
      } catch (error: unknown) {
        const errorMessage = error instanceof Error ? error.message : String(error)
        if (errorMessage.includes('was kicked')) {
          return errorMessage
        }
        lastError = error instanceof Error ? error : new Error(String(error))
      }

      await sleep(1000)
    }

    throw lastError ?? new Error('Expected denied connection but did not receive a denial kick')
  }

  afterEach(async () => {
    try {
      await sendCommand(`bmunban ${BANNED_USERNAME}`)
    } catch (e) {}
    await bannedBot?.disconnect()
    bannedBot = null
  })

  test('[pin] placeholder in ban message is replaced with actual pin', async () => {
    const banResponse = await sendCommand(`bmban ${BANNED_USERNAME} Testing pin placeholder`)
    console.log(`Ban response: ${banResponse}`)
    await sleep(2000)

    const errorMessage = await expectDeniedConnection()
    console.log(`Player was denied as expected: ${errorMessage}`)

    expect(errorMessage).not.toContain('[pin]')

    const pinMatch = errorMessage.match(/pin is (\d{6})/)
    expect(pinMatch).not.toBeNull()

    if (pinMatch != null) {
      const pin = pinMatch[1]
      console.log(`Extracted pin from ban message: ${pin}`)
      expect(pin).toMatch(/^\d{6}$/)
    }
  }, TEST_TIMEOUT_MS)

  test('[pin] placeholder in tempban message is replaced with actual pin', async () => {
    const tempbanResponse = await sendCommand(`bmtempban ${BANNED_USERNAME} 1h Testing tempban pin placeholder`)
    console.log(`Tempban response: ${tempbanResponse}`)
    await sleep(2000)

    const errorMessage = await expectDeniedConnection()
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
  }, TEST_TIMEOUT_MS)
})

describe('Online Kick Pin Placeholder', () => {
  const KICK_USERNAME = 'OnlineKickPin'
  const TEST_TIMEOUT_MS = 60000
  let bot: TestBot | null = null

  afterEach(async () => {
    try {
      await sendCommand(`bmunban ${KICK_USERNAME}`)
    } catch (e) {}
    await bot?.disconnect()
    bot = null
  })

  test('[pin] placeholder in kick message is replaced when banning an online player', async () => {
    bot = await createBot(KICK_USERNAME)
    const kickPromise = bot.waitForKick()

    await sleep(1000)
    const banResponse = await sendCommand(`bmban ${KICK_USERNAME} Testing online kick pin`)
    console.log(`Ban response: ${banResponse}`)

    const kickReason = await kickPromise
    console.log(`Online player kicked with reason: ${kickReason}`)
    bot = null

    expect(kickReason).not.toContain('[pin]')

    const pinMatch = kickReason.match(/pin is (\d{6})/)
    expect(pinMatch).not.toBeNull()

    if (pinMatch != null) {
      const pin = pinMatch[1]
      console.log(`Extracted pin from online kick message: ${pin}`)
      expect(pin).toMatch(/^\d{6}$/)
    }
  }, TEST_TIMEOUT_MS)

  test('[pin] placeholder in kick message is replaced when tempbanning an online player', async () => {
    bot = await createBot(KICK_USERNAME)
    const kickPromise = bot.waitForKick()

    await sleep(1000)
    const tempbanResponse = await sendCommand(`bmtempban ${KICK_USERNAME} 1h Testing online tempban kick pin`)
    console.log(`Tempban response: ${tempbanResponse}`)

    const kickReason = await kickPromise
    console.log(`Online player kicked (tempban) with reason: ${kickReason}`)
    bot = null

    expect(kickReason).not.toContain('[pin]')

    const pinMatch = kickReason.match(/pin is (\d{6})/)
    expect(pinMatch).not.toBeNull()

    if (pinMatch != null) {
      const pin = pinMatch[1]
      console.log(`Extracted pin from online tempban kick message: ${pin}`)
      expect(pin).toMatch(/^\d{6}$/)
    }
  }, TEST_TIMEOUT_MS)
})
