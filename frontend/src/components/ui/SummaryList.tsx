export interface SummaryItem {
  label: string
  value: string
}

interface SummaryListProps {
  label: string
  items: SummaryItem[]
  columnsClassName?: string
}

/** Label/value pairs of a result returned by the API (player, purchase, sale, club totals). */
export function SummaryList({
  label,
  items,
  columnsClassName = 'grid-cols-2 sm:grid-cols-3',
}: SummaryListProps) {
  return (
    <dl aria-label={label} className={`grid gap-3 ${columnsClassName}`}>
      {items.map((item) => (
        <div key={item.label}>
          <dt className="text-xs text-slate-500">{item.label}</dt>
          <dd className="font-medium">{item.value}</dd>
        </div>
      ))}
    </dl>
  )
}
