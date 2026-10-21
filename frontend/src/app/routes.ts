export const routes = {
  home: '/',
  players: '/players',
  clubs: '/clubs',
  matches: '/matches',
  standings: '/standings',
} as const

export function clubDetailsPath(clubId: number): string {
  return `${routes.clubs}/${clubId}`
}

export function matchDetailsPath(matchId: number): string {
  return `${routes.matches}/${matchId}`
}
