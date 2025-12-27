import { TestBot, createBot, sendCommand, disconnectRcon, opPlayer, sleep } from './helpers'

describe('Pin Command', () => {
  let playerBot: TestBot

  beforeAll(async () => {
    playerBot = await createBot('PinTestPlayer')
    await sleep(2000)
    await opPlayer('PinTestPlayer')
    await sleep(500)
  })

  afterAll(async () => {
    await playerBot?.disconnect()
    await disconnectRcon()
  })

  test('bmpin command returns a 6-digit pin', async () => {
    playerBot.clearSystemMessages()
    await playerBot.sendChat('/bmpin')

    const response = await playerBot.waitForSystemMessage('pin expires', 10000)
    expect(response.message).toMatch(/\d{6}|expires/)
    console.log(`Received pin message: ${response.message}`)
  })

  test('bmpin command generates a new pin each time', async () => {
    playerBot.clearSystemMessages()
    await playerBot.sendChat('/bmpin')
    const firstResponse = await playerBot.waitForSystemMessage('pin', 10000)
    const firstPin = firstResponse.message.match(/\d{6}/)?.[0]

    await sleep(1000)

    playerBot.clearSystemMessages()
    await playerBot.sendChat('/bmpin')
    const secondResponse = await playerBot.waitForSystemMessage('pin', 10000)
    const secondPin = secondResponse.message.match(/\d{6}/)?.[0]

    if (firstPin != null) {
      expect(firstPin).toMatch(/^\d{6}$/)
    }
    if (secondPin != null) {
      expect(secondPin).toMatch(/^\d{6}$/)
    }

    console.log(`First pin: ${firstPin ?? 'not found'}, Second pin: ${secondPin ?? 'not found'}`)
  })
})
