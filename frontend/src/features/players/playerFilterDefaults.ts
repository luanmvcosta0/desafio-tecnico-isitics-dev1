import type { PlayerFilters } from '../../types/player'

export const EMPTY_PLAYER_FILTERS: PlayerFilters = {
  search: '',
  position: '',
  condition: '',
  free: false,
  club_id: '',
  min_age: '',
  max_age: '',
  min_value: '',
  max_value: '',
  sort: 'name',
}
