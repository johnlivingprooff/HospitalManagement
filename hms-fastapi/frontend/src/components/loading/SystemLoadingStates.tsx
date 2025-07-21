import { Skeleton } from '../ui/Skeleton'

export const LoadingMedicalRecords = () => {
  return (
    <div className="bg-white min-h-screen">
      {/* Header */}
      <div className="py-6 px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between">
          <div className="flex items-center">
            <Skeleton className="h-8 w-48" />
          </div>
          <div className="flex items-center gap-4">
            <Skeleton className="h-10 w-32" />
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

      {/* Records List */}
      <div className="px-4 sm:px-6 lg:px-8 py-6">
        <div className="space-y-6">
          {[1, 2, 3, 4, 5].map(i => (
            <div key={i} className="bg-white rounded-lg shadow-sm border p-6">
              <div className="flex items-start justify-between mb-4">
                <div className="flex items-center space-x-4">
                  <Skeleton className="h-12 w-12 rounded-full" />
                  <div>
                    <Skeleton className="h-5 w-48 mb-2" />
                    <Skeleton className="h-4 w-32" />
                  </div>
                </div>
                <div className="text-right">
                  <Skeleton className="h-4 w-24 mb-1" />
                  <Skeleton className="h-6 w-20" />
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
                <div>
                  <Skeleton className="h-4 w-20 mb-2" />
                  <Skeleton className="h-5 w-32" />
                </div>
                <div>
                  <Skeleton className="h-4 w-24 mb-2" />
                  <Skeleton className="h-5 w-28" />
                </div>
                <div>
                  <Skeleton className="h-4 w-28 mb-2" />
                  <Skeleton className="h-5 w-36" />
                </div>
              </div>

              <div className="mb-4">
                <Skeleton className="h-4 w-20 mb-2" />
                <Skeleton className="h-4 w-full" />
                <Skeleton className="h-4 w-3/4 mt-1" />
              </div>

              <div className="flex justify-between items-center pt-4 border-t">
                <div className="flex items-center space-x-4">
                  <Skeleton className="h-4 w-24" />
                  <Skeleton className="h-4 w-32" />
                </div>
                <Skeleton className="h-8 w-20" />
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}

export const LoadingUsersPage = () => {
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

      {/* Users Table */}
      <div className="px-4 sm:px-6 lg:px-8">
        <div className="mt-8 flex flex-col">
          <div className="-my-2 -mx-4 sm:-mx-6 lg:-mx-8">
            <div className="inline-block min-w-full py-2 align-middle">
              <div className="shadow-sm ring-1 ring-black ring-opacity-5">
                <table className="min-w-full border-separate border-spacing-0">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="sticky top-0 border-b border-gray-300 bg-gray-50 py-3.5 pl-4 pr-3 text-left sm:pl-6 lg:pl-8">
                        <Skeleton className="h-4 w-24" />
                      </th>
                      <th className="sticky top-0 border-b border-gray-300 bg-gray-50 px-3 py-3.5 text-left">
                        <Skeleton className="h-4 w-28" />
                      </th>
                      <th className="sticky top-0 border-b border-gray-300 bg-gray-50 px-3 py-3.5 text-left">
                        <Skeleton className="h-4 w-32" />
                      </th>
                      <th className="sticky top-0 border-b border-gray-300 bg-gray-50 px-3 py-3.5 text-left">
                        <Skeleton className="h-4 w-20" />
                      </th>
                      <th className="sticky top-0 border-b border-gray-300 bg-gray-50 px-3 py-3.5 text-left">
                        <Skeleton className="h-4 w-24" />
                      </th>
                      <th className="sticky top-0 border-b border-gray-300 bg-gray-50 py-3.5 pl-3 pr-4 sm:pr-6 lg:pr-8">
                        <Skeleton className="h-4 w-16" />
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {[1, 2, 3, 4, 5].map(i => (
                      <tr key={i}>
                        <td className="whitespace-nowrap border-b border-gray-200 py-4 pl-4 pr-3 sm:pl-6 lg:pl-8">
                          <div className="flex items-center">
                            <Skeleton className="h-10 w-10 rounded-full mr-4" />
                            <div>
                              <Skeleton className="h-4 w-32 mb-1" />
                              <Skeleton className="h-3 w-24" />
                            </div>
                          </div>
                        </td>
                        <td className="whitespace-nowrap border-b border-gray-200 px-3 py-4">
                          <Skeleton className="h-4 w-40" />
                        </td>
                        <td className="whitespace-nowrap border-b border-gray-200 px-3 py-4">
                          <Skeleton className="h-4 w-24" />
                        </td>
                        <td className="whitespace-nowrap border-b border-gray-200 px-3 py-4">
                          <Skeleton className="h-6 w-20" />
                        </td>
                        <td className="whitespace-nowrap border-b border-gray-200 px-3 py-4">
                          <Skeleton className="h-4 w-24" />
                        </td>
                        <td className="whitespace-nowrap border-b border-gray-200 py-4 pl-3 pr-4 text-right sm:pr-6 lg:pr-8">
                          <Skeleton className="h-8 w-8 ml-auto" />
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
