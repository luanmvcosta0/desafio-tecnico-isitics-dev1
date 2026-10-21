export type ClubSort = 'name' | 'net_worth_desc' | 'net_worth_asc'

export interface Club {
  id: number
  name: string
  acronym: string
  cash: number
  squad_value: number
  net_worth: number
}

export interface ClubCreateInput {
  name: string
  acronym: string
}
