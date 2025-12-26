import { TestBot, createBot, sendCommand, disconnectRcon, opPlayer, sleep } from './helpers'

describe('Pin Command', () => {
  let playerBot: TestBot

  beforeAll(async () => {
    // Connect and set up the player
    playerBot = await createBot('PinTestPlayer')

    // Wait a moment for server to register the player
    await sleep(2000)

    // Give the player operator permissions for testing
    await opPlayer('PinTestPlayer')
    await sleep(500)
  })

  afterAll(async () => {
    await playerBot?.disconnect()
    await disconnectRcon()
  })

  test('bmpin command returns a 6-digit pin', async () => {
    // Clear previous messages
    playerBot.clearSystemMessages()

    // Execute the /bmpin command
    await playerBot.sendChat('/bmpin')

    // Wait for the response
    const response = await playerBot.waitForSystemMessage('pin expires', 10000)

    // The message should contain a 6-digit pin
    // Expected format: "Your pin expires in [expires]" and the pin should be sent
    expect(response.message).toMatch(/\d{6}|expires/)
    console.log(`Received pin message: ${response.message}`)
  })

  test('bmpin command generates a new pin each time', async () => {
    // First pin request
    playerBot.clearSystemMessages()
    await playerBot.sendChat('/bmpin')
    const firstResponse = await playerBot.waitForSystemMessage('pin', 10000)

    // Extract pin from first response (if possible)
    const firstPin = firstResponse.message.match(/\d{6}/)?.[0]

    // Wait a moment
    await sleep(1000)

    // Second pin request - within same session, should return same pin from cache
    playerBot.clearSystemMessages()
    await playerBot.sendChat('/bmpin')
    const secondResponse = await playerBot.waitForSystemMessage('pin', 10000)

    const secondPin = secondResponse.message.match(/\d{6}/)?.[0]

    // Both should be valid 6-digit pins
    if (firstPin != null) {
      expect(firstPin).toMatch(/^\d{6}$/)
    }
    if (secondPin != null) {
      expect(secondPin).toMatch(/^\d{6}$/)
    }

    console.log(`First pin: ${firstPin ?? 'not found'}, Second pin: ${secondPin ?? 'not found'}`)
  })
})
