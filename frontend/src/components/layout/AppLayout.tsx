import { Link, NavLink, Outlet } from 'react-router'
import { routes } from '../../app/routes'

const navigationItems = [
  { label: 'Jogadores', to: routes.players },
  { label: 'Clubes', to: routes.clubs },
  { label: 'Partidas', to: routes.matches },
  { label: 'Classificação', to: routes.standings },
]

function navLinkClassName({ isActive }: { isActive: boolean }): string {
  const base = 'rounded-md px-3 py-2 text-sm font-medium transition-colors'
  return isActive
    ? `${base} bg-brand-50 text-brand-700`
    : `${base} text-slate-600 hover:bg-slate-100 hover:text-slate-900`
}

export function AppLayout() {
  return (
    <div className="flex min-h-screen flex-col">
      <header className="border-b border-slate-200">
        <div className="mx-auto flex max-w-6xl flex-wrap items-center gap-4 px-4 py-3">
          <Link to={routes.home} className="flex items-center gap-2 text-lg font-bold">
            <span className="bg-brand-600 flex size-8 items-center justify-center rounded-lg text-white">
              L
            </span>
            LigaHub
          </Link>
          <nav aria-label="Navegação principal" className="flex flex-wrap gap-1">
            {navigationItems.map((item) => (
              <NavLink key={item.to} to={item.to} className={navLinkClassName}>
                {item.label}
              </NavLink>
            ))}
          </nav>
        </div>
      </header>
      <main className="mx-auto w-full max-w-6xl flex-1 px-4 py-6">
        <Outlet />
      </main>
      <footer className="border-t border-slate-200 py-4 text-center text-xs text-slate-500">
        Liga Fictícia · Horário de Brasília (UTC−3)
      </footer>
    </div>
  )
}
