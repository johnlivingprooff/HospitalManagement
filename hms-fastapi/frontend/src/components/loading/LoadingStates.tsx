import { cn } from '../../utils/cn'
import { Skeleton } from '../ui/Skeleton'

interface LoadingCardProps {
  className?: string
  count?: number
}

export function LoadingCard({ className, count = 1 }: LoadingCardProps) {
  return (
    <>
      {Array(count)
        .fill(0)
        .map((_, i) => (
          <div
            key={i}
            className={cn(
              "rounded-lg border border-gray-200 p-4 shadow-sm",
              className
            )}
          >
            <div className="flex items-center space-x-4">
              <Skeleton className="h-12 w-12 rounded-full" />
              <div className="space-y-2">
                <Skeleton className="h-4 w-[250px]" />
                <Skeleton className="h-4 w-[200px]" />
              </div>
            </div>
          </div>
        ))}
    </>
  )
}

export function LoadingStats() {
  return (
    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
      {Array(4)
        .fill(0)
        .map((_, i) => (
          <div
            key={i}
            className="rounded-lg border border-gray-200 p-4 shadow-sm"
          >
            <Skeleton className="mb-2 h-4 w-[100px]" />
            <Skeleton className="h-8 w-[120px]" />
          </div>
        ))}
    </div>
  )
}

export function LoadingTable({ columns = 4, rows = 5 }: { columns?: number; rows?: number }) {
  return (
    <div className="rounded-lg border border-gray-200 shadow-sm">
      <div className="grid grid-cols-1">
        <div className="border-b border-gray-200 p-4">
          <div className="flex items-center space-x-4">
            {Array(columns)
              .fill(0)
              .map((_, i) => (
                <Skeleton key={i} className="h-4 w-[100px]" />
              ))}
          </div>
        </div>
        {Array(rows)
          .fill(0)
          .map((_, i) => (
            <div key={i} className="border-b border-gray-200 p-4 last:border-0">
              <div className="flex items-center space-x-4">
                {Array(columns)
                  .fill(0)
                  .map((_, j) => (
                    <Skeleton key={j} className="h-4 w-[100px]" />
                  ))}
              </div>
            </div>
          ))}
      </div>
    </div>
  )
}

export function LoadingList({ count = 5 }: { count?: number }) {
  return (
    <div className="space-y-4">
      {Array(count)
        .fill(0)
        .map((_, i) => (
          <div key={i} className="flex items-center space-x-4">
            <Skeleton className="h-4 w-[300px]" />
          </div>
        ))}
    </div>
  )
}

export function LoadingDashboardData() {
  return (
    <div className="space-y-6">
      <LoadingStats />
      <div className="grid gap-6 md:grid-cols-2">
        <div>
          <Skeleton className="mb-4 h-6 w-[200px]" />
          <LoadingTable columns={3} rows={5} />
        </div>
        <div>
          <Skeleton className="mb-4 h-6 w-[200px]" />
          <div className="h-[300px]">
            <Skeleton className="h-full w-full" />
          </div>
        </div>
      </div>
    </div>
  )
}
