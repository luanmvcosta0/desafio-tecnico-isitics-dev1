import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import type { Player } from '../../types/player'
import { PlayerCreateForm } from './PlayerCreateForm'

const createdPlayer: Player = {
  id: 2,
  name: 'Meia Lua',
  position: 'MEI',
  agility: 85,
  age: 25,
  condition: 'APTO',
  base_value: 40,
  market_value: 40,
  pace: 70,
  shooting: 68,
  passing: 82,
  dribbling: 79,
  defending: 55,
  physical: 64,
  potential: 86,
  overall: 75,
  club_id: null,
}

const validValues: Record<string, string> = {
  Nome: 'Meia Lua',
  'Agilidade (1–100)': '85',
  'Idade (16–45)': '25',
  Velocidade: '70',
  Finalização: '68',
  Passe: '82',
  Drible: '79',
  Defesa: '55',
  Físico: '64',
  Potencial: '86',
}

function jsonResponse(status: number, body: unknown): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })
}

function renderForm() {
  const onCreated = vi.fn()
  render(<PlayerCreateForm onCreated={onCreated} onCancel={vi.fn()} />)
  return { onCreated, user: userEvent.setup() }
}

async function fillValidForm(user: ReturnType<typeof userEvent.setup>) {
  for (const [label, value] of Object.entries(validValues)) {
    await user.type(screen.getByLabelText(label), value)
  }
  await user.selectOptions(screen.getByLabelText('Posição'), 'MEI')
}

describe('PlayerCreateForm', () => {
  it('shows the messages next to the fields without calling the API', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch')
    const { user } = renderForm()

    await user.type(screen.getByLabelText('Nome'), 'Lu')
    await user.type(screen.getByLabelText('Idade (16–45)'), '15')
    await user.click(screen.getByRole('button', { name: 'Cadastrar jogador' }))

    expect(screen.getByLabelText('Nome')).toHaveAccessibleDescription(
      'O nome deve ter pelo menos 3 caracteres.',
    )
    expect(screen.getByLabelText('Idade (16–45)')).toHaveAccessibleDescription(
      'Informe um número inteiro de 16 a 45.',
    )
    expect(screen.getByLabelText('Posição')).toHaveAccessibleDescription('Selecione a posição.')
    expect(screen.getByLabelText('Potencial')).toHaveAccessibleDescription('Campo obrigatório.')
    expect(fetchSpy).not.toHaveBeenCalled()
  })

  it('sends the numbers as JSON numbers and returns the created player', async () => {
    const fetchSpy = vi
      .spyOn(globalThis, 'fetch')
      .mockResolvedValue(jsonResponse(201, createdPlayer))
    const { user, onCreated } = renderForm()

    await fillValidForm(user)
    await user.click(screen.getByRole('button', { name: 'Cadastrar jogador' }))

    const [url, init] = fetchSpy.mock.calls[0]
    expect(url).toBe('http://localhost:8080/api/v1/players')
    expect(JSON.parse(init?.body as string)).toEqual({
      name: 'Meia Lua',
      position: 'MEI',
      agility: 85,
      age: 25,
      pace: 70,
      shooting: 68,
      passing: 82,
      dribbling: 79,
      defending: 55,
      physical: 64,
      potential: 86,
    })
    expect(onCreated).toHaveBeenCalledWith(createdPlayer)
  })

  it('shows the message returned by the API when the registration is refused', async () => {
    const message = 'A soma dos seis indicadores técnicos deve ser no máximo 480.'
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse(400, { message }))
    const { user, onCreated } = renderForm()

    await fillValidForm(user)
    await user.click(screen.getByRole('button', { name: 'Cadastrar jogador' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(message)
    expect(onCreated).not.toHaveBeenCalled()
  })

  it('disables the button and sends only once while the request is pending', async () => {
    let resolveResponse: (response: Response) => void = () => {}
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockReturnValue(
      new Promise<Response>((resolve) => {
        resolveResponse = resolve
      }),
    )
    const { user } = renderForm()

    await fillValidForm(user)
    const button = screen.getByRole('button', { name: 'Cadastrar jogador' })
    await user.dblClick(button)

    expect(screen.getByRole('button', { name: 'Salvando…' })).toBeDisabled()
    resolveResponse(jsonResponse(201, createdPlayer))
    expect(await screen.findByRole('button', { name: 'Cadastrar jogador' })).toBeEnabled()
    expect(fetchSpy).toHaveBeenCalledTimes(1)
  })
})
