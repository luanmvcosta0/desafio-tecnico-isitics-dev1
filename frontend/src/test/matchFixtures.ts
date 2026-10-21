import type { Duel, Match } from '../types/league'

function duel(
  order: number,
  attacker: string,
  value: number,
  defender: string,
  defense: number,
): Duel {
  return {
    order,
    attacking_club_id: order <= 3 ? 1 : 2,
    attacker: {
      player_id: order,
      player_name: attacker,
      position: 'ATA',
      condition: 'APTO',
      attribute: 'shooting',
      value,
    },
    defender: {
      player_id: order + 10,
      player_name: defender,
      position: 'GOL',
      condition: 'APTO',
      attribute: 'defending',
      value: defense,
    },
    goal: value > defense,
  }
}

export const aMatch: Match = {
  id: 1,
  played_at: '2026-11-12T15:00:00-03:00',
  home: { club_id: 1, club_name: 'Águias FC', formation: '2-1-1', goals: 1 },
  away: { club_id: 2, club_name: 'Delta FC', formation: '2-1-1', goals: 2 },
  result: 'VISITANTE',
  duels: [
    duel(1, 'Otto Rangel', 75, 'Ivo Teles', 72),
    duel(2, 'Lauro Neves', 82, 'Hugo Vaz', 84),
    duel(3, 'Davi Lins', 63, 'Lino Matos', 63),
    duel(4, 'Ugo Falcão', 81, 'Rui Prado', 77),
    duel(5, 'Nei Soares', 70, 'Téo Brandão', 84),
    duel(6, 'Lino Matos', 79, 'Davi Lins', 56),
  ],
}
