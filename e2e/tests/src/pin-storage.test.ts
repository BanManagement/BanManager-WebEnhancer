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

  beforeAll(async () => {
    playerBot = await createBot('PinStoragePlayer')
    await sleep(2000)
    await opPlayer('PinStoragePlayer')
    await sleep(500)
  })

  afterAll(async () => {
    await playerBot?.disconnect()
    await disconnectRcon()
    await closeDatabase()
  })

  test('pin is stored in database after /bmpin command', async () => {
    const initialCount = await getPlayerPinCount('PinStoragePlayer')

    playerBot.clearSystemMessages()
    await playerBot.sendChat('/bmpin')

    const response = await playerBot.waitForSystemMessage('pin', 10000)
    console.log(`Received pin message: ${response.message}`)

    await sleep(1000)

    const newCount = await getPlayerPinCount('PinStoragePlayer')
    expect(newCount).toBeGreaterThan(initialCount)
    console.log(`Pin count increased from ${initialCount} to ${newCount}`)

    const latestPin = await getLatestPlayerPin('PinStoragePlayer')
    expect(latestPin).not.toBeNull()
    expect(latestPin?.expires).toBeGreaterThan(Date.now() / 1000)
    console.log(`Pin expires at ${new Date((latestPin?.expires ?? 0) * 1000)}`)
  }, 20000)

  test('pin hash is stored securely with argon2', async () => {
    playerBot.clearSystemMessages()
    await playerBot.sendChat('/bmpin')

    await playerBot.waitForSystemMessage('pin', 10000)
    await sleep(1000)

    const latestPin = await getLatestPlayerPin('PinStoragePlayer')
    expect(latestPin).not.toBeNull()
    expect(latestPin?.pin).toContain('$argon2')
    console.log(`Pin is hashed with argon2: ${latestPin?.pin.substring(0, 30)}...`)
  }, 15000)
})
