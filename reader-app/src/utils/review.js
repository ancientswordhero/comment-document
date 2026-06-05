export function canEditReview(review) {
  if (!review.createdAt) return false
  return (new Date() - new Date(review.createdAt)) < 3 * 60 * 1000
}
