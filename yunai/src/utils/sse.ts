export type SseMessage = {
  data?: string
  event?: string
}

export const createSseUrl = (path: string, params: Record<string, string | number>) => {
  const url = new URL(`http://localhost:8123/api${path}`)
  Object.entries(params).forEach(([key, value]) => {
    url.searchParams.set(key, String(value))
  })
  return url.toString()
}

export const parseSseChunk = (raw: string) => {
  if (!raw) {
    return ''
  }
  try {
    const parsed = JSON.parse(raw)
    return parsed?.d ?? ''
  } catch {
    return raw
  }
}
