import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { DataTable, type Column } from './DataTable'

interface Row {
  id: number
  name: string
}

const columns: Column<Row>[] = [{ header: 'Nome', render: (row) => row.name }]

function renderTable(props: Partial<Parameters<typeof DataTable<Row>>[0]>) {
  render(
    <DataTable<Row>
      caption="Teste"
      columns={columns}
      rows={null}
      getRowKey={(row) => row.id}
      isLoading={false}
      error={null}
      emptyMessage="Nada por aqui."
      {...props}
    />,
  )
}

describe('DataTable', () => {
  it('shows the loading state', () => {
    renderTable({ isLoading: true })

    expect(screen.getByRole('status')).toHaveTextContent('Carregando…')
  })

  it('shows the empty state', () => {
    renderTable({ rows: [] })

    expect(screen.getByText('Nada por aqui.')).toBeInTheDocument()
  })

  it('shows the error with a retry button', async () => {
    const onRetry = vi.fn()
    renderTable({ error: 'Não foi possível conectar à API.', onRetry })

    expect(screen.getByRole('alert')).toHaveTextContent('Não foi possível conectar à API.')
    await userEvent.click(screen.getByRole('button', { name: 'Tentar novamente' }))
    expect(onRetry).toHaveBeenCalledOnce()
  })

  it('renders the rows in the given order', () => {
    renderTable({
      rows: [
        { id: 2, name: 'Zeta' },
        { id: 1, name: 'Alfa' },
      ],
    })

    const cells = screen.getAllByRole('cell').map((cell) => cell.textContent)
    expect(cells).toEqual(['Zeta', 'Alfa'])
  })
})
