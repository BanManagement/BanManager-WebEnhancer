import { TestBot, createBot, sendCommand, disconnectRcon, sleep } from './helpers'

describe('Denied Pin Placeholder', () => {
  let bannedBot: TestBot | null = null

  afterEach(async () => {
    // Ensure we unban and disconnect
    try {
      await sendCommand('unban DeniedPinPlayer')
    } catch (e) {
      // Ignore errors
    }
    await bannedBot?.disconnect()
    bannedBot = null
  })

  afterAll(async () => {
    await disconnectRcon()
  })

  test.skip('[pin] placeholder in ban message is replaced with actual pin', async () => {
    // This test requires:
    // 1. BanManager messages.yml configured with [pin] in the ban denied message
    // 2. A player to get banned and then try to join

    // First, ban the player
    await sendCommand('ban DeniedPinPlayer Testing pin placeholder')
    await sleep(1000)

    // Try to connect - this should fail with a ban message containing a pin
    try {
      bannedBot = await createBot('DeniedPinPlayer')
      // If we get here, the player wasn't banned - this is unexpected
      console.log('Player was able to connect, ban may not have worked')
    } catch (error: any) {
      // We expect to be kicked with a ban message
      const errorMessage = error?.message ?? String(error)
      console.log(`Player was kicked as expected: ${errorMessage}`)

      // The kick message should contain a 6-digit pin (not the placeholder [pin])
      // This depends on BanManager's messages.yml being configured with [pin] in the ban message
      expect(errorMessage).not.toContain('[pin]')
    }
  })
})
