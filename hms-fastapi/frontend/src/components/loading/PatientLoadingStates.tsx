import { Skeleton } from '../ui/Skeleton'

export const LoadingPatientsOverview = () => {
  return (
    <div className="bg-white min-h-screen">
      {/* Header */}
      <div className="py-6 px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between">
          <div className="flex items-center">
            <Skeleton className="h-8 w-32" />
          </div>
          <div className="flex items-center gap-4">
            <Skeleton className="h-10 w-28" />
          </div>
        </div>
      </div>

      {/* Search and Filters */}
      <div className="px-4 sm:px-6 lg:px-8 py-4 bg-white border-t border-gray-200">
        <div className="flex flex-wrap gap-4 items-center">
          <div className="flex-1 min-w-[200px]">
            <Skeleton className="h-10 w-full max-w-xs" />
          </div>
          <div className="flex-1 min-w-[200px]">
            <Skeleton className="h-10 w-full max-w-xs" />
          </div>
          <div className="flex-1 min-w-[200px]">
            <Skeleton className="h-10 w-full max-w-xs" />
          </div>
        </div>
      </div>

      {/* Patients Grid */}
      <div className="px-4 sm:px-6 lg:px-8 py-6">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {[1, 2, 3, 4, 5, 6, 7, 8].map(i => (
            <div key={i} className="bg-white rounded-lg shadow-sm p-6">
              <div className="flex items-start justify-between">
                <div className="flex items-center space-x-4">
                  <Skeleton className="h-12 w-12 rounded-full" />
                  <div>
                    <Skeleton className="h-5 w-32 mb-1" />
                    <Skeleton className="h-4 w-24" />
                  </div>
                </div>
                <Skeleton className="h-8 w-8" />
              </div>

              <div className="mt-4 space-y-3">
                <div className="flex items-center">
                  <Skeleton className="h-4 w-4 mr-2" />
                  <Skeleton className="h-4 w-32" />
                </div>
                <div className="flex items-center">
                  <Skeleton className="h-4 w-4 mr-2" />
                  <Skeleton className="h-4 w-40" />
                </div>
                <div className="flex items-center">
                  <Skeleton className="h-4 w-4 mr-2" />
                  <Skeleton className="h-4 w-28" />
                </div>
              </div>

              <div className="mt-4 pt-4 border-t">
                <div className="flex justify-between items-center">
                  <Skeleton className="h-6 w-24" />
                  <Skeleton className="h-8 w-8" />
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}

export const LoadingPatientDetail = () => {
  return (
    <div className="min-h-screen bg-gray-100 p-6">
      <div className="max-w-7xl mx-auto">
        {/* Back button */}
        <div className="mb-6">
          <Skeleton className="h-8 w-24" />
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Profile Card */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-lg shadow-sm p-6">
              <div className="text-center">
                <Skeleton className="h-24 w-24 rounded-full mx-auto mb-4" />
                <Skeleton className="h-6 w-48 mx-auto mb-2" />
                <Skeleton className="h-4 w-32 mx-auto mb-4" />
              </div>

              <div className="space-y-4 mt-6">
                <div className="flex items-center">
                  <Skeleton className="h-5 w-5 mr-3" />
                  <Skeleton className="h-4 w-full" />
                </div>
                <div className="flex items-center">
                  <Skeleton className="h-5 w-5 mr-3" />
                  <Skeleton className="h-4 w-full" />
                </div>
                <div className="flex items-center">
                  <Skeleton className="h-5 w-5 mr-3" />
                  <Skeleton className="h-4 w-full" />
                </div>
                <div className="flex items-center">
                  <Skeleton className="h-5 w-5 mr-3" />
                  <Skeleton className="h-4 w-full" />
                </div>
              </div>

              <div className="mt-6">
                <Skeleton className="h-10 w-full" />
              </div>
            </div>
          </div>

          {/* Medical History */}
          <div className="lg:col-span-2">
            <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
              <div className="flex justify-between items-center mb-6">
                <Skeleton className="h-6 w-40" />
                <Skeleton className="h-9 w-28" />
              </div>

              <div className="space-y-6">
                {[1, 2, 3].map(i => (
                  <div key={i} className="border rounded-lg p-4">
                    <div className="flex justify-between items-start mb-3">
                      <div>
                        <Skeleton className="h-5 w-48 mb-2" />
                        <Skeleton className="h-4 w-32" />
                      </div>
                      <Skeleton className="h-6 w-24" />
                    </div>
                    <div className="space-y-2">
                      <Skeleton className="h-4 w-full" />
                      <Skeleton className="h-4 w-3/4" />
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Recent Activity */}
            <div className="bg-white rounded-lg shadow-sm p-6">
              <div className="flex justify-between items-center mb-6">
                <Skeleton className="h-6 w-40" />
                <Skeleton className="h-9 w-28" />
              </div>

              <div className="space-y-4">
                {[1, 2, 3].map(i => (
                  <div key={i} className="flex items-start space-x-4">
                    <Skeleton className="h-10 w-10 rounded-full" />
                    <div className="flex-1">
                      <Skeleton className="h-4 w-3/4 mb-2" />
                      <Skeleton className="h-4 w-1/2" />
                    </div>
                    <Skeleton className="h-4 w-24" />
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
