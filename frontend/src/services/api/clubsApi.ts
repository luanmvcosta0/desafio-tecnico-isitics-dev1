import type { Club, ClubCreateInput, ClubSort } from '../../types/club'
import { requestJson } from './httpClient'

const CLUBS_PATH = '/clubs'

export function listClubs(sort: ClubSort): Promise<Club[]> {
  return requestJson<Club[]>(CLUBS_PATH, { query: { sort } })
}

export function createClub(input: ClubCreateInput): Promise<Club> {
  return requestJson<Club>(CLUBS_PATH, { method: 'POST', body: input })
}
