import { Skeleton } from '../ui/Skeleton'

export const LoadingBillsOverview = () => {
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

      {/* Bills Table */}
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
                        <Skeleton className="h-4 w-24" />
                      </th>
                      <th className="sticky top-0 border-b border-gray-300 bg-gray-50 px-3 py-3.5 text-right">
                        <Skeleton className="h-4 w-24 ml-auto" />
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
                          <Skeleton className="h-4 w-40" />
                        </td>
                        <td className="whitespace-nowrap border-b border-gray-200 px-3 py-4">
                          <Skeleton className="h-4 w-24" />
                        </td>
                        <td className="whitespace-nowrap border-b border-gray-200 px-3 py-4 text-right">
                          <Skeleton className="h-4 w-24 ml-auto" />
                        </td>
                        <td className="whitespace-nowrap border-b border-gray-200 px-3 py-4">
                          <Skeleton className="h-6 w-20" />
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

export const LoadingBillDetail = () => {
  return (
    <div className="min-h-screen bg-gray-100 p-6">
      <div className="max-w-7xl mx-auto">
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
          {/* Bill Header */}
          <div className="flex justify-between items-start mb-6">
            <div className="space-y-4">
              <Skeleton className="h-8 w-48" />
              <div className="space-y-2">
                <Skeleton className="h-4 w-32" />
                <Skeleton className="h-4 w-40" />
              </div>
            </div>
            <div className="text-right space-y-2">
              <Skeleton className="h-6 w-24 ml-auto" />
              <Skeleton className="h-4 w-32 ml-auto" />
            </div>
          </div>

          {/* Bill Details */}
          <div className="grid grid-cols-2 gap-6 mb-6">
            <div className="space-y-2">
              <Skeleton className="h-4 w-24" />
              <Skeleton className="h-6 w-48" />
            </div>
            <div className="space-y-2">
              <Skeleton className="h-4 w-24" />
              <Skeleton className="h-6 w-48" />
            </div>
            <div className="space-y-2">
              <Skeleton className="h-4 w-24" />
              <Skeleton className="h-6 w-48" />
            </div>
            <div className="space-y-2">
              <Skeleton className="h-4 w-24" />
              <Skeleton className="h-6 w-48" />
            </div>
          </div>

          {/* Bill Items Table */}
          <div className="mt-8">
            <table className="w-full">
              <thead>
                <tr className="border-b">
                  <th className="pb-2 text-left"><Skeleton className="h-4 w-32" /></th>
                  <th className="pb-2 text-right"><Skeleton className="h-4 w-24 ml-auto" /></th>
                  <th className="pb-2 text-right"><Skeleton className="h-4 w-24 ml-auto" /></th>
                  <th className="pb-2 text-right"><Skeleton className="h-4 w-24 ml-auto" /></th>
                </tr>
              </thead>
              <tbody className="divide-y">
                {[1, 2, 3].map(i => (
                  <tr key={i} className="py-3">
                    <td className="py-3"><Skeleton className="h-4 w-64" /></td>
                    <td className="text-right"><Skeleton className="h-4 w-20 ml-auto" /></td>
                    <td className="text-right"><Skeleton className="h-4 w-16 ml-auto" /></td>
                    <td className="text-right"><Skeleton className="h-4 w-24 ml-auto" /></td>
                  </tr>
                ))}
              </tbody>
              <tfoot>
                <tr className="border-t font-semibold">
                  <td colSpan={3} className="py-3 text-right"><Skeleton className="h-5 w-32 ml-auto" /></td>
                  <td className="py-3 text-right"><Skeleton className="h-5 w-28 ml-auto" /></td>
                </tr>
              </tfoot>
            </table>
          </div>

          {/* Actions */}
          <div className="mt-8 flex justify-end gap-4">
            <Skeleton className="h-10 w-28" />
            <Skeleton className="h-10 w-28" />
          </div>
        </div>
      </div>
    </div>
  )
}
