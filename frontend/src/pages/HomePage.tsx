import { Link } from 'react-router'
import { routes } from '../app/routes'

const flowSteps = [
  {
    title: 'Jogadores',
    description: 'Nascem livres e APTO; o valor de mercado vem da condição, agilidade e idade.',
    to: routes.players,
  },
  {
    title: 'Clubes',
    description: 'Começam com 300,00 em caixa e sem jogadores.',
    to: routes.clubs,
  },
  {
    title: 'Compra',
    description: 'Valor + taxa, até 50% do caixa, com contrato de fidelidade.',
    to: routes.players,
  },
  {
    title: 'Venda',
    description: 'Multa antes do fim da fidelidade e comissão sobre o lucro.',
    to: routes.clubs,
  },
  {
    title: 'Histórico',
    description: 'Toda compra e venda fica registrada, com totais e CSV.',
    to: routes.clubs,
  },
  {
    title: 'Liga',
    description: 'Escale 5 titulares e jogue partidas que entram na classificação.',
    to: routes.matches,
  },
]

export function HomePage() {
  return (
    <section className="flex flex-col items-center gap-8 py-8 text-center">
      <div className="max-w-2xl">
        <h1 className="text-3xl font-bold tracking-tight sm:text-4xl">
          Gestão da liga, do mercado à classificação
        </h1>
        <p className="mt-3 text-slate-600">
          Cadastre jogadores, monte os clubes e negocie com contrato de fidelidade. Depois, escale
          os titulares e jogue partidas com placar calculado pelos atributos.
        </p>
      </div>
      <Link
        to={routes.players}
        className="bg-brand-600 hover:bg-brand-700 rounded-lg px-8 py-3 font-medium text-white"
      >
        Iniciar
      </Link>
      <ol className="grid w-full gap-4 text-left sm:grid-cols-2 lg:grid-cols-3">
        {flowSteps.map((step, index) => (
          <li key={step.title}>
            <Link
              to={step.to}
              className="hover:border-brand-500 block h-full rounded-xl border border-slate-200 p-4 transition-colors"
            >
              <span className="font-mono text-xs text-slate-500">
                {String(index + 1).padStart(2, '0')}
              </span>
              <h2 className="mt-1 font-semibold">{step.title}</h2>
              <p className="mt-1 text-sm text-slate-600">{step.description}</p>
            </Link>
          </li>
        ))}
      </ol>
    </section>
  )
}
