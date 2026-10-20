const DEFAULT_API_ORIGIN = 'http://localhost:8080'

export const env = {
  apiBaseUrl: `${import.meta.env.VITE_API_ORIGIN ?? DEFAULT_API_ORIGIN}/api/v1`,
}
