import { useState, type FormEvent } from 'react'
import { Button } from '../../components/ui/Button'
import { FormField } from '../../components/ui/FormField'
import { Select } from '../../components/ui/Select'
import { TextInput } from '../../components/ui/TextInput'
import type { Club } from '../../types/club'
import { CONDITIONS, POSITIONS, type PlayerFilters, type PlayerSort } from '../../types/player'
import { EMPTY_PLAYER_FILTERS } from './playerFilterDefaults'
import { conditionLabels, positionLabels } from './playerLabels'

const sortOptions: { value: PlayerSort; label: string }[] = [
  { value: 'name', label: 'Nome (A–Z)' },
  { value: 'value_desc', label: 'Valor de mercado (maior primeiro)' },
  { value: 'value_asc', label: 'Valor de mercado (menor primeiro)' },
  { value: 'age_asc', label: 'Idade (menor primeiro)' },
  { value: 'age_desc', label: 'Idade (maior primeiro)' },
  { value: 'position', label: 'Posição (GOL → ATA)' },
]

const positionOptions = POSITIONS.map((position) => ({
  value: position,
  label: `${position} · ${positionLabels[position]}`,
}))

const conditionOptions = CONDITIONS.map((condition) => ({
  value: condition,
  label: conditionLabels[condition],
}))

interface PlayerFilterFormProps {
  clubs: Club[]
  onApply: (filters: PlayerFilters) => void
}

/** Search, combinable filters and sort of the players list, applied on submit. */
export function PlayerFilterForm({ clubs, onApply }: PlayerFilterFormProps) {
  const [draft, setDraft] = useState<PlayerFilters>(EMPTY_PLAYER_FILTERS)
  const clubOptions = clubs.map((club) => ({
    value: String(club.id),
    label: `${club.acronym} · ${club.name}`,
  }))

  function update<K extends keyof PlayerFilters>(key: K, value: PlayerFilters[K]) {
    setDraft((current) => ({ ...current, [key]: value }))
  }

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    onApply(draft)
  }

  function clear() {
    setDraft(EMPTY_PLAYER_FILTERS)
    onApply(EMPTY_PLAYER_FILTERS)
  }

  return (
    <form
      aria-label="Filtros de jogadores"
      onSubmit={handleSubmit}
      className="grid gap-3 rounded-xl border border-slate-200 p-4 sm:grid-cols-2 lg:grid-cols-4"
    >
      <div className="sm:col-span-2">
        <FormField label="Buscar por nome" htmlFor="filter-search">
          <TextInput
            id="filter-search"
            type="search"
            value={draft.search}
            onChange={(event) => update('search', event.target.value)}
          />
        </FormField>
      </div>
      <FormField label="Posição" htmlFor="filter-position">
        <Select
          id="filter-position"
          placeholder="Todas"
          options={positionOptions}
          value={draft.position}
          onChange={(event) => update('position', event.target.value as PlayerFilters['position'])}
        />
      </FormField>
      <FormField label="Condição" htmlFor="filter-condition">
        <Select
          id="filter-condition"
          placeholder="Todas"
          options={conditionOptions}
          value={draft.condition}
          onChange={(event) =>
            update('condition', event.target.value as PlayerFilters['condition'])
          }
        />
      </FormField>
      <FormField
        label="Clube"
        htmlFor="filter-club"
        error={draft.free ? 'Desmarque "Só livres" para filtrar por clube.' : undefined}
      >
        <Select
          id="filter-club"
          placeholder="Todos"
          options={clubOptions}
          value={draft.club_id}
          disabled={draft.free}
          onChange={(event) => update('club_id', event.target.value)}
        />
      </FormField>
      <label className="flex items-center gap-2 self-end pb-2 text-sm font-medium text-slate-700">
        <input
          type="checkbox"
          checked={draft.free}
          onChange={(event) =>
            setDraft((current) => ({
              ...current,
              free: event.target.checked,
              club_id: event.target.checked ? '' : current.club_id,
            }))
          }
          className="size-4"
        />
        Só livres
      </label>
      <div className="grid grid-cols-2 gap-3">
        <FormField label="Idade mínima" htmlFor="filter-min-age">
          <TextInput
            id="filter-min-age"
            type="number"
            inputMode="numeric"
            value={draft.min_age}
            onChange={(event) => update('min_age', event.target.value)}
          />
        </FormField>
        <FormField label="Idade máxima" htmlFor="filter-max-age">
          <TextInput
            id="filter-max-age"
            type="number"
            inputMode="numeric"
            value={draft.max_age}
            onChange={(event) => update('max_age', event.target.value)}
          />
        </FormField>
      </div>
      <div className="grid grid-cols-2 gap-3">
        <FormField label="Valor mínimo" htmlFor="filter-min-value">
          <TextInput
            id="filter-min-value"
            type="number"
            inputMode="decimal"
            step="0.01"
            value={draft.min_value}
            onChange={(event) => update('min_value', event.target.value)}
          />
        </FormField>
        <FormField label="Valor máximo" htmlFor="filter-max-value">
          <TextInput
            id="filter-max-value"
            type="number"
            inputMode="decimal"
            step="0.01"
            value={draft.max_value}
            onChange={(event) => update('max_value', event.target.value)}
          />
        </FormField>
      </div>
      <FormField label="Ordenar por" htmlFor="filter-sort">
        <Select
          id="filter-sort"
          options={sortOptions}
          value={draft.sort}
          onChange={(event) => update('sort', event.target.value as PlayerSort)}
        />
      </FormField>
      <div className="flex items-end justify-end gap-2 sm:col-span-2 lg:col-span-4">
        <Button variant="secondary" onClick={clear}>
          Limpar
        </Button>
        <Button type="submit">Aplicar filtros</Button>
      </div>
    </form>
  )
}
