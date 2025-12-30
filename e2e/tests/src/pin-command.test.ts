import { TestBot, createBot, disconnectRcon, opPlayer, sleep, clearPlayerPins } from './helpers'

describe('Pin Command', () => {
  let playerBot: TestBot

  afterAll(async () => {
    await playerBot?.disconnect()
    await disconnectRcon()
  })

  test('bmpin command returns a 6-digit pin', async () => {
    // Use a unique player for this test
    playerBot = await createBot('PinCmdPlayer1')
    await sleep(2000)
    await opPlayer('PinCmdPlayer1')
    await sleep(500)

    playerBot.clearSystemMessages()
    await playerBot.sendChat('/bmpin')

    const response = await playerBot.waitForSystemMessage('expires', 10000)
    expect(response.message).toMatch(/expires/)
    console.log(`Received pin message: ${response.message}`)

    // Also verify we got a 6-digit pin
    const allMessages = playerBot.getSystemMessages()
    const pinMessage = allMessages.find(m => /^\d{6}$/.test(m.message))
    expect(pinMessage).toBeDefined()
    console.log(`Pin: ${pinMessage?.message}`)

    await playerBot.disconnect()
  }, 20000)

  test('bmpin command generates a new pin after rate limit expires', async () => {
    // Use a unique player for this test
    playerBot = await createBot('PinCmdPlayer2')
    await sleep(2000)
    await opPlayer('PinCmdPlayer2')
    await sleep(500)
    await clearPlayerPins()
    await sleep(500)

    // First pin request
    playerBot.clearSystemMessages()
    await playerBot.sendChat('/bmpin')
    await playerBot.waitForSystemMessage('expires', 10000)

    const firstMessages = playerBot.getSystemMessages()
    const firstPin = firstMessages.find(m => /^\d{6}$/.test(m.message))?.message

    expect(firstPin).toBeDefined()
    expect(firstPin).toMatch(/^\d{6}$/)
    console.log(`First pin: ${firstPin}`)

    // Wait for rate limit to expire (30 seconds)
    console.log('Waiting 31 seconds for rate limit to expire...')
    await sleep(31000)

    // Second pin request
    playerBot.clearSystemMessages()
    await playerBot.sendChat('/bmpin')
    await playerBot.waitForSystemMessage('expires', 10000)

    const secondMessages = playerBot.getSystemMessages()
    const secondPin = secondMessages.find(m => /^\d{6}$/.test(m.message))?.message

    expect(secondPin).toBeDefined()
    expect(secondPin).toMatch(/^\d{6}$/)
    console.log(`Second pin: ${secondPin}`)

    // Pins should be different (fresh each time)
    expect(secondPin).not.toBe(firstPin)
    console.log(`First pin: ${firstPin}, Second pin: ${secondPin} - different as expected`)

    await playerBot.disconnect()
  }, 70000)
})
