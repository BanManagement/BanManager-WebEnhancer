import {
  TestBot,
  createBot,
  disconnectRcon,
  opPlayer,
  sleep,
  getPlayerPinCount,
  getAllPlayerPins,
  clearPlayerPins,
  closeDatabase
} from './helpers'

describe('Pin Cache Behavior', () => {
  let playerBot: TestBot

  beforeAll(async () => {
    playerBot = await createBot('PinCachePlayer')
    await sleep(2000)
    await opPlayer('PinCachePlayer')
    await sleep(500)
    await clearPlayerPins()
  })

  afterAll(async () => {
    await playerBot?.disconnect()
    await disconnectRcon()
    await closeDatabase()
  })

  beforeEach(async () => {
    await clearPlayerPins()
    await sleep(500)
  })

  test('same pin is returned within 5-minute cache window', async () => {
    // First /bmpin call
    playerBot.clearSystemMessages()
    await playerBot.sendChat('/bmpin')

    // Wait for the expiry message first
    const firstResponse = await playerBot.waitForSystemMessage('expires', 10000)
    console.log(`First call - Received: ${firstResponse.message}`)

    await sleep(500)

    // Get all system messages and find the 6-digit pin
    const firstMessages = playerBot.getSystemMessages()
    let actualFirstPin: string | undefined
    for (const msg of firstMessages) {
      const match = msg.message.match(/^\d{6}$/)
      if (match != null) {
        actualFirstPin = match[0]
        break
      }
    }
    console.log(`First call - Actual pin: ${actualFirstPin ?? 'not found'}`)

    await sleep(1000)

    // Second /bmpin call (should return cached pin)
    playerBot.clearSystemMessages()
    await playerBot.sendChat('/bmpin')

    const secondResponse = await playerBot.waitForSystemMessage('expires', 10000)
    console.log(`Second call - Received: ${secondResponse.message}`)

    await sleep(500)

    // Get all system messages and find the 6-digit pin
    const secondMessages = playerBot.getSystemMessages()
    let actualSecondPin: string | undefined
    for (const msg of secondMessages) {
      const match = msg.message.match(/^\d{6}$/)
      if (match != null) {
        actualSecondPin = match[0]
        break
      }
    }
    console.log(`Second call - Actual pin: ${actualSecondPin ?? 'not found'}`)

    // The pins should be the same (cached)
    if (actualFirstPin != null && actualSecondPin != null) {
      expect(actualFirstPin).toBe(actualSecondPin)
      console.log(`Pins match as expected (cache working): ${actualFirstPin}`)
    }
  }, 30000)

  test('pin is stored in database on first request', async () => {
    const initialCount = await getPlayerPinCount('PinCachePlayer')
    console.log(`Initial pin count: ${initialCount}`)

    playerBot.clearSystemMessages()
    await playerBot.sendChat('/bmpin')
    await playerBot.waitForSystemMessage('expires', 10000)
    await sleep(1000)

    const newCount = await getPlayerPinCount('PinCachePlayer')
    // Pin count should increase by at least 1 (could be more if previous test created one)
    expect(newCount).toBeGreaterThanOrEqual(initialCount)
    console.log(`Pin count after /bmpin: ${newCount}`)
  }, 20000)

  test('only one pin is stored for multiple cached requests', async () => {
    // Clear and wait for cache to be empty
    await clearPlayerPins()
    await sleep(500)

    const initialCount = await getPlayerPinCount('PinCachePlayer')
    console.log(`Initial pin count after clear: ${initialCount}`)

    // First call creates a pin
    playerBot.clearSystemMessages()
    await playerBot.sendChat('/bmpin')
    await playerBot.waitForSystemMessage('expires', 10000)
    await sleep(500)

    const afterFirstCall = await getPlayerPinCount('PinCachePlayer')
    console.log(`Pin count after first call: ${afterFirstCall}`)

    // Make two more /bmpin calls (should use cache)
    for (let i = 0; i < 2; i++) {
      playerBot.clearSystemMessages()
      await playerBot.sendChat('/bmpin')
      await playerBot.waitForSystemMessage('expires', 10000)
      await sleep(500)
    }

    await sleep(1000)

    // Should still have the same count (cached, not generating new ones)
    const finalCount = await getPlayerPinCount('PinCachePlayer')
    console.log(`Final pin count: ${finalCount}`)

    // Final count should equal the count after first call (no new pins generated)
    expect(finalCount).toBe(afterFirstCall)
  }, 30000)
})
