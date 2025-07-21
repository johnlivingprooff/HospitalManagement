import { Skeleton } from '../ui/Skeleton'

// Medical Records Loading States
export function LoadingMedicalRecordList() {
  return (
    <div className="space-y-4">
      {Array(5).fill(0).map((_, i) => (
        <div key={i} className="p-4 border rounded-lg">
          <div className="flex items-start justify-between">
            <div className="space-y-2">
              <Skeleton variant="text" width={200} height={24} />
              <Skeleton variant="text" width={300} height={20} />
              <div className="flex mt-2 space-x-2">
                <Skeleton variant="badge" width={80} />
                <Skeleton variant="badge" width={100} />
              </div>
            </div>
            <Skeleton variant="button" width={100} />
          </div>
        </div>
      ))}
    </div>
  )
}

// Prescription Loading States
export function LoadingPrescriptionDetail() {
  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between">
        <div>
          <Skeleton className="h-8 rounded w-60" />
          <div className="flex mt-2 space-x-3">
            <Skeleton className="h-6 rounded-full w-30" />
            <Skeleton className="w-24 h-6 rounded-full" />
          </div>
        </div>
        <div className="flex space-x-3">
          <Skeleton className="h-10 rounded w-30" />
          <Skeleton className="h-10 rounded w-30" />
        </div>
      </div>
      
      <div className="grid gap-6 md:grid-cols-2">
        <div className="space-y-4">
          <Skeleton className="h-6 rounded w-36" />
          <div className="space-y-2">
            {Array(3).fill(0).map((_, i) => (
              <Skeleton key={i} className="w-full h-12 rounded" />
            ))}
          </div>
        </div>
        <div className="space-y-4">
          <Skeleton className="h-6 rounded w-36" />
          <div className="space-y-2">
            {Array(2).fill(0).map((_, i) => (
              <Skeleton key={i} className="w-full h-12 rounded" />
            ))}
          </div>
        </div>
      </div>

      <div className="space-y-3">
        <Skeleton className="w-48 h-6 rounded" />
        <div className="space-y-2">
          {Array(3).fill(0).map((_, i) => (
            <div key={i} className="p-3 border rounded-lg">
              <div className="flex justify-between">
                <div className="space-y-1">
                  <Skeleton className="w-40 h-5 rounded" />
                  <Skeleton className="w-56 h-4 rounded" />
                </div>
                <Skeleton className="w-16 h-8 rounded" />
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}

// Ward Overview with Beds
export function LoadingWardOverview() {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="space-y-2">
          <Skeleton className="h-8 rounded w-60" />
          <div className="flex space-x-3">
            <Skeleton className="h-6 rounded-full w-30" />
            <Skeleton className="w-24 h-6 rounded-full" />
          </div>
        </div>
        <div className="flex space-x-3">
          <Skeleton className="h-10 rounded w-30" />
          <Skeleton className="h-10 rounded w-30" />
        </div>
      </div>

      <div className="grid gap-4 lg:grid-cols-3 md:grid-cols-2">
        <div className="space-y-3">
          <Skeleton className="h-6 rounded w-36" />
          <div className="space-y-2">
            {Array(3).fill(0).map((_, i) => (
              <Skeleton key={i} className="w-full h-12 rounded" />
            ))}
          </div>
        </div>
        <div className="col-span-2 space-y-3">
          <Skeleton className="h-6 rounded w-36" />
          <Skeleton className="w-full h-48 rounded-lg" />
        </div>
      </div>
    </div>
  )
}

// Ward Detail with Bed Layout
export function LoadingWardDetail() {
  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between">
        <div className="space-y-2">
          <Skeleton className="w-48 h-8 rounded" />
          <Skeleton className="h-5 rounded w-80" />
          <div className="flex space-x-2">
            <Skeleton className="w-20 h-6 rounded-full" />
            <Skeleton className="w-24 h-6 rounded-full" />
            <Skeleton className="w-16 h-6 rounded-full" />
          </div>
        </div>
        <Skeleton className="w-32 h-10 rounded" />
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <div className="space-y-4">
          <Skeleton className="w-32 h-6 rounded" />
          <div className="space-y-2">
            {Array(4).fill(0).map((_, i) => (
              <div key={i} className="flex justify-between py-2">
                <Skeleton className="w-24 h-4 rounded" />
                <Skeleton className="w-16 h-4 rounded" />
              </div>
            ))}
          </div>
        </div>
        <div className="space-y-4">
          <Skeleton className="h-6 rounded w-28" />
          <div className="grid grid-cols-3 gap-2">
            {Array(12).fill(0).map((_, i) => (
              <div key={i} className="p-2 border rounded-lg aspect-square">
                <div className="space-y-1">
                  <Skeleton className="w-8 h-3 rounded" />
                  <Skeleton className="w-12 h-2 rounded" />
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}

// Lab Test Results Loading
export function LoadingLabResults() {
  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <Skeleton className="w-40 h-8 rounded" />
        <Skeleton className="w-32 h-10 rounded" />
      </div>
      
      {Array(4).fill(0).map((_, i) => (
        <div key={i} className="p-4 border rounded-lg">
          <div className="flex items-start justify-between mb-3">
            <div className="space-y-1">
              <Skeleton className="w-48 h-6 rounded" />
              <Skeleton className="w-32 h-4 rounded" />
            </div>
            <Skeleton className="w-20 h-6 rounded-full" />
          </div>
          
          <div className="grid gap-4 md:grid-cols-3">
            <div className="space-y-2">
              <Skeleton className="w-24 h-4 rounded" />
              <Skeleton className="w-16 h-6 rounded" />
            </div>
            <div className="space-y-2">
              <Skeleton className="h-4 rounded w-28" />
              <Skeleton className="w-20 h-6 rounded" />
            </div>
            <div className="space-y-2">
              <Skeleton className="w-20 h-4 rounded" />
              <Skeleton className="w-24 h-6 rounded" />
            </div>
          </div>
        </div>
      ))}
    </div>
  )
}

// Lab Test Detail with Charts
export function LoadingLabTestDetail() {
  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between">
        <div className="space-y-2">
          <Skeleton className="w-56 h-8 rounded" />
          <div className="flex space-x-2">
            <Skeleton className="w-24 h-6 rounded-full" />
            <Skeleton className="w-20 h-6 rounded-full" />
          </div>
        </div>
        <div className="flex space-x-2">
          <Skeleton className="h-10 rounded w-28" />
          <Skeleton className="w-24 h-10 rounded" />
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        <div className="space-y-4">
          <Skeleton className="w-32 h-6 rounded" />
          <div className="space-y-2">
            {Array(5).fill(0).map((_, i) => (
              <div key={i} className="flex justify-between">
                <Skeleton className="h-4 rounded w-28" />
                <Skeleton className="w-16 h-4 rounded" />
              </div>
            ))}
          </div>
        </div>
        <div className="col-span-2">
          <Skeleton className="w-40 h-6 mb-4 rounded" />
          <Skeleton className="w-full h-64 rounded-lg" />
        </div>
      </div>
    </div>
  )
}

// General Loading for Feature Cards
export function LoadingFeatureCard() {
  return (
    <div className="p-4 space-y-3 border rounded-lg">
      <div className="flex items-center space-x-3">
        <Skeleton className="w-12 h-12 rounded-lg" />
        <div className="space-y-2">
          <Skeleton className="w-32 h-5 rounded" />
          <Skeleton className="w-48 h-4 rounded" />
        </div>
      </div>
      <Skeleton className="w-full h-10 rounded" />
    </div>
  )
}

// System Metrics Loading
export function LoadingSystemMetrics() {
  return (
    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
      {Array(4).fill(0).map((_, i) => (
        <div key={i} className="p-4 space-y-2 border rounded-lg">
          <div className="flex items-start justify-between">
            <Skeleton className="w-24 h-4 rounded" />
            <Skeleton className="w-8 h-8 rounded" />
          </div>
          <Skeleton className="w-16 h-8 rounded" />
          <Skeleton className="w-20 h-3 rounded" />
        </div>
      ))}
    </div>
  )
}
