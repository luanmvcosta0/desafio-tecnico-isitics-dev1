import { Route, Routes } from 'react-router'
import { AppLayout } from '../components/layout/AppLayout'
import { ClubDetailsPage } from '../pages/ClubDetailsPage'
import { ClubsPage } from '../pages/ClubsPage'
import { HomePage } from '../pages/HomePage'
import { MatchDetailsPage } from '../pages/MatchDetailsPage'
import { MatchesPage } from '../pages/MatchesPage'
import { NotFoundPage } from '../pages/NotFoundPage'
import { PlayersPage } from '../pages/PlayersPage'
import { StandingsPage } from '../pages/StandingsPage'

export function AppRoutes() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route index element={<HomePage />} />
        <Route path="players" element={<PlayersPage />} />
        <Route path="clubs" element={<ClubsPage />} />
        <Route path="clubs/:clubId" element={<ClubDetailsPage />} />
        <Route path="matches" element={<MatchesPage />} />
        <Route path="matches/:matchId" element={<MatchDetailsPage />} />
        <Route path="standings" element={<StandingsPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}
