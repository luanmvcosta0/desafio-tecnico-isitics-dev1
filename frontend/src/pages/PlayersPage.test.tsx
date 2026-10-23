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
      'GET /clubs': () => jsonResponse(200, []),
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
    expect(requested).toEqual(['?sort=name&page=1&limit=10', '?sort=name&page=2&limit=10'])
  })

  it('shows the error state and loads again on retry', async () => {
    let calls = 0
    mockApi({
      'GET /clubs': () => jsonResponse(200, []),
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
      'GET /clubs': () => jsonResponse(200, []),
      'GET /players': () => jsonResponse(200, aPage(registered ? [created] : [])),
      'POST /players': () => {
        registered = true
        return jsonResponse(201, created)
      },
    })
    const user = renderPage()
    await screen.findByText('Nenhum jogador encontrado.')

    await user.click(screen.getByRole('button', { name: 'Novo jogador' }))
    const form = within(screen.getByRole('form', { name: 'Cadastro de jogador' }))
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
      await user.type(form.getByLabelText(label), value)
    }
    await user.selectOptions(form.getByLabelText('Posição'), 'LAT')
    await user.click(form.getByRole('button', { name: 'Cadastrar jogador' }))

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
      'GET /clubs': () => jsonResponse(200, []),
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
      'GET /clubs': () => jsonResponse(200, []),
      'GET /players': () => jsonResponse(200, aPage([aPlayer({ condition })])),
      'PATCH /players/2/condition': (_url, init) => {
        condition = JSON.parse(init?.body as string).condition
        return jsonResponse(200, aPlayer({ condition }))
      },
    })
    const user = renderPage()
    await user.click(await screen.findByRole('button', { name: 'Alterar condição de Meia Lua' }))

    const dialog = within(screen.getByRole('dialog', { name: 'Condição de Meia Lua' }))
    await user.selectOptions(dialog.getByLabelText('Condição'), 'DOENTE')
    await user.click(dialog.getByRole('button', { name: 'Salvar condição' }))

    expect(await screen.findByText('Condição de Meia Lua atualizada.')).toBeInTheDocument()
    expect(await within(rowOf('Meia Lua')).findByText('Doente')).toBeInTheDocument()
    expect(condition).toBe('DOENTE')
  })

  it('goes back to the last page when an action empties the current one', async () => {
    let condition: Condition = 'APTO'
    mockApi({
      'GET /clubs': () => jsonResponse(200, []),
      'GET /players': (url) => {
        const page = Number(url.searchParams.get('page'))
        const isSick = condition === 'DOENTE'
        if (page === 1) {
          return jsonResponse(
            200,
            aPage([aPlayer({ id: 1, name: 'Jogador Um' })], 1, isSick ? 10 : 11),
          )
        }
        return jsonResponse(200, aPage(isSick ? [] : [aPlayer({ condition })], 2, isSick ? 10 : 11))
      },
      'PATCH /players/2/condition': () => {
        condition = 'DOENTE'
        return jsonResponse(200, aPlayer({ condition }))
      },
    })
    const user = renderPage()
    await screen.findByText('Jogador Um')
    await user.click(screen.getByRole('button', { name: 'Próxima' }))
    await user.click(await screen.findByRole('button', { name: 'Alterar condição de Meia Lua' }))

    const dialog = within(screen.getByRole('dialog', { name: 'Condição de Meia Lua' }))
    await user.selectOptions(dialog.getByLabelText('Condição'), 'DOENTE')
    await user.click(dialog.getByRole('button', { name: 'Salvar condição' }))

    expect(await screen.findByText('Página 1 de 1')).toBeInTheDocument()
    expect(await screen.findByText('Jogador Um')).toBeInTheDocument()
  })

  it('deletes only free players, after confirmation', async () => {
    let deleted = false
    mockApi({
      'GET /clubs': () => jsonResponse(200, []),
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

  it('sends the combined filters and blocks the club filter with "Só livres"', async () => {
    const requested: URLSearchParams[] = []
    mockApi({
      'GET /clubs': () =>
        jsonResponse(200, [
          { id: 1, name: 'Águias FC', acronym: 'AGU', cash: 300, squad_value: 0, net_worth: 300 },
        ]),
      'GET /players': (url) => {
        requested.push(url.searchParams)
        return jsonResponse(200, aPage([aPlayer({ club_id: 1 })]))
      },
    })
    const user = renderPage()
    expect(
      await within(await screen.findByRole('row', { name: /Meia Lua/ })).findByText('AGU'),
    ).toBeInTheDocument()

    await user.type(screen.getByLabelText('Buscar por nome'), 'joao')
    await user.selectOptions(screen.getByLabelText('Posição'), 'MEI')
    await user.selectOptions(screen.getByLabelText('Clube'), '1')
    await user.type(screen.getByLabelText('Idade mínima'), '20')
    await user.type(screen.getByLabelText('Valor máximo'), '50.5')
    await user.selectOptions(screen.getByLabelText('Ordenar por'), 'value_desc')
    await user.click(screen.getByRole('button', { name: 'Aplicar filtros' }))

    await screen.findByText('Meia Lua')
    expect(Object.fromEntries(requested.at(-1)!)).toEqual({
      search: 'joao',
      position: 'MEI',
      club_id: '1',
      min_age: '20',
      max_value: '50.5',
      sort: 'value_desc',
      page: '1',
      limit: '10',
    })

    await user.click(screen.getByLabelText('Só livres'))
    expect(screen.getByLabelText('Clube')).toBeDisabled()
    await user.click(screen.getByRole('button', { name: 'Aplicar filtros' }))
    await screen.findByText('Meia Lua')
    expect(requested.at(-1)!.get('free')).toBe('true')
    expect(requested.at(-1)!.has('club_id')).toBe(false)
  })

  it('shows the API message when a filter is refused', async () => {
    mockApi({
      'GET /clubs': () => jsonResponse(200, []),
      'GET /players': (url) =>
        url.searchParams.has('min_age')
          ? jsonResponse(400, { message: 'O parâmetro min_age não pode ser maior que max_age.' })
          : jsonResponse(200, aPage([aPlayer()])),
    })
    const user = renderPage()
    await screen.findByText('Meia Lua')

    await user.type(screen.getByLabelText('Idade mínima'), '40')
    await user.type(screen.getByLabelText('Idade máxima'), '20')
    await user.click(screen.getByRole('button', { name: 'Aplicar filtros' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'O parâmetro min_age não pode ser maior que max_age.',
    )
  })

  it('buys a free player and shows the club in the table afterwards', async () => {
    let bought = false
    mockApi({
      'GET /clubs': () =>
        jsonResponse(200, [
          { id: 1, name: 'Águias FC', acronym: 'AGU', cash: 300, squad_value: 0, net_worth: 300 },
        ]),
      'GET /players': () => jsonResponse(200, aPage([aPlayer({ club_id: bought ? 1 : null })])),
      'POST /players/2/buy': () => {
        bought = true
        return jsonResponse(200, {
          id: 1,
          club_id: 1,
          player_id: 2,
          player_name: 'Meia Lua',
          type: 'COMPRA',
          value: 40,
          fee: 2,
          penalty: 0,
          profit: null,
          cash_change: -42,
          created_at: '2026-11-02T10:15:00-03:00',
          contract: {
            purchase_value: 40,
            loyalty_until: '2027-03-02T10:15:00-03:00',
            status: 'ATIVO',
          },
          club_cash: 258,
        })
      },
    })
    const user = renderPage()
    await user.click(await screen.findByRole('button', { name: 'Comprar Meia Lua' }))

    const dialog = within(screen.getByRole('dialog', { name: 'Comprar Meia Lua' }))
    await user.selectOptions(dialog.getByLabelText('Clube comprador'), '1')
    await user.type(
      dialog.getByLabelText('Fim da fidelidade (horário de Brasília)'),
      '2027-03-02T10:15',
    )
    await user.click(dialog.getByRole('button', { name: 'Confirmar compra' }))
    await user.click(await dialog.findByRole('button', { name: 'Fechar' }))

    expect(await within(rowOf('Meia Lua')).findByText('AGU')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Comprar Meia Lua' })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Excluir Meia Lua' })).not.toBeInTheDocument()
  })
})
