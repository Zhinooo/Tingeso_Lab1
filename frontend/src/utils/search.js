const normalize = (value) =>
  String(value ?? '')
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .toLowerCase()

export const matchesSearch = (query, ...fields) => {
  const term = normalize(query).trim()
  if (!term) return true
  return fields.some((field) => normalize(field).includes(term))
}
