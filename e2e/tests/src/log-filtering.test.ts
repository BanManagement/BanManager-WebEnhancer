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

    for (let i = 0; i < 30; i++) {
      if (lastLogs.length > 0 && (marker == null || lastLogs.some(log => log.message.includes(marker)))) {
        return lastLogs
      }

      await sleep(500)
      lastLogs = await getReportLogsWithMessages(reportId)
    }

    return lastLogs
  }

  const waitForReportByReason = async (reason: string) => {
    for (let i = 0; i < 40; i++) {
      const report = await getLatestReportByReason(reason)
      if (report != null) return report
      await sleep(500)
    }

    return null
  }

  const captureLogsForReason = async (
    reasonPrefix: string,
    normalMarker: string,
    emitMessages: () => Promise<void>
  ) => {
    for (let attempt = 1; attempt <= 3; attempt++) {
      await emitMessages()
      // Give Sponge time to flush command output to latest.log before snapshotting logs on report.
      await sleep(1500)
      // Re-emit the marker immediately before /report so it is always inside the captured tail window.
      await sendCommand(`say ${normalMarker}`)
      await sleep(300)

      const reportReason = `${reasonPrefix} attempt-${attempt} ${Date.now()}`
      reporterBot.clearSystemMessages()
      await reporterBot.sendChat(`/report FilterTarget ${reportReason}`)

      const report = await waitForReportByReason(reportReason)
      if (report == null) {
        continue
      }

      const logs = await waitForReportLogs(report.id, normalMarker)
      if (logs.some(log => log.message.includes(normalMarker))) {
        return logs
      }
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
    const logs = await captureLogsForReason(
      'Testing log capture',
      normalMessage,
      async () => {
        await sendCommand(`say ${normalMessage}`)
      }
    )

    expect(logs).not.toBeNull()
    if (logs != null) {
      console.log(`Found ${logs.length} associated logs for normal message test`)
      const found = logs.some(log => log.message.includes(normalMessage))
      expect(found).toBe(true)
    }
  }, 60000)

  test('[BanManager] messages are filtered from report logs', async () => {
    const uniqueId = Date.now()
    const bmMessage = `[BanManager] FilteredMessage_${uniqueId}`
    const normalMessage = `NormalMarker_${uniqueId}`
    const logs = await captureLogsForReason(
      'Testing BanManager filter',
      normalMessage,
      async () => {
        await sendCommand(`say ${bmMessage}`)
        await sendCommand(`say ${normalMessage}`)
      }
    )

    expect(logs).not.toBeNull()
    if (logs != null) {
      const bmFound = logs.some(log => log.message.includes(bmMessage))
      expect(bmFound).toBe(false)

      const normalFound = logs.some(log => log.message.includes(normalMessage))
      expect(normalFound).toBe(true)

      console.log('[BanManager] messages correctly filtered, normal messages captured')
    }
  }, 60000)

  test('Metrics messages are filtered from report logs', async () => {
    const uniqueId = Date.now()
    const metricsMessage = `Metrics data test_${uniqueId}`
    const normalMessage = `NormalMarkerMet_${uniqueId}`
    const logs = await captureLogsForReason(
      'Testing Metrics filter',
      normalMessage,
      async () => {
        await sendCommand(`say ${metricsMessage}`)
        await sendCommand(`say ${normalMessage}`)
      }
    )

    expect(logs).not.toBeNull()
    if (logs != null) {
      const metricsFound = logs.some(log => log.message.includes(metricsMessage))
      expect(metricsFound).toBe(false)

      const normalFound = logs.some(log => log.message.includes(normalMessage))
      expect(normalFound).toBe(true)

      console.log('Metrics messages correctly filtered')
    }
  }, 60000)

  test('[PlugMan] messages are filtered from report logs', async () => {
    const uniqueId = Date.now()
    const plugmanMessage = `[PlugMan] Action test_${uniqueId}`
    const normalMessage = `NormalMarkerPlugMan_${uniqueId}`
    const logs = await captureLogsForReason(
      'Testing PlugMan filter',
      normalMessage,
      async () => {
        await sendCommand(`say ${plugmanMessage}`)
        await sendCommand(`say ${normalMessage}`)
      }
    )

    expect(logs).not.toBeNull()
    if (logs != null) {
      const plugmanFound = logs.some(log => log.message.includes(plugmanMessage))
      expect(plugmanFound).toBe(false)

      const normalFound = logs.some(log => log.message.includes(normalMessage))
      expect(normalFound).toBe(true)

      console.log('[PlugMan] messages correctly filtered')
    }
  }, 60000)

  test('report command output is filtered from report logs', async () => {
    const uniqueId = Date.now()
    const reportMessage = 'issued server command: /report'
    const normalMessage = `NormalMarkerReport_${uniqueId}`
    const logs = await captureLogsForReason(
      'Testing report cmd filter',
      normalMessage,
      async () => {
        await sendCommand(`say ${reportMessage}`)
        await sendCommand(`say ${normalMessage}`)
      }
    )

    expect(logs).not.toBeNull()
    if (logs != null) {
      const reportCmdFound = logs.some(log => log.message.includes(reportMessage))
      expect(reportCmdFound).toBe(false)

      const normalFound = logs.some(log => log.message.includes(normalMessage))
      expect(normalFound).toBe(true)

      console.log('Report command output correctly filtered')
    }
  }, 60000)
})
