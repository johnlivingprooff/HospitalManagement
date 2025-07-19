import React from 'react'
import { useRole } from '../contexts/RoleContext'
import { ShieldX } from 'lucide-react'

interface ProtectedPageProps {
  resource: string
  action?: string
  children: React.ReactNode
}

const ProtectedPage: React.FC<ProtectedPageProps> = ({ 
  resource, 
  action = 'read', 
  children 
}) => {
  const { hasPermission, getUserRole } = useRole()

  if (!hasPermission(resource, action)) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
        <div className="max-w-md w-full space-y-8">
          <div className="text-center">
            <ShieldX className="mx-auto h-24 w-24 text-red-400" />
            <h2 className="mt-6 text-3xl font-extrabold text-gray-900">
              Access Denied
            </h2>
            <p className="mt-2 text-sm text-gray-600">
              You don't have permission to access this page.
            </p>
            <p className="text-xs text-gray-500 mt-4">
              Current role: <span className="font-medium">{getUserRole()}</span>
            </p>
            <p className="text-xs text-gray-500">
              Required permission: <span className="font-medium">{action}</span> access to <span className="font-medium">{resource}</span>
            </p>
          </div>
          <div className="text-center">
            <button
              onClick={() => window.history.back()}
              className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              Go Back
            </button>
          </div>
        </div>
      </div>
    )
  }

  return <>{children}</>
}

export default ProtectedPage
