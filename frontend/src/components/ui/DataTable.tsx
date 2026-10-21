import type { ReactNode } from 'react'
import { Button } from './Button'

export interface Column<T> {
  header: string
  render: (row: T) => ReactNode
}

interface DataTableProps<T> {
  caption: string
  columns: Column<T>[]
  rows: T[] | null
  getRowKey: (row: T) => string | number
  isLoading: boolean
  error: string | null
  emptyMessage: string
  onRetry?: () => void
}

/** Shared table of every list screen, with the loading, empty and error states. */
export function DataTable<T>({
  caption,
  columns,
  rows,
  getRowKey,
  isLoading,
  error,
  emptyMessage,
  onRetry,
}: DataTableProps<T>) {
  function renderState(content: ReactNode, role?: 'status' | 'alert') {
    return (
      <tr>
        <td colSpan={columns.length} className="px-4 py-10 text-center text-slate-600">
          <div role={role} className="flex flex-col items-center gap-3">
            {content}
          </div>
        </td>
      </tr>
    )
  }

  function renderBody() {
    if (isLoading) {
      return renderState('Carregando…', 'status')
    }
    if (error) {
      return renderState(
        <>
          <span className="text-red-700">{error}</span>
          {onRetry && (
            <Button variant="secondary" onClick={onRetry}>
              Tentar novamente
            </Button>
          )}
        </>,
        'alert',
      )
    }
    if (!rows || rows.length === 0) {
      return renderState(emptyMessage)
    }
    return rows.map((row) => (
      <tr key={getRowKey(row)} className="border-t border-slate-100 hover:bg-slate-50">
        {columns.map((column) => (
          <td key={column.header} className="px-3 py-2 text-center whitespace-nowrap">
            {column.render(row)}
          </td>
        ))}
      </tr>
    ))
  }

  return (
    <div className="overflow-x-auto rounded-xl border border-slate-200">
      <table className="w-full text-sm" aria-busy={isLoading}>
        <caption className="sr-only">{caption}</caption>
        <thead className="bg-slate-50 text-xs text-slate-500 uppercase">
          <tr>
            {columns.map((column) => (
              <th
                key={column.header}
                scope="col"
                className="px-3 py-2 text-center font-medium whitespace-nowrap"
              >
                {column.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>{renderBody()}</tbody>
      </table>
    </div>
  )
}
