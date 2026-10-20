import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import { describe, expect, it } from 'vitest'
import { AppRoutes } from './AppRoutes'

function renderAt(path: string) {
  render(
    <MemoryRouter initialEntries={[path]}>
      <AppRoutes />
    </MemoryRouter>,
  )
}

describe('AppRoutes', () => {
  it('renders the players page inside the layout', () => {
    renderAt('/players')

    expect(screen.getByRole('heading', { level: 1, name: 'Jogadores' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Jogadores' })).toHaveAttribute('aria-current', 'page')
  })

  it('renders the not found page for unknown paths', () => {
    renderAt('/inexistente')

    expect(screen.getByRole('heading', { name: 'Página não encontrada' })).toBeInTheDocument()
  })
})
