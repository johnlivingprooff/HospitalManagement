// Example usage of the Skeleton component
import { Skeleton } from '../ui/Skeleton'

// Method 1: Using variant prop (recommended for consistency)
export function ExampleWithVariants() {
  return (
    <div className="space-y-4">
      <Skeleton variant="text" width={200} height={24} />
      <Skeleton variant="button" width={120} />
      <Skeleton variant="card" width="100%" height={100} />
      <Skeleton variant="badge" width={80} />
      <Skeleton variant="avatar" width={40} height={40} />
      <Skeleton variant="input" className="w-full" />
    </div>
  )
}

// Method 2: Using className prop for custom styles
export function ExampleWithCustomClasses() {
  return (
    <div className="space-y-4">
      <Skeleton className="w-48 h-6 rounded" />
      <Skeleton className="w-32 h-10 rounded-md" />
      <Skeleton className="w-full h-24 border rounded-lg" />
    </div>
  )
}

// Method 3: Combining both approaches
export function ExampleWithMixedApproach() {
  return (
    <div className="space-y-4">
      <Skeleton variant="card" className="p-4" width="100%" height={120} />
      <Skeleton variant="text" className="mx-auto" width={150} />
      <Skeleton variant="button" className="ml-auto" width={100} />
    </div>
  )
}
