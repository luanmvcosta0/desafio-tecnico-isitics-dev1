import type { Club } from '../../types/club'

/** Club choices of the selects that pick a club among all of them ("ABC · Name"). */
export function clubOptions(clubs: Club[]) {
  return clubs.map((club) => ({ value: String(club.id), label: `${club.acronym} · ${club.name}` }))
}
