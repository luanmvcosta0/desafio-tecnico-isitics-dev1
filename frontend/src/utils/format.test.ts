import { describe, expect, it } from 'vitest'
import { formatDateTime, formatMoney, formatSignedMoney, toApiDateTime } from './format'

describe('formatMoney', () => {
  it.each([
    [40, '40,00'],
    [261.15, '261,15'],
    [1234.5, '1.234,50'],
    [0, '0,00'],
  ])('formats %d as %s', (value, expected) => {
    expect(formatMoney(value)).toBe(expected)
  })
})

describe('formatSignedMoney', () => {
  it.each([
    [35.7, '+35,70'],
    [-38.85, '-38,85'],
    [0, '0,00'],
  ])('formats %d as %s', (value, expected) => {
    expect(formatSignedMoney(value)).toBe(expected)
  })
})

describe('formatDateTime', () => {
  it('shows the API date-time in Brasília time', () => {
    expect(formatDateTime('2026-11-05T16:40:00-03:00')).toBe('05/11/2026, 16:40')
  })
})

describe('toApiDateTime', () => {
  it('appends the Brasília offset to a datetime-local value', () => {
    expect(toApiDateTime('2027-03-02T10:15')).toBe('2027-03-02T10:15-03:00')
  })
})
