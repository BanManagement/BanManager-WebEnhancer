import { Rcon } from 'rcon-client'

const RCON_HOST = process.env.RCON_HOST ?? 'localhost'
const RCON_PORT = parseInt(process.env.RCON_PORT ?? '25575', 10)
const RCON_PASSWORD = process.env.RCON_PASSWORD ?? 'testing'

// Proxy mode: When testing on a proxy (Velocity, BungeeCord, etc.), some commands
// like 'list' and 'op' need to be sent to the backend server instead
const IS_PROXY = process.env.IS_PROXY === 'true'

// Backend server RCON for commands that don't exist on proxies
const BACKEND_RCON_HOST = process.env.BACKEND_RCON_HOST ?? ''
const BACKEND_RCON_PORT = parseInt(process.env.BACKEND_RCON_PORT ?? '25575', 10)
const BACKEND_RCON_PASSWORD = process.env.BACKEND_RCON_PASSWORD ?? 'testing'

let rconClient: Rcon | null = null
let backendRconClient: Rcon | null = null

export async function connectRcon (): Promise<Rcon> {
  if (rconClient?.authenticated === true) {
    return rconClient
  }

  rconClient = await Rcon.connect({
    host: RCON_HOST,
    port: RCON_PORT,
    password: RCON_PASSWORD
  })

  console.log(`Connected to RCON at ${RCON_HOST}:${RCON_PORT}`)
  return rconClient
}

async function connectBackendRcon (): Promise<Rcon> {
  if (backendRconClient?.authenticated === true) {
    return backendRconClient
  }

  backendRconClient = await Rcon.connect({
    host: BACKEND_RCON_HOST,
    port: BACKEND_RCON_PORT,
    password: BACKEND_RCON_PASSWORD
  })

  console.log(`Connected to Backend RCON at ${BACKEND_RCON_HOST}:${BACKEND_RCON_PORT}`)
  return backendRconClient
}

export async function sendCommand (command: string): Promise<string> {
  const client = await connectRcon()
  console.log(`RCON: ${command}`)
  const response = await client.send(command)
  console.log(`RCON Response: ${response}`)
  return response
}

export async function disconnectRcon (): Promise<void> {
  if (rconClient != null) {
    await rconClient.end()
    rconClient = null
    console.log('Disconnected from RCON')
  }
  if (backendRconClient != null) {
    await backendRconClient.end()
    backendRconClient = null
    console.log('Disconnected from Backend RCON')
  }
}

/**
 * Check if running on a proxy (Velocity, BungeeCord, etc.)
 */
export function isProxy (): boolean {
  return IS_PROXY
}

export async function reloadPlugin (): Promise<string> {
  return await sendCommand('bmreload')
}

export async function mutePlayer (player: string, reason: string = 'E2E Test'): Promise<string> {
  return await sendCommand(`bmmute ${player} ${reason}`)
}

export async function unmutePlayer (player: string): Promise<string> {
  return await sendCommand(`bmunmute ${player}`)
}

export async function getPlayerList (): Promise<string> {
  // On proxies, use the backend server's RCON for 'list' command
  if (IS_PROXY && BACKEND_RCON_HOST !== '') {
    const client = await connectBackendRcon()
    return await client.send('list')
  }
  return await sendCommand('list')
}

export async function banPlayer (player: string, reason: string = 'E2E Test'): Promise<string> {
  return await sendCommand(`bmban ${player} ${reason}`)
}

export async function unbanPlayer (player: string): Promise<string> {
  const bmResult = await sendCommand(`bmunban ${player}`)
  try {
    await sendCommand(`pardon ${player}`)
  } catch { /* ignore */ }
  return bmResult
}

export async function tempBanPlayer (player: string, duration: string, reason: string = 'E2E Test'): Promise<string> {
  return await sendCommand(`bmtempban ${player} ${duration} ${reason}`)
}

export async function tempMutePlayer (player: string, duration: string, reason: string = 'E2E Test'): Promise<string> {
  return await sendCommand(`bmtempmute ${player} ${duration} ${reason}`)
}

export async function warnPlayer (player: string, reason: string = 'E2E Test'): Promise<string> {
  return await sendCommand(`bmwarn ${player} ${reason}`)
}

export async function kickPlayer (player: string, reason: string = 'E2E Test'): Promise<string> {
  return await sendCommand(`bmkick ${player} ${reason}`)
}

export async function reportPlayer (player: string, reason: string = 'E2E Test Report'): Promise<string> {
  return await sendCommand(`report ${player} ${reason}`)
}

export async function opPlayer (player: string): Promise<string> {
  // Proxies don't have 'op' command - send to backend server
  if (IS_PROXY && BACKEND_RCON_HOST !== '') {
    const client = await connectBackendRcon()
    const response = await client.send(`op ${player}`)
    console.log(`Backend RCON op response: ${response}`)
    return response
  }
  return await sendCommand(`op ${player}`)
}

export async function deopPlayer (player: string): Promise<string> {
  // Proxies don't have 'deop' command - send to backend server
  if (IS_PROXY && BACKEND_RCON_HOST !== '') {
    const client = await connectBackendRcon()
    const response = await client.send(`deop ${player}`)
    console.log(`Backend RCON deop response: ${response}`)
    return response
  }
  return await sendCommand(`deop ${player}`)
}
