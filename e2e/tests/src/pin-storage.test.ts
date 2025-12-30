import {
  TestBot,
  createBot,
  disconnectRcon,
  opPlayer,
  sleep,
  getPlayerPinCount,
  getLatestPlayerPin,
  closeDatabase
} from './helpers'

describe('Pin Storage', () => {
  let playerBot: TestBot

  afterAll(async () => {
    await playerBot?.disconnect()
    await disconnectRcon()
    await closeDatabase()
  })

  test('pin is stored in database after /bmpin command', async () => {
    // Use a unique player for this test (max 16 chars)
    playerBot = await createBot('PinStoreTest1')
    await sleep(2000)
    await opPlayer('PinStoreTest1')
    await sleep(500)

    const initialCount = await getPlayerPinCount('PinStoreTest1')

    playerBot.clearSystemMessages()
    await playerBot.sendChat('/bmpin')

    const response = await playerBot.waitForSystemMessage('expires', 10000)
    console.log(`Received pin message: ${response.message}`)

    await sleep(1000)

    const newCount = await getPlayerPinCount('PinStoreTest1')
    expect(newCount).toBeGreaterThanOrEqual(1)
    console.log(`Pin count: ${newCount}`)

    const latestPin = await getLatestPlayerPin('PinStoreTest1')
    expect(latestPin).not.toBeNull()
    expect(latestPin?.expires).toBeGreaterThan(Date.now() / 1000)
    console.log(`Pin expires at ${new Date((latestPin?.expires ?? 0) * 1000)}`)

    await playerBot.disconnect()
  }, 20000)

  test('pin hash is stored securely with argon2', async () => {
    // Use a unique player for this test (max 16 chars)
    playerBot = await createBot('PinStoreTest2')
    await sleep(2000)
    await opPlayer('PinStoreTest2')
    await sleep(500)

    playerBot.clearSystemMessages()
    await playerBot.sendChat('/bmpin')

    await playerBot.waitForSystemMessage('expires', 10000)
    await sleep(1000)

    const latestPin = await getLatestPlayerPin('PinStoreTest2')
    expect(latestPin).not.toBeNull()
    expect(latestPin?.pin).toContain('$argon2')
    console.log(`Pin is hashed with argon2: ${latestPin?.pin.substring(0, 30)}...`)

    await playerBot.disconnect()
  }, 20000)
})
