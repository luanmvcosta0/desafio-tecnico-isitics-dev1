import type { DuelAttribute, MatchResult } from '../../types/league'

export const duelAttributeLabels: Record<DuelAttribute, string> = {
  shooting: 'Finalização',
  passing: 'Passe',
  dribbling: 'Drible',
  defending: 'Defesa',
}

export const matchResultLabels: Record<MatchResult, string> = {
  MANDANTE: 'Vitória do mandante',
  VISITANTE: 'Vitória do visitante',
  EMPATE: 'Empate',
}
