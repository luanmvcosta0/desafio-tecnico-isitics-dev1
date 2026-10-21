import { Button } from './Button'

interface PaginationProps {
  page: number
  totalPages: number
  onPageChange: (page: number) => void
}

export function Pagination({ page, totalPages, onPageChange }: PaginationProps) {
  const lastPage = Math.max(totalPages, 1)
  return (
    <nav aria-label="Paginação" className="flex items-center justify-end gap-2 text-sm">
      <Button variant="secondary" onClick={() => onPageChange(page - 1)} disabled={page <= 1}>
        Anterior
      </Button>
      <span className="px-2 text-slate-600">
        Página {page} de {lastPage}
      </span>
      <Button
        variant="secondary"
        onClick={() => onPageChange(page + 1)}
        disabled={page >= lastPage}
      >
        Próxima
      </Button>
    </nav>
  )
}
