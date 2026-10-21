import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import { jsonResponse, mockApi } from '../test/mockApi'
import type { Condition } from '../types/player'
import { aPage, aPlayer } from '../test/playerFixtures'
import { PlayersPage } from './PlayersPage'

function renderPage() {
  render(<PlayersPage />)
  return userEvent.setup()
}

function rowOf(name: string) {
  return screen.getByRole('row', { name: new RegExp(name) })
}

describe('PlayersPage', () => {
  it('lists the players of the API with total, "Livre" and 10 per page', async () => {
    const requested: string[] = []
    mockApi({
      'GET /players': (url) => {
        requested.push(url.search)
        const page = Number(url.searchParams.get('page'))
        return jsonResponse(
          200,
          aPage([aPlayer({ id: page, name: `Jogador ${page === 1 ? 'Um' : 'Dois'}` })], page, 11),
        )
      },
    })
    const user = renderPage()

    expect(await screen.findByText('Jogador Um')).toBeInTheDocument()
    expect(screen.getByText('11 jogador(es) encontrado(s)')).toBeInTheDocument()
    expect(within(rowOf('Jogador Um')).getByText('Livre')).toBeInTheDocument()
    expect(within(rowOf('Jogador Um')).getAllByText('40,00')).toHaveLength(2)

    await user.click(screen.getByRole('button', { name: 'Próxima' }))

    expect(await screen.findByText('Jogador Dois')).toBeInTheDocument()
    expect(screen.getByText('Página 2 de 2')).toBeInTheDocument()
    expect(requested).toEqual(['?page=1&limit=10', '?page=2&limit=10'])
  })

  it('shows the error state and loads again on retry', async () => {
    let calls = 0
    mockApi({
      'GET /players': () => {
        calls += 1
        if (calls === 1) {
          throw new TypeError('Failed to fetch')
        }
        return jsonResponse(200, aPage([]))
      },
    })
    const user = renderPage()

    expect(await screen.findByRole('alert')).toHaveTextContent('Não foi possível conectar à API.')
    await user.click(screen.getByRole('button', { name: 'Tentar novamente' }))

    expect(await screen.findByText('Nenhum jogador encontrado.')).toBeInTheDocument()
  })

  it('shows the values computed by the API after registering and reloads the list', async () => {
    const created = aPlayer({ id: 7, name: 'Davi Lins', base_value: 38.08, market_value: 41.89 })
    let registered = false
    mockApi({
      'GET /players': () => jsonResponse(200, aPage(registered ? [created] : [])),
      'POST /players': () => {
        registered = true
        return jsonResponse(201, created)
      },
    })
    const user = renderPage()
    await screen.findByText('Nenhum jogador encontrado.')

    await user.click(screen.getByRole('button', { name: 'Novo jogador' }))
    const values: Record<string, string> = {
      Nome: 'Davi Lins',
      'Agilidade (1–100)': '60',
      'Idade (16–45)': '19',
      Velocidade: '72',
      Finalização: '70',
      Passe: '78',
      Drible: '76',
      Defesa: '70',
      Físico: '66',
      Potencial: '84',
    }
    for (const [label, value] of Object.entries(values)) {
      await user.type(screen.getByLabelText(label), value)
    }
    await user.selectOptions(screen.getByLabelText('Posição'), 'LAT')
    await user.click(screen.getByRole('button', { name: 'Cadastrar jogador' }))

    expect(await screen.findByRole('status')).toHaveTextContent(
      'Jogador Davi Lins cadastrado com sucesso.',
    )
    const summary = screen.getByRole('region', { name: 'Resumo de Davi Lins' })
    expect(summary).toHaveTextContent('Valor base38,08')
    expect(summary).toHaveTextContent('Valor de mercado41,89')
    expect(await screen.findByRole('row', { name: /Davi Lins/ })).toBeInTheDocument()
  })

  it('edits a player and shows the API message when the edition is refused', async () => {
    let attempts = 0
    mockApi({
      'GET /players': () => jsonResponse(200, aPage([aPlayer()])),
      'PUT /players/2': (_url, init) => {
        attempts += 1
        if (attempts === 1) {
          return jsonResponse(409, { message: 'A idade não pode ser menor que a atual (25 anos).' })
        }
        return jsonResponse(200, { ...aPlayer(), ...JSON.parse(init?.body as string) })
      },
    })
    const user = renderPage()
    await user.click(await screen.findByRole('button', { name: 'Editar Meia Lua' }))

    const dialog = screen.getByRole('dialog', { name: 'Editar Meia Lua' })
    await user.click(within(dialog).getByRole('button', { name: 'Salvar alterações' }))
    expect(await within(dialog).findByRole('alert')).toHaveTextContent(
      'A idade não pode ser menor que a atual (25 anos).',
    )

    await user.clear(within(dialog).getByLabelText('Nome'))
    await user.type(within(dialog).getByLabelText('Nome'), 'Meia Zeta')
    await user.click(within(dialog).getByRole('button', { name: 'Salvar alterações' }))

    expect(await screen.findByText('Jogador Meia Zeta atualizado.')).toBeInTheDocument()
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })

  it('changes the condition of a player', async () => {
    let condition: Condition = 'APTO'
    mockApi({
      'GET /players': () => jsonResponse(200, aPage([aPlayer({ condition })])),
      'PATCH /players/2/condition': (_url, init) => {
        condition = JSON.parse(init?.body as string).condition
        return jsonResponse(200, aPlayer({ condition }))
      },
    })
    const user = renderPage()
    await user.click(await screen.findByRole('button', { name: 'Alterar condição de Meia Lua' }))

    await user.selectOptions(screen.getByLabelText('Condição'), 'DOENTE')
    await user.click(screen.getByRole('button', { name: 'Salvar condição' }))

    expect(await screen.findByText('Condição de Meia Lua atualizada.')).toBeInTheDocument()
    expect(await within(rowOf('Meia Lua')).findByText('Doente')).toBeInTheDocument()
    expect(condition).toBe('DOENTE')
  })

  it('deletes only free players, after confirmation', async () => {
    let deleted = false
    mockApi({
      'GET /players': () =>
        jsonResponse(
          200,
          aPage(
            deleted
              ? [aPlayer({ id: 3, name: 'Otto Rangel', club_id: 1 })]
              : [aPlayer(), aPlayer({ id: 3, name: 'Otto Rangel', club_id: 1 })],
          ),
        ),
      'DELETE /players/2': () => {
        deleted = true
        return new Response(null, { status: 204 })
      },
    })
    const user = renderPage()
    await screen.findByText('Meia Lua')

    expect(screen.queryByRole('button', { name: 'Excluir Otto Rangel' })).not.toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Excluir Meia Lua' }))
    const dialog = screen.getByRole('dialog', { name: 'Excluir jogador' })
    await user.click(within(dialog).getByRole('button', { name: 'Excluir' }))

    expect(await screen.findByText('Jogador Meia Lua excluído.')).toBeInTheDocument()
    expect(screen.queryByRole('row', { name: /Meia Lua/ })).not.toBeInTheDocument()
    expect(screen.getByRole('row', { name: /Otto Rangel/ })).toBeInTheDocument()
  })
})
