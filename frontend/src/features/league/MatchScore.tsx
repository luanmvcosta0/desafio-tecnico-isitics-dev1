import type { Match } from '../../types/league'
import { formatDateTime } from '../../utils/format'
import { matchResultLabels } from './leagueLabels'

interface MatchScoreProps {
  match: Match
}

/** Score and result exactly as recorded by the API. */
export function MatchScore({ match }: MatchScoreProps) {
  return (
    <section
      aria-label="Placar"
      className="flex flex-col items-center gap-1 rounded-xl border border-slate-200 p-4 text-center"
    >
      <p className="text-xs text-slate-500">{formatDateTime(match.played_at)}</p>
      <p className="text-xl font-bold">
        {match.home.club_name} {match.home.goals} × {match.away.goals} {match.away.club_name}
      </p>
      <p className="text-sm text-slate-600">
        {matchResultLabels[match.result]} · formações {match.home.formation} e{' '}
        {match.away.formation}
      </p>
    </section>
  )
}
