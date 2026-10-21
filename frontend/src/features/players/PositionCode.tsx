import type { Position } from '../../types/player'
import { positionLabels } from './playerLabels'

interface PositionCodeProps {
  position: Position
}

/** Position code of the tables, with the full name on hover. */
export function PositionCode({ position }: PositionCodeProps) {
  return <abbr title={positionLabels[position]}>{position}</abbr>
}
