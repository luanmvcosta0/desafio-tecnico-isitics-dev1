import type { ReactNode } from 'react'

export interface Tab<T extends string> {
  id: T
  label: string
}

interface TabsProps<T extends string> {
  label: string
  tabs: Tab<T>[]
  active: T
  onChange: (tab: T) => void
  children: ReactNode
}

/** Tab bar; only the active panel is rendered, so it loads fresh data when opened. */
export function Tabs<T extends string>({ label, tabs, active, onChange, children }: TabsProps<T>) {
  return (
    <div className="flex flex-col gap-4">
      <div role="tablist" aria-label={label} className="flex gap-1 border-b border-slate-200">
        {tabs.map((tab) => (
          <button
            key={tab.id}
            type="button"
            role="tab"
            id={`tab-${tab.id}`}
            aria-selected={tab.id === active}
            aria-controls={`panel-${tab.id}`}
            onClick={() => onChange(tab.id)}
            className={`-mb-px border-b-2 px-4 py-2 text-sm font-medium ${
              tab.id === active
                ? 'border-brand-600 text-brand-700'
                : 'border-transparent text-slate-600 hover:text-slate-900'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>
      <div role="tabpanel" id={`panel-${active}`} aria-labelledby={`tab-${active}`}>
        {children}
      </div>
    </div>
  )
}
