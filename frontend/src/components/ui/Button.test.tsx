import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { Button } from './Button'

describe('Button', () => {
  it('handles a double click as a single click', async () => {
    const onClick = vi.fn()
    render(<Button onClick={onClick}>Vender</Button>)

    await userEvent.dblClick(screen.getByRole('button', { name: 'Vender' }))

    expect(onClick).toHaveBeenCalledOnce()
  })

  it('submits its form only once on double click', async () => {
    const onSubmit = vi.fn((event: SubmitEvent) => event.preventDefault())
    render(
      <form onSubmit={(event) => onSubmit(event.nativeEvent as SubmitEvent)}>
        <Button type="submit">Jogar partida</Button>
      </form>,
    )

    await userEvent.dblClick(screen.getByRole('button', { name: 'Jogar partida' }))

    expect(onSubmit).toHaveBeenCalledOnce()
  })
})
