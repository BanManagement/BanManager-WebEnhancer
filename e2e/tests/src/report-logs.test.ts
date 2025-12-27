import {
  TestBot,
  createBot,
  sendCommand,
  disconnectRcon,
  opPlayer,
  sleep,
  getServerLogs,
  getReportLogs,
  getLatestReport,
  clearReportLogs,
  deleteReportsForPlayer,
  closeDatabase
} from './helpers'

describe('Report Log Capture', () => {
  let reporterBot: TestBot
  let targetBot: TestBot

  beforeAll(async () => {
    reporterBot = await createBot('ReportTestPlayer')
    await sleep(2000)
    await opPlayer('ReportTestPlayer')
    await sleep(500)

    targetBot = await createBot('ReportTarget')
    await sleep(1000)
  })

  afterAll(async () => {
    await reporterBot?.disconnect()
    await targetBot?.disconnect()
    await disconnectRcon()
    await closeDatabase()
  })

  beforeEach(async () => {
    await clearReportLogs()
    await deleteReportsForPlayer('ReportTarget')
  })

  test('server logs are captured when a report is created', async () => {
    await sendCommand('say TestLogMessage_BeforeReport_123')
    await sleep(1000)

    reporterBot.clearSystemMessages()
    await reporterBot.sendChat('/report ReportTarget Test report reason')
    await sleep(3000)

    const report = await getLatestReport()
    expect(report).not.toBeNull()

    if (report != null) {
      const reportLogs = await getReportLogs(report.id)
      expect(reportLogs.length).toBeGreaterThan(0)
      console.log(`Report ${report.id} has ${reportLogs.length} associated logs`)
    }
  }, 30000)

  test('server logs table captures console messages', async () => {
    const uniqueId = Date.now()
    const uniqueMessage = `UniqueTestMessage_${uniqueId}`

    await sendCommand(`say ${uniqueMessage}`)
    await sleep(3000)

    const logs = await getServerLogs(100)
    console.log(`Found ${logs.length} logs in database`)

    if (logs.length > 0) {
      console.log('Sample logs:', logs.slice(0, 5).map(l => l.message.substring(0, 80)))
    }

    const serverLogCount = logs.length
    expect(serverLogCount).toBeGreaterThan(0)
  }, 15000)

  test('ignored patterns are not captured', async () => {
    const ignoredMessage = '[BanManager] This message should be filtered'
    await sendCommand(`say ${ignoredMessage}`)
    await sleep(2000)

    const logs = await getServerLogs(100)
    const found = logs.some(log => log.message.includes(ignoredMessage))
    expect(found).toBe(false)
  }, 15000)
})
