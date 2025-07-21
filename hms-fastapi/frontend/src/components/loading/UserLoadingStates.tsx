// import React from 'react'
import { Skeleton } from '../ui/Skeleton'

export const LoadingUsersOverview = () => {
  return (
    <div className="min-h-screen bg-gray-100">
      {/* Header */}
      <div className="px-4 py-6 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between">
          <div className="flex items-center">
            <Skeleton className="w-32 h-8" />
          </div>
          <div className="flex items-center gap-4">
            <Skeleton className="h-10 w-28" />
          </div>
        </div>
      </div>

      {/* Search and Filters */}
      <div className="px-4 py-4 bg-white border-t border-gray-200 sm:px-6 lg:px-8">
        <div className="flex flex-wrap items-center gap-4">
          <div className="flex-1 min-w-[200px]">
            <Skeleton className="w-full h-10 max-w-xs" />
          </div>
          <div className="flex-1 min-w-[200px]">
            <Skeleton className="w-full h-10 max-w-xs" />
          </div>
        </div>
      </div>

      {/* Users Table */}
      <div className="px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col mt-8">
          <div className="-mx-4 -my-2 sm:-mx-6 lg:-mx-8">
            <div className="inline-block min-w-full py-2 align-middle">
              <div className="shadow-sm ring-1 ring-black ring-opacity-5">
                <table className="min-w-full border-separate border-spacing-0">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="sticky top-0 border-b border-gray-300 bg-gray-50 py-3.5 pl-4 pr-3 text-left sm:pl-6 lg:pl-8">
                        <Skeleton className="w-24 h-4" />
                      </th>
                      <th className="sticky top-0 border-b border-gray-300 bg-gray-50 px-3 py-3.5 text-left">
                        <Skeleton className="h-4 w-28" />
                      </th>
                      <th className="sticky top-0 border-b border-gray-300 bg-gray-50 px-3 py-3.5 text-left">
                        <Skeleton className="w-32 h-4" />
                      </th>
                      <th className="sticky top-0 border-b border-gray-300 bg-gray-50 px-3 py-3.5 text-left">
                        <Skeleton className="w-24 h-4" />
                      </th>
                      <th className="sticky top-0 border-b border-gray-300 bg-gray-50 px-3 py-3.5 text-center">
                        <Skeleton className="w-24 h-4 mx-auto" />
                      </th>
                      <th className="sticky top-0 border-b border-gray-300 bg-gray-50 py-3.5 pl-3 pr-4 sm:pr-6 lg:pr-8">
                        <Skeleton className="w-16 h-4" />
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {[1, 2, 3, 4, 5].map(i => (
                      <tr key={i}>
                        <td className="py-4 pl-4 pr-3 border-b border-gray-200 whitespace-nowrap sm:pl-6 lg:pl-8">
                          <div className="flex items-center">
                            <Skeleton className="w-8 h-8 mr-3 rounded-full" />
                            <div>
                              <Skeleton className="w-32 h-4 mb-1" />
                              <Skeleton className="w-24 h-3" />
                            </div>
                          </div>
                        </td>
                        <td className="px-3 py-4 border-b border-gray-200 whitespace-nowrap">
                          <Skeleton className="w-32 h-4" />
                        </td>
                        <td className="px-3 py-4 border-b border-gray-200 whitespace-nowrap">
                          <Skeleton className="w-40 h-4" />
                        </td>
                        <td className="px-3 py-4 border-b border-gray-200 whitespace-nowrap">
                          <Skeleton className="w-20 h-6" />
                        </td>
                        <td className="px-3 py-4 text-center border-b border-gray-200 whitespace-nowrap">
                          <Skeleton className="w-6 h-6 mx-auto" />
                        </td>
                        <td className="py-4 pl-3 pr-4 text-right border-b border-gray-200 whitespace-nowrap sm:pr-6 lg:pr-8">
                          <Skeleton className="w-8 h-8 ml-auto" />
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
