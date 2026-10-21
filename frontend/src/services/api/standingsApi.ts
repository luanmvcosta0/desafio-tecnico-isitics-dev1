import type { Standing } from '../../types/league'
import { requestJson } from './httpClient'

export function getStandings(): Promise<Standing[]> {
  return requestJson<Standing[]>('/standings')
}
