import { LoadingCard } from './LoadingStates'
import { Skeleton } from '../ui/Skeleton'

export function LoadingAppointmentDetail() {
  return (
    <div className="space-y-8">
      <div className="rounded-lg border p-6">
        <div className="mb-6 space-y-4">
          <Skeleton className="h-8 w-[300px]" />
          <div className="flex space-x-4">
            <Skeleton className="h-6 w-[150px]" />
            <Skeleton className="h-6 w-[150px]" />
          </div>
        </div>
        <div className="grid gap-6 md:grid-cols-2">
          <div className="space-y-4">
            <Skeleton className="h-4 w-[200px]" />
            <Skeleton className="h-4 w-[250px]" />
            <Skeleton className="h-4 w-[150px]" />
          </div>
          <div className="space-y-4">
            <Skeleton className="h-4 w-[200px]" />
            <Skeleton className="h-4 w-[250px]" />
            <Skeleton className="h-4 w-[150px]" />
          </div>
        </div>
      </div>
      <div className="rounded-lg border p-6">
        <Skeleton className="mb-4 h-6 w-[200px]" />
        <div className="space-y-4">
          <Skeleton className="h-24 w-full" />
          <div className="flex justify-end space-x-4">
            <Skeleton className="h-10 w-[120px]" />
            <Skeleton className="h-10 w-[120px]" />
          </div>
        </div>
      </div>
    </div>
  )
}

export function LoadingPatientRecord() {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="space-y-1">
          <Skeleton className="h-8 w-[300px]" />
          <Skeleton className="h-4 w-[200px]" />
        </div>
        <Skeleton className="h-10 w-[150px]" />
      </div>
      <div className="grid gap-6 md:grid-cols-2">
        <LoadingCard count={3} />
      </div>
      <div className="rounded-lg border p-6">
        <Skeleton className="mb-4 h-6 w-[200px]" />
        <div className="space-y-3">
          {Array(5).fill(0).map((_, i) => (
            <div key={i} className="flex space-x-4">
              {Array(4).fill(0).map((_, j) => (
                <Skeleton key={j} className="h-10 flex-1" />
              ))}
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}

export function LoadingForm() {
  return (
    <div className="space-y-6 rounded-lg border p-6">
      <Skeleton className="h-8 w-[250px]" />
      <div className="space-y-4">
        {Array(6).fill(0).map((_, i) => (
          <div key={i} className="space-y-2">
            <Skeleton className="h-4 w-[150px]" />
            <Skeleton className="h-10 w-full" />
          </div>
        ))}
        <div className="flex justify-end space-x-4">
          <Skeleton className="h-10 w-[100px]" />
          <Skeleton className="h-10 w-[100px]" />
        </div>
      </div>
    </div>
  )
}

export function LoadingUserProfile() {
  return (
    <div className="space-y-8">
      <div className="flex items-start space-x-6">
        <Skeleton className="h-24 w-24 rounded-full" />
        <div className="space-y-2">
          <Skeleton className="h-8 w-[250px]" />
          <Skeleton className="h-4 w-[200px]" />
          <Skeleton className="h-4 w-[150px]" />
        </div>
      </div>
      <div className="grid gap-6 md:grid-cols-2">
        <LoadingCard count={2} />
      </div>
    </div>
  )
}
