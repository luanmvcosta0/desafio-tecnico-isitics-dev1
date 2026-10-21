import { CONDITIONS, POSITIONS, type Condition, type Position } from '../../types/player'

export const positionLabels: Record<Position, string> = {
  GOL: 'Goleiro',
  ZAG: 'Zagueiro',
  LAT: 'Lateral',
  MEI: 'Meio-campista',
  ATA: 'Atacante',
}

export const conditionLabels: Record<Condition, string> = {
  APTO: 'Apto',
  MAL_CONDICIONADO: 'Mal condicionado',
  DOENTE: 'Doente',
}

export const FREE_PLAYER_LABEL = 'Livre'

export const positionOptions = POSITIONS.map((position) => ({
  value: position,
  label: `${position} · ${positionLabels[position]}`,
}))

export const conditionOptions = CONDITIONS.map((condition) => ({
  value: condition,
  label: conditionLabels[condition],
}))
