// import React from 'react'
import { Skeleton } from '../ui/Skeleton'

export const LoadingWardDetail = () => {
  return (
    <div className="min-h-screen p-6 bg-gray-100">
      <div className="mx-auto max-w-7xl">
        {/* Back button */}
        <div className="mb-6">
          <Skeleton className="w-24 h-8" />
        </div>

        <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
          {/* Ward Info Card */}
          <div className="lg:col-span-1">
            <div className="p-6 bg-white rounded-lg shadow-sm">
              <div className="text-center">
                <Skeleton className="w-16 h-16 mx-auto mb-4" />
                <Skeleton className="w-48 h-6 mx-auto mb-2" />
                <Skeleton className="w-32 h-4 mx-auto mb-4" />
              </div>

              <div className="mt-6 space-y-4">
                <div className="flex items-center">
                  <Skeleton className="w-5 h-5 mr-3" />
                  <Skeleton className="w-full h-4" />
                </div>
                <div className="flex items-center">
                  <Skeleton className="w-5 h-5 mr-3" />
                  <Skeleton className="w-full h-4" />
                </div>
                <div className="flex items-center">
                  <Skeleton className="w-5 h-5 mr-3" />
                  <Skeleton className="w-full h-4" />
                </div>
                <div className="flex items-center">
                  <Skeleton className="w-5 h-5 mr-3" />
                  <Skeleton className="w-full h-4" />
                </div>
              </div>

              <div className="mt-6">
                <Skeleton className="w-full h-10" />
              </div>
            </div>
          </div>

          {/* Beds Overview */}
          <div className="lg:col-span-2">
            <div className="p-6 mb-6 bg-white rounded-lg shadow-sm">
              <div className="flex items-center justify-between mb-6">
                <Skeleton className="w-40 h-6" />
                <div className="flex gap-3">
                  <Skeleton className="h-9 w-28" />
                  <Skeleton className="h-9 w-28" />
                </div>
              </div>

              <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
                {[1, 2, 3, 4, 5, 6].map(i => (
                  <div key={i} className="p-4 border rounded-lg">
                    <div className="flex items-start justify-between mb-3">
                      <div>
                        <Skeleton className="w-24 h-5 mb-2" />
                        <Skeleton className="w-32 h-4" />
                      </div>
                      <Skeleton className="w-6 h-6" />
                    </div>
                    <div className="space-y-2">
                      <div className="flex items-center">
                        <Skeleton className="w-4 h-4 mr-2" />
                        <Skeleton className="w-32 h-4" />
                      </div>
                      <div className="flex items-center">
                        <Skeleton className="w-4 h-4 mr-2" />
                        <Skeleton className="h-4 w-28" />
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Recent Activity */}
            <div className="p-6 bg-white rounded-lg shadow-sm">
              <div className="flex items-center justify-between mb-6">
                <Skeleton className="w-40 h-6" />
                <Skeleton className="h-9 w-28" />
              </div>

              <div className="space-y-4">
                {[1, 2, 3].map(i => (
                  <div key={i} className="flex items-start space-x-4">
                    <Skeleton className="w-10 h-10 rounded-full" />
                    <div className="flex-1">
                      <Skeleton className="w-3/4 h-4 mb-2" />
                      <Skeleton className="w-1/2 h-4" />
                    </div>
                    <Skeleton className="w-24 h-4" />
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
