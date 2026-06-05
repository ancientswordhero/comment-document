export function flattenCategories(categories, depth = 0) {
  const prefix = '  '.repeat(depth)
  let result = []
  for (const cat of categories) {
    result.push({ ...cat, displayName: (depth > 0 ? prefix : '') + cat.name })
    if (cat.children && cat.children.length) {
      result = result.concat(flattenCategories(cat.children, depth + 1))
    }
  }
  return result
}
