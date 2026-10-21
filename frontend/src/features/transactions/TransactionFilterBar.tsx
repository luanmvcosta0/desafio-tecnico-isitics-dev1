import { FormField } from '../../components/ui/FormField'
import { Select } from '../../components/ui/Select'
import type { TransactionFilters, TransactionType } from '../../types/market'
import { transactionTypeLabels } from './transactionLabels'
import type { PlayerOption } from './useTransactionHistory'

const TYPES: TransactionType[] = ['COMPRA', 'VENDA']

interface TransactionFilterBarProps {
  filters: TransactionFilters
  players: PlayerOption[]
  onChange: (filters: TransactionFilters) => void
}

/** One or more types and a player among those in the club history, applied on change. */
export function TransactionFilterBar({ filters, players, onChange }: TransactionFilterBarProps) {
  function toggleType(type: TransactionType, checked: boolean) {
    const types = checked
      ? TYPES.filter((item) => item === type || filters.types.includes(item))
      : filters.types.filter((item) => item !== type)
    onChange({ ...filters, types })
  }

  return (
    <div className="flex flex-wrap items-end gap-4">
      <fieldset className="flex gap-4 pb-2">
        <legend className="mb-1 text-sm font-medium text-slate-700">Tipo</legend>
        {TYPES.map((type) => (
          <label key={type} className="flex items-center gap-2 text-sm">
            <input
              type="checkbox"
              className="size-4"
              checked={filters.types.includes(type)}
              onChange={(event) => toggleType(type, event.target.checked)}
            />
            {transactionTypeLabels[type]}
          </label>
        ))}
      </fieldset>
      <div className="w-full sm:w-64">
        <FormField label="Jogador" htmlFor="history-player">
          <Select
            id="history-player"
            placeholder="Todos"
            value={filters.player_id}
            options={players.map((player) => ({ value: String(player.id), label: player.name }))}
            onChange={(event) => onChange({ ...filters, player_id: event.target.value })}
          />
        </FormField>
      </div>
    </div>
  )
}
