import {
  TestBot,
  createBot,
  sendCommand,
  disconnectRcon,
  opPlayer,
  sleep,
  getReportLogsWithMessages,
  getLatestReportByReason,
  clearReportLogs,
  deleteReportsForPlayer,
  closeDatabase,
  isProxy
} from './helpers'

/**
 * Log filtering tests - verifies that ignoreContains patterns are applied.
 *
 * IMPORTANT: Logs are only persisted to the database when a report is created.
 * The queue captures logs, filters are applied during capture, and then
 * logs are written to bm_server_logs when a report triggers persistence.
 *
 * So to test filtering, we must:
 * 1. Generate log messages (some filtered, some not)
 * 2. Create a report to trigger log persistence
 * 3. Verify filtered messages are NOT in the report's logs
 *
 * NOTE: This test suite only runs on non-proxy platforms (Bukkit, Fabric, Sponge)
 * since proxies don't have access to server logs.
 */
const describeOrSkip = isProxy() ? describe.skip : describe

describeOrSkip('Log Filtering (ignoreContains)', () => {
  let reporterBot: TestBot
  let targetBot: TestBot

  const waitForReportLogs = async (reportId: number, marker?: string) => {
    let lastLogs = await getReportLogsWithMessages(reportId)

    for (let i = 0; i < 20; i++) {
      if (lastLogs.length > 0 && (marker == null || lastLogs.some(log => log.message.includes(marker)))) {
        return lastLogs
      }

      await sleep(500)
      lastLogs = await getReportLogsWithMessages(reportId)
    }

    return lastLogs
  }

  const waitForReportByReason = async (reason: string) => {
    for (let i = 0; i < 20; i++) {
      const report = await getLatestReportByReason(reason)
      if (report != null) return report
      await sleep(500)
    }

    return null
  }

  beforeAll(async () => {
    reporterBot = await createBot('FilterReporter')
    await sleep(2000)
    await opPlayer('FilterReporter')
    await sleep(500)

    targetBot = await createBot('FilterTarget')
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
    await deleteReportsForPlayer('FilterTarget')
    await sleep(500)
  })

  test('normal messages appear in report logs', async () => {
    const uniqueId = Date.now()
    const normalMessage = `NormalTestMessage_${uniqueId}`
    const reportReason = `Testing log capture ${uniqueId}`

    // Generate a normal (non-filtered) log message
    await sendCommand(`say ${normalMessage}`)
    await sleep(500)

    // Create a report to trigger log persistence
    reporterBot.clearSystemMessages()
    await reporterBot.sendChat(`/report FilterTarget ${reportReason}`)

    const report = await waitForReportByReason(reportReason)
    expect(report).not.toBeNull()

    if (report != null) {
      const logs = await waitForReportLogs(report.id, normalMessage)
      console.log(`Report ${report.id} has ${logs.length} associated logs`)

      // Verify the normal message is in the logs
      const found = logs.some(log => log.message.includes(normalMessage))
      expect(found).toBe(true)
    }
  }, 30000)

  test('[BanManager] messages are filtered from report logs', async () => {
    const uniqueId = Date.now()
    const bmMessage = `[BanManager] FilteredMessage_${uniqueId}`
    const normalMessage = `NormalMarker_${uniqueId}`
    const reportReason = `Testing BanManager filter ${uniqueId}`

    // Generate both a filtered and a normal message
    await sendCommand(`say ${bmMessage}`)
    await sendCommand(`say ${normalMessage}`)
    await sleep(500)

    // Create a report to trigger log persistence
    reporterBot.clearSystemMessages()
    await reporterBot.sendChat(`/report FilterTarget ${reportReason}`)

    const report = await waitForReportByReason(reportReason)
    expect(report).not.toBeNull()

    if (report != null) {
      const logs = await waitForReportLogs(report.id, normalMessage)
      console.log(`Report ${report.id} has ${logs.length} associated logs`)

      // Verify [BanManager] message is NOT in logs (filtered)
      const bmFound = logs.some(log => log.message.includes(bmMessage))
      expect(bmFound).toBe(false)

      // Verify normal message IS in logs (to confirm logs were captured)
      const normalFound = logs.some(log => log.message.includes(normalMessage))
      expect(normalFound).toBe(true)

      console.log(`[BanManager] messages correctly filtered, normal messages captured`)
    }
  }, 30000)

  test('Metrics messages are filtered from report logs', async () => {
    const uniqueId = Date.now()
    const metricsMessage = `Metrics data test_${uniqueId}`
    const normalMessage = `NormalMarkerMet_${uniqueId}`
    const reportReason = `Testing Metrics filter ${uniqueId}`

    await sendCommand(`say ${metricsMessage}`)
    await sendCommand(`say ${normalMessage}`)
    await sleep(500)

    reporterBot.clearSystemMessages()
    await reporterBot.sendChat(`/report FilterTarget ${reportReason}`)

    const report = await waitForReportByReason(reportReason)
    expect(report).not.toBeNull()

    if (report != null) {
      const logs = await waitForReportLogs(report.id, normalMessage)

      // Metrics message should be filtered
      const metricsFound = logs.some(log => log.message.includes(metricsMessage))
      expect(metricsFound).toBe(false)

      // Normal message should exist
      const normalFound = logs.some(log => log.message.includes(normalMessage))
      expect(normalFound).toBe(true)

      console.log(`Metrics messages correctly filtered`)
    }
  }, 30000)

  test('[PlugMan] messages are filtered from report logs', async () => {
    const uniqueId = Date.now()
    const plugmanMessage = `[PlugMan] Action test_${uniqueId}`
    const normalMessage = `NormalMarkerPlugMan_${uniqueId}`
    const reportReason = `Testing PlugMan filter ${uniqueId}`

    await sendCommand(`say ${plugmanMessage}`)
    await sendCommand(`say ${normalMessage}`)
    await sleep(500)

    reporterBot.clearSystemMessages()
    await reporterBot.sendChat(`/report FilterTarget ${reportReason}`)

    const report = await waitForReportByReason(reportReason)
    expect(report).not.toBeNull()

    if (report != null) {
      const logs = await waitForReportLogs(report.id, normalMessage)

      // [PlugMan] message should be filtered
      const plugmanFound = logs.some(log => log.message.includes(plugmanMessage))
      expect(plugmanFound).toBe(false)

      // Normal message should exist
      const normalFound = logs.some(log => log.message.includes(normalMessage))
      expect(normalFound).toBe(true)

      console.log(`[PlugMan] messages correctly filtered`)
    }
  }, 30000)

  test('report command output is filtered from report logs', async () => {
    const uniqueId = Date.now()
    const reportMessage = 'issued server command: /report'
    const normalMessage = `NormalMarkerReport_${uniqueId}`
    const reportReason = `Testing report cmd filter ${uniqueId}`

    await sendCommand(`say ${reportMessage}`)
    await sendCommand(`say ${normalMessage}`)
    await sleep(500)

    reporterBot.clearSystemMessages()
    await reporterBot.sendChat(`/report FilterTarget ${reportReason}`)

    const report = await waitForReportByReason(reportReason)
    expect(report).not.toBeNull()

    if (report != null) {
      const logs = await waitForReportLogs(report.id, normalMessage)

      // Report command message should be filtered
      const reportCmdFound = logs.some(log => log.message.includes(reportMessage))
      expect(reportCmdFound).toBe(false)

      // Normal message should exist
      const normalFound = logs.some(log => log.message.includes(normalMessage))
      expect(normalFound).toBe(true)

      console.log(`Report command output correctly filtered`)
    }
  }, 30000)
})
