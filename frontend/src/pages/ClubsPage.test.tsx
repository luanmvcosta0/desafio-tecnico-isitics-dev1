import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import { jsonResponse, mockApi } from '../test/mockApi'
import type { Club } from '../types/club'
import { ClubsPage } from './ClubsPage'

const aguias: Club = {
  id: 1,
  name: 'Águias FC',
  acronym: 'AGU',
  cash: 261.15,
  squad_value: 1234.5,
  net_worth: 1495.65,
}
const delta: Club = {
  id: 2,
  name: 'Delta FC',
  acronym: 'DFC',
  cash: 300,
  squad_value: 0,
  net_worth: 300,
}

function namesInTable() {
  return screen
    .getAllByRole('row')
    .slice(1)
    .map((row) => row.querySelector('td')?.textContent)
}

describe('ClubsPage', () => {
  it('lists the clubs in the API order and sorts by net worth through the API', async () => {
    const sorts: (string | null)[] = []
    mockApi({
      'GET /clubs': (url) => {
        sorts.push(url.searchParams.get('sort'))
        return jsonResponse(
          200,
          url.searchParams.get('sort') === 'name' ? [aguias, delta] : [aguias, delta].reverse(),
        )
      },
    })
    const user = userEvent.setup()
    render(<ClubsPage />)

    expect(await screen.findByText('Águias FC')).toBeInTheDocument()
    expect(screen.getByText('261,15')).toBeInTheDocument()
    expect(screen.getByText('1.234,50')).toBeInTheDocument()
    expect(screen.getByText('1.495,65')).toBeInTheDocument()

    await user.selectOptions(screen.getByLabelText('Ordenar por'), 'net_worth_asc')

    await screen.findByText('Delta FC')
    expect(namesInTable()).toEqual(['Delta FC', 'Águias FC'])
    expect(sorts).toEqual(['name', 'net_worth_asc'])
  })

  it('shows the empty state', async () => {
    mockApi({ 'GET /clubs': () => jsonResponse(200, []) })
    render(<ClubsPage />)

    expect(await screen.findByText('Nenhum clube cadastrado.')).toBeInTheDocument()
  })

  it('validates the fields before sending and shows the API message on refusal', async () => {
    let created = false
    const fetchSpy = mockApi({
      'GET /clubs': () => jsonResponse(200, created ? [aguias] : []),
      'POST /clubs': (_url, init) => {
        const body = JSON.parse(init?.body as string)
        if (body.acronym === 'AGU' && created) {
          return jsonResponse(409, { message: 'Já existe um clube com a sigla AGU.' })
        }
        created = true
        return jsonResponse(201, aguias)
      },
    })
    const user = userEvent.setup()
    render(<ClubsPage />)
    await screen.findByText('Nenhum clube cadastrado.')

    await user.click(screen.getByRole('button', { name: 'Novo clube' }))
    await user.type(screen.getByLabelText('Nome'), 'Ág')
    await user.type(screen.getByLabelText('Sigla'), 'agu')
    await user.click(screen.getByRole('button', { name: 'Criar clube' }))

    expect(screen.getByLabelText('Nome')).toHaveAccessibleDescription(
      'O nome deve ter pelo menos 3 caracteres.',
    )
    expect(screen.getByLabelText('Sigla')).toHaveAccessibleDescription(
      'A sigla deve ter 3 letras maiúsculas (A–Z).',
    )
    expect(fetchSpy).toHaveBeenCalledTimes(1)

    await user.clear(screen.getByLabelText('Nome'))
    await user.type(screen.getByLabelText('Nome'), 'Águias FC')
    await user.clear(screen.getByLabelText('Sigla'))
    await user.type(screen.getByLabelText('Sigla'), 'AGU')
    await user.click(screen.getByRole('button', { name: 'Criar clube' }))

    expect(await screen.findByRole('status')).toHaveTextContent('Clube Águias FC criado')
    expect(await screen.findByText('Águias FC')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Novo clube' }))
    await user.type(screen.getByLabelText('Nome'), 'Águias Clube')
    await user.type(screen.getByLabelText('Sigla'), 'AGU')
    await user.click(screen.getByRole('button', { name: 'Criar clube' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Já existe um clube com a sigla AGU.',
    )
  })
})
