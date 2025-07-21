import { Skeleton } from '../ui/Skeleton'

export function LoadingPharmacyPage() {
  return (
    <div className="space-y-6">
      {/* Header Loading */}
      <div className="flex items-center justify-between">
        <Skeleton className="h-8 w-48" />
        <Skeleton variant="button" width={150} height={40} />
      </div>

      {/* Filters Loading */}
      <div className="p-4 bg-white rounded-lg shadow">
        <div className="flex flex-col gap-4 md:flex-row">
          <Skeleton variant="input" className="flex-1" />
          <Skeleton variant="input" width={150} />
        </div>
      </div>

      {/* Summary Cards Loading */}
      <div className="grid grid-cols-1 gap-4 md:grid-cols-4">
        {Array(4).fill(0).map((_, i) => (
          <div key={i} className="p-6 bg-white rounded-lg shadow">
            <div className="flex items-center">
              <Skeleton variant="circle" width={32} height={32} />
              <div className="ml-4 space-y-2">
                <Skeleton variant="text" width={80} height={16} />
                <Skeleton variant="text" width={40} height={24} />
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Table Loading */}
      <div className="overflow-hidden bg-white rounded-lg shadow">
        <div className="px-6 py-4 border-b border-gray-200">
          <Skeleton variant="text" width={200} height={24} />
        </div>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                {["Patient", "Medication", "Dosage", "Quantity", "Status", "Doctor", "Date", "Actions"].map((_, i) => (
                  <th key={i} className="px-6 py-3">
                    <Skeleton variant="text" width={80} height={16} />
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {Array(5).fill(0).map((_, i) => (
                <tr key={i}>
                  {Array(8).fill(0).map((_, j) => (
                    <td key={j} className="px-6 py-4">
                      <Skeleton 
                        variant="text" 
                        width={j === 7 ? 120 : 100} 
                        height={j === 7 ? 32 : 20}
                      />
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
