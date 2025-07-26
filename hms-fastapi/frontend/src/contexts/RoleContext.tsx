import { createContext, useContext } from 'react'
import { useAuth } from './AuthContext'

// Define action types
type Action = 'read' | 'write' | 'delete'

// Define resource types
type Resource = 
  | 'dashboard' 
  | 'patients' 
  | 'appointments' 
  | 'doctors' 
  | 'wards' 
  | 'medical_records' 
  | 'claims'
  | 'lab' 
  | 'pharmacy' 
  | 'bills' 
  | 'users' 
  | 'settings' 
  | 'reports'

// Define role permissions structure
type RolePermissions = {
  [K in Resource]: Action[]
}

type Roles = 'admin' | 'doctor' | 'nurse' | 'receptionist'

// Define role hierarchy and permissions
const ROLE_PERMISSIONS: Record<Roles, RolePermissions> = {
  admin: {
    // Admin has full access to everything
    dashboard: ['read'],
    patients: ['read', 'write', 'delete'],
    appointments: ['read', 'write', 'delete'],
    doctors: ['read', 'write', 'delete'],
    wards: ['read', 'write', 'delete'],
    medical_records: ['read', 'write', 'delete'],
    claims: ['read', 'write', 'delete'],
    lab: ['read', 'write', 'delete'],
    pharmacy: ['read', 'write', 'delete'],
    bills: ['read', 'write', 'delete'],
    users: ['read', 'write', 'delete'],
    settings: ['read', 'write', 'delete'],
    reports: ['read', 'write', 'delete'],
  },
  doctor: {
    // Doctors have extensive access but limited user management
    dashboard: ['read'],
    patients: ['read', 'write', 'delete'],
    appointments: ['read', 'write', 'delete'],
    doctors: ['read'],
    wards: ['read', 'write'],
    medical_records: ['read', 'write', 'delete'],
    claims: ['read', 'write'],
    lab: ['read', 'write'],
    pharmacy: ['read', 'write'],
    bills: ['read', 'write'],
    users: [], // No user management access - only admin can manage users
    settings: ['read'],
    reports: ['read', 'write'],
  },
  nurse: {
    // Nurses have operational access but limited deletion rights
    dashboard: ['read'],
    patients: ['read', 'write'],
    appointments: ['read', 'write'],
    doctors: [], // No access to doctors page
    wards: ['read', 'write'],
    medical_records: ['read', 'write'],
    claims: ['read'],
    lab: ['read', 'write'],
    pharmacy: ['read'],
    bills: ['read'],
    users: [], // No user management access
    settings: ['read'],
    reports: ['read'],
  },
  receptionist: {
    // Receptionists have limited access, mainly for front desk operations
    dashboard: ['read'],
    patients: ['read', 'write'],
    appointments: ['read', 'write', 'delete'],
    doctors: ['read'],
    wards: ['read'], // Can view wards but not manage them
    medical_records: ['read'],
    claims: ['read', 'write'],
    lab: ['read'],
    pharmacy: ['read'],
    bills: ['read', 'write'],
    users: [], // No user management access
    settings: ['read'],
    reports: [],
  },
}

interface RoleContextType {
  hasPermission: (resource: string, action: string) => boolean
  canAccess: (resource: string) => boolean
  getUserRole: () => string
  isAdmin: () => boolean
  isDoctor: () => boolean
  isNurse: () => boolean
  isReceptionist: () => boolean
}

const RoleContext = createContext<RoleContextType | undefined>(undefined)

export const RoleProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user } = useAuth()

  const getUserRole = (): string => {
    return user?.role || 'receptionist' // Default to most restrictive role
  }

  const hasPermission = (resource: string, action: string): boolean => {
    const userRole = getUserRole() as Roles
    const rolePermissions = ROLE_PERMISSIONS[userRole]
    
    // Fallback for unknown roles
    if (!rolePermissions) {
      return false
    }
    
    const resourceKey = resource as Resource
    if (!rolePermissions[resourceKey]) {
      return false
    }
    
    const actionKey = action as Action
    return rolePermissions[resourceKey].includes(actionKey)
  }

  const canAccess = (resource: string): boolean => {
    const userRole = getUserRole() as Roles
    const rolePermissions = ROLE_PERMISSIONS[userRole]
    
    // Fallback for unknown roles
    if (!rolePermissions) {
      return false
    }
    
    const resourceKey = resource as Resource
    if (!rolePermissions[resourceKey]) {
      return false
    }
    
    return rolePermissions[resourceKey].length > 0
  }

  const isAdmin = (): boolean => getUserRole() === 'admin'
  const isDoctor = (): boolean => getUserRole() === 'doctor'
  const isNurse = (): boolean => getUserRole() === 'nurse'
  const isReceptionist = (): boolean => getUserRole() === 'receptionist'

  const value: RoleContextType = {
    hasPermission,
    canAccess,
    getUserRole,
    isAdmin,
    isDoctor,
    isNurse,
    isReceptionist,
  }

  return <RoleContext.Provider value={value}>{children}</RoleContext.Provider>
}

export const useRole = (): RoleContextType => {
  const context = useContext(RoleContext)
  if (context === undefined) {
    throw new Error('useRole must be used within a RoleProvider')
  }
  return context
}

// Helper components for conditional rendering
interface ProtectedComponentProps {
  resource: string
  action?: string
  fallback?: React.ReactNode
  children: React.ReactNode
}

export const ProtectedComponent: React.FC<ProtectedComponentProps> = ({
  resource,
  action = 'read',
  fallback = null,
  children,
}) => {
  const { hasPermission } = useRole()
  
  if (!hasPermission(resource, action)) {
    return <>{fallback}</>
  }
  
  return <>{children}</>
}

interface RoleGuardProps {
  allowedRoles: string[]
  fallback?: React.ReactNode
  children: React.ReactNode
}

export const RoleGuard: React.FC<RoleGuardProps> = ({
  allowedRoles,
  fallback = null,
  children,
}) => {
  const { getUserRole } = useRole()
  
  if (!allowedRoles.includes(getUserRole())) {
    return <>{fallback}</>
  }
  
  return <>{children}</>
}

export default RoleContext
