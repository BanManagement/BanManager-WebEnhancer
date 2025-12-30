import {
  TestBot,
  createBot,
  disconnectRcon,
  opPlayer,
  sleep,
  getPlayerPinCount,
  clearPlayerPins,
  closeDatabase
} from './helpers'

describe('Pin Rate Limiting Behavior', () => {
  let playerBot: TestBot

  afterAll(async () => {
    await playerBot?.disconnect()
    await disconnectRcon()
    await closeDatabase()
  })

  test('rate limit prevents rapid pin generation', async () => {
    playerBot = await createBot('RateLimitTest')
    await sleep(2000)
    await opPlayer('RateLimitTest')
    await sleep(500)

    // First /bmpin call - should succeed
    playerBot.clearSystemMessages()
    await playerBot.sendChat('/bmpin')

    // Wait for the expiry message (indicates success)
    const firstResponse = await playerBot.waitForSystemMessage('expires', 10000)
    console.log(`First call - Received: ${firstResponse.message}`)

    await sleep(500)

    // Get the first pin
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
    expect(actualFirstPin).toBeDefined()

    await sleep(1000)

    // Second /bmpin call immediately - should be rate limited
    playerBot.clearSystemMessages()
    await playerBot.sendChat('/bmpin')

    // Should receive rate limit message
    const rateLimitResponse = await playerBot.waitForSystemMessage('wait', 10000)
    console.log(`Second call - Rate limit message: ${rateLimitResponse.message}`)

    expect(rateLimitResponse.message).toContain('wait')
    expect(rateLimitResponse.message).toContain('seconds')

    await playerBot.disconnect()
  }, 30000)

  test('pin is stored in database on first request', async () => {
    // Use a unique player for this test (max 16 chars)
    playerBot = await createBot('PinDbTest')
    await sleep(2000)
    await opPlayer('PinDbTest')
    await sleep(500)
    await clearPlayerPins()
    await sleep(500)

    const initialCount = await getPlayerPinCount('PinDbTest')
    console.log(`Initial pin count: ${initialCount}`)

    playerBot.clearSystemMessages()
    await playerBot.sendChat('/bmpin')
    await playerBot.waitForSystemMessage('expires', 10000)
    await sleep(1000)

    const newCount = await getPlayerPinCount('PinDbTest')
    // Pin count should be exactly 1 (old pins deleted before new one created)
    expect(newCount).toBe(1)
    console.log(`Pin count after /bmpin: ${newCount}`)

    await playerBot.disconnect()
  }, 30000)

  test('new pin generation deletes old pins for same player', async () => {
    // Use a unique player for this test (max 16 chars)
    playerBot = await createBot('PinDelTest')
    await sleep(2000)
    await opPlayer('PinDelTest')
    await sleep(500)
    await clearPlayerPins()
    await sleep(500)

    // First call creates a pin
    playerBot.clearSystemMessages()
    await playerBot.sendChat('/bmpin')
    await playerBot.waitForSystemMessage('expires', 10000)
    await sleep(500)

    const afterFirstCall = await getPlayerPinCount('PinDelTest')
    console.log(`Pin count after first call: ${afterFirstCall}`)
    expect(afterFirstCall).toBe(1)

    // Wait for rate limit to expire (30 seconds)
    console.log('Waiting 31 seconds for rate limit to expire...')
    await sleep(31000)

    // Second call should delete old pin and create new one
    playerBot.clearSystemMessages()
    await playerBot.sendChat('/bmpin')
    await playerBot.waitForSystemMessage('expires', 10000)
    await sleep(500)

    // Should still have only 1 pin (old one was deleted)
    const finalCount = await getPlayerPinCount('PinDelTest')
    console.log(`Pin count after second call: ${finalCount}`)
    expect(finalCount).toBe(1)

    await playerBot.disconnect()
  }, 70000)
})
