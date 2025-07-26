import { Skeleton } from '../ui/Skeleton'

export const LoadingClaimsPage = () => (
  <div className="space-y-6">
    {/* Header skeletons */}
    <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
      <Skeleton className="w-48 h-8 rounded" />
      <Skeleton className="w-40 h-10 rounded" />
    </div>
    {/* Table header skeleton */}
    <div className="hidden grid-cols-7 gap-4 px-2 sm:grid">
      <Skeleton className="w-8 h-4" />
      <Skeleton className="w-20 h-4" />
      <Skeleton className="w-24 h-4" />
      <Skeleton className="w-16 h-4" />
      <Skeleton className="w-20 h-4" />
      <Skeleton className="w-24 h-4" />
      <Skeleton className="w-20 h-4" />
    </div>
    {/* Card-like skeleton rows */}
    <div className="space-y-4">
      {[1,2,3].map(i => (
        <div key={i} className="flex flex-col items-center gap-4 px-4 py-6 bg-white rounded-lg shadow sm:flex-row">
          <Skeleton className="w-8 h-4" />
          <Skeleton className="w-20 h-4" />
          <Skeleton className="w-24 h-4" />
          <Skeleton className="w-16 h-4" />
          <Skeleton className="w-20 h-4" />
          <Skeleton className="w-24 h-4" />
          <Skeleton className="w-20 h-4" />
        </div>
      ))}
    </div>
  </div>
)
