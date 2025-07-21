import { Skeleton } from '../ui/Skeleton'

export const LoadingAppointmentsPage = () => {
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

      {/* Filters */}
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

      {/* Appointments Table */}
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
                        <Skeleton className="h-4 w-28" />
                      </th>
                      <th className="sticky top-0 border-b border-gray-300 bg-gray-50 px-3 py-3.5 text-left">
                        <Skeleton className="h-4 w-24" />
                      </th>
                      <th className="sticky top-0 border-b border-gray-300 bg-gray-50 px-3 py-3.5 text-left">
                        <Skeleton className="h-4 w-24" />
                      </th>
                      <th className="sticky top-0 border-b border-gray-300 bg-gray-50 px-3 py-3.5 text-left">
                        <Skeleton className="h-4 w-20" />
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
                          <Skeleton className="h-4 w-20" />
                        </td>
                        <td className="whitespace-nowrap border-b border-gray-200 px-3 py-4">
                          <Skeleton className="h-4 w-32" />
                        </td>
                        <td className="whitespace-nowrap border-b border-gray-200 px-3 py-4">
                          <div className="flex flex-col gap-1">
                            <Skeleton className="h-4 w-32" />
                            <Skeleton className="h-3 w-24" />
                          </div>
                        </td>
                        <td className="whitespace-nowrap border-b border-gray-200 px-3 py-4">
                          <Skeleton className="h-4 w-28" />
                        </td>
                        <td className="whitespace-nowrap border-b border-gray-200 px-3 py-4">
                          <Skeleton className="h-4 w-24" />
                        </td>
                        <td className="whitespace-nowrap border-b border-gray-200 px-3 py-4">
                          <Skeleton className="h-6 w-20" />
                        </td>
                        <td className="whitespace-nowrap border-b border-gray-200 py-4 pl-3 pr-4 sm:pr-6 lg:pr-8">
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

export const LoadingAppointmentDetail = () => {
  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center space-x-4">
        <Skeleton className="h-10 w-10 rounded-lg" />
        <div>
          <Skeleton className="h-8 w-48 mb-2" />
          <Skeleton className="h-5 w-32" />
        </div>
      </div>

      {/* Appointment Information Cards */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Appointment Information */}
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center mb-4">
            <Skeleton className="h-5 w-5 rounded mr-2" />
            <Skeleton className="h-6 w-40" />
          </div>
          <div className="space-y-4">
            {Array(4).fill(0).map((_, i) => (
              <div key={i}>
                <Skeleton className="h-4 w-24 mb-1" />
                <div className="flex items-center">
                  <Skeleton className="h-4 w-4 rounded mr-2" />
                  <Skeleton className="h-5 w-32" />
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Patient & Doctor */}
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center mb-4">
            <Skeleton className="h-5 w-5 rounded mr-2" />
            <Skeleton className="h-6 w-32" />
          </div>
          <div className="space-y-4">
            {Array(3).fill(0).map((_, i) => (
              <div key={i}>
                <Skeleton className="h-4 w-16 mb-1" />
                <div className="flex items-center">
                  <Skeleton className="h-4 w-4 rounded mr-2" />
                  <Skeleton className="h-5 w-40" />
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Notes & Medical Information */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center mb-4">
            <Skeleton className="h-5 w-5 rounded mr-2" />
            <Skeleton className="h-6 w-36" />
          </div>
          <div className="space-y-3">
            <Skeleton className="h-4 w-20" />
            <Skeleton className="h-20 w-full rounded" />
            <Skeleton className="h-4 w-16" />
            <Skeleton className="h-20 w-full rounded" />
          </div>
        </div>

        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center mb-4">
            <Skeleton className="h-5 w-5 rounded mr-2" />
            <Skeleton className="h-6 w-44" />
          </div>
          <div className="space-y-3">
            <Skeleton className="h-4 w-20" />
            <Skeleton className="h-20 w-full rounded" />
            <Skeleton className="h-4 w-28" />
            <Skeleton className="h-20 w-full rounded" />
          </div>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="flex justify-end space-x-4">
        <Skeleton className="h-10 w-32 rounded-lg" />
        <Skeleton className="h-10 w-40 rounded-lg" />
        <Skeleton className="h-10 w-36 rounded-lg" />
      </div>
    </div>
  )
}
