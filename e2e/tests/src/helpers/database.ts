import mysql, { Pool, RowDataPacket } from 'mysql2/promise'

const DB_HOST = process.env.DB_HOST ?? 'mariadb'
const DB_PORT = parseInt(process.env.DB_PORT ?? '3306', 10)
const DB_NAME = process.env.DB_NAME ?? 'banmanager'
const DB_USER = process.env.DB_USER ?? 'banmanager'
const DB_PASSWORD = process.env.DB_PASSWORD ?? 'banmanager'

let pool: Pool | null = null

export async function getConnection (): Promise<Pool> {
  if (pool == null) {
    pool = mysql.createPool({
      host: DB_HOST,
      port: DB_PORT,
      database: DB_NAME,
      user: DB_USER,
      password: DB_PASSWORD,
      waitForConnections: true,
      connectionLimit: 5
    })
  }
  return pool
}

export async function closeDatabase (): Promise<void> {
  if (pool != null) {
    await pool.end()
    pool = null
  }
}

export interface ServerLog {
  id: number
  message: string
  created: number
}

export interface ReportLog {
  id: number
  report_id: number
  log_id: number
}

export interface PlayerPin {
  id: number
  player_id: Buffer
  pin: string
  expires: number
}

export async function getServerLogs (limit: number = 100): Promise<ServerLog[]> {
  const conn = await getConnection()
  const [rows] = await conn.query<RowDataPacket[]>(
    'SELECT id, message, created FROM bm_server_logs ORDER BY created DESC LIMIT ?',
    [limit]
  )
  return rows as ServerLog[]
}

export async function getReportLogs (reportId: number): Promise<ReportLog[]> {
  const conn = await getConnection()
  const [rows] = await conn.query<RowDataPacket[]>(
    'SELECT id, report_id, log_id FROM bm_report_logs WHERE report_id = ?',
    [reportId]
  )
  return rows as ReportLog[]
}

export interface ReportLogWithMessage {
  id: number
  report_id: number
  log_id: number
  message: string
  created: number
}

export async function getReportLogsWithMessages (reportId: number): Promise<ReportLogWithMessage[]> {
  const conn = await getConnection()
  const [rows] = await conn.query<RowDataPacket[]>(
    `SELECT rl.id, rl.report_id, rl.log_id, sl.message, sl.created
     FROM bm_report_logs rl
     JOIN bm_server_logs sl ON rl.log_id = sl.id
     WHERE rl.report_id = ?
     ORDER BY sl.created DESC`,
    [reportId]
  )
  return rows as ReportLogWithMessage[]
}

export async function getLatestReport (): Promise<{ id: number } | null> {
  const conn = await getConnection()
  const [rows] = await conn.query<RowDataPacket[]>(
    'SELECT id FROM bm_player_reports ORDER BY created DESC LIMIT 1'
  )
  return rows.length > 0 ? { id: rows[0].id } : null
}

export async function getPlayerPinCount (playerName: string): Promise<number> {
  const conn = await getConnection()
  const [rows] = await conn.query<RowDataPacket[]>(`
    SELECT COUNT(*) as count
    FROM bm_player_pins pp
    JOIN bm_players p ON pp.player_id = p.id
    WHERE p.name = ?
  `, [playerName])
  return rows[0]?.count ?? 0
}

export async function getLatestPlayerPin (playerName: string): Promise<PlayerPin | null> {
  const conn = await getConnection()
  const [rows] = await conn.query<RowDataPacket[]>(`
    SELECT pp.id, pp.player_id, pp.pin, pp.expires
    FROM bm_player_pins pp
    JOIN bm_players p ON pp.player_id = p.id
    WHERE p.name = ?
    ORDER BY pp.expires DESC
    LIMIT 1
  `, [playerName])
  return rows.length > 0 ? rows[0] as PlayerPin : null
}

export async function clearServerLogs (): Promise<void> {
  const conn = await getConnection()
  await conn.query('DELETE FROM bm_server_logs')
}

export async function clearReportLogs (): Promise<void> {
  const conn = await getConnection()
  await conn.query('DELETE FROM bm_report_logs')
}

export async function deleteReportsForPlayer (playerName: string): Promise<void> {
  const conn = await getConnection()
  await conn.query(`
    DELETE pr FROM bm_player_reports pr
    JOIN bm_players p ON pr.player_id = p.id
    WHERE p.name = ?
  `, [playerName])
}

export async function clearPlayerPins (): Promise<void> {
  const conn = await getConnection()
  await conn.query('DELETE FROM bm_player_pins')
}

export async function getLogCount (): Promise<number> {
  const conn = await getConnection()
  const [rows] = await conn.query<RowDataPacket[]>(
    'SELECT COUNT(*) as count FROM bm_server_logs'
  )
  return rows[0]?.count ?? 0
}

export async function getLogsContaining (text: string): Promise<ServerLog[]> {
  const conn = await getConnection()
  const [rows] = await conn.query<RowDataPacket[]>(
    'SELECT id, message, created FROM bm_server_logs WHERE message LIKE ? ORDER BY created DESC',
    [`%${text}%`]
  )
  return rows as ServerLog[]
}

export async function getAllPlayerPins (playerName: string): Promise<PlayerPin[]> {
  const conn = await getConnection()
  const [rows] = await conn.query<RowDataPacket[]>(`
    SELECT pp.id, pp.player_id, pp.pin, pp.expires
    FROM bm_player_pins pp
    JOIN bm_players p ON pp.player_id = p.id
    WHERE p.name = ?
    ORDER BY pp.expires DESC
  `, [playerName])
  return rows as PlayerPin[]
}
