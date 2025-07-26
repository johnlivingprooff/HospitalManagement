import { useState } from 'react'
import { Outlet, Link, useLocation } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { useRole } from '../contexts/RoleContext'
import { 
  Home, 
  Users, 
  Calendar, 
  FileText, 
  LogOut,
  Menu,
  X,
  UserCheck,
  CreditCard,
  PillIcon,
  TestTube,
  UserCog,
  Building,
  Bell,
  Settings as SettingsIcon,
  User as UserIcon
} from 'lucide-react'

const Layout = () => {
  const { user, logout } = useAuth()
  const { canAccess, getUserRole } = useRole()
  const location = useLocation()
  const [isSidebarOpen, setIsSidebarOpen] = useState(false)
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false)

  const allNavigation = [
    { name: 'Dashboard', href: '/', icon: Home, resource: 'dashboard' },
    { name: 'Patients', href: '/patients', icon: Users, resource: 'patients' },
    { name: 'Appointments', href: '/appointments', icon: Calendar, resource: 'appointments' },
    { name: 'Doctors', href: '/doctors', icon: UserCheck, resource: 'doctors' },
    { name: 'Wards', href: '/wards', icon: Building, resource: 'wards' },
    { name: 'Medical Records', href: '/medical-records', icon: FileText, resource: 'medical_records' },
    { name: 'Claims', href: '/claims', icon: CreditCard, resource: 'claims' },
    { name: 'Lab', href: '/lab', icon: TestTube, resource: 'lab' },
    { name: 'Pharmacy', href: '/pharmacy', icon: PillIcon, resource: 'pharmacy' },
    { name: 'Bills', href: '/bills', icon: CreditCard, resource: 'bills' },
    { name: 'Users', href: '/users', icon: UserCog, resource: 'users' },
    // Settings tab removed from nav
  ]

  // Filter navigation based on user role
  const navigation = allNavigation.filter(item => canAccess(item.resource))

  const isActive = (path: string) => {
    return location.pathname === path
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Mobile sidebar */}
      <div className={`lg:hidden fixed inset-0 z-50 ${isSidebarOpen ? 'block' : 'hidden'}`}>
        <div className="fixed inset-0 bg-black bg-opacity-50" onClick={() => setIsSidebarOpen(false)} />
        <div className="fixed inset-y-0 left-0 w-64 bg-white shadow-xl">
          <div className="flex items-center justify-between p-4 border-b">
            <div className="flex items-center space-x-3">
              <img src="/icon.svg" alt="HMS Logo" className="w-8 h-8" />
              <h1 className="text-xl font-bold text-gray-900">HMS</h1>
            </div>
            <button onClick={() => setIsSidebarOpen(false)}>
              <X className="w-6 h-6" />
            </button>
          </div>
          <nav className="mt-4">
            {navigation.map((item) => {
              const Icon = item.icon
              return (
                <Link
                  key={item.name}
                  to={item.href}
                  className={`flex items-center px-4 py-3 text-sm font-medium ${
                    isActive(item.href)
                      ? 'bg-primary-50 text-primary-600 border-r-2 border-primary-600'
                      : 'text-gray-700 hover:bg-gray-50'
                  }`}
                  onClick={() => setIsSidebarOpen(false)}
                >
                  <Icon className="w-5 h-5 mr-3" />
                  {item.name}
                </Link>
              )
            })}
          </nav>
        </div>
      </div>

      {/* Desktop sidebar */}
      <div className={`hidden lg:flex lg:flex-col lg:fixed lg:inset-y-0 ${isSidebarCollapsed ? 'lg:w-20' : 'lg:w-64'} lg:bg-white lg:shadow-lg transition-all duration-200`}>
        <div className={`flex items-center ${isSidebarCollapsed ? 'justify-center' : 'justify-between'} p-6 border-b`}>
          <div className={`flex items-center ${isSidebarCollapsed ? 'justify-center' : 'space-x-3'}`}>
            <img src="/icon.svg" alt="HMS Logo" className="w-10 h-10" />
            {!isSidebarCollapsed && <h1 className="text-2xl font-bold text-gray-900">HMS</h1>}
          </div>
          <button
            className={`ml-2 p-2 rounded hover:bg-gray-100 transition ${isSidebarCollapsed ? 'mx-auto' : ''}`}
            onClick={() => setIsSidebarCollapsed(v => !v)}
            aria-label={isSidebarCollapsed ? 'Expand sidebar' : 'Collapse sidebar'}
          >
            {isSidebarCollapsed ? <Menu className="w-6 h-6" /> : <X className="w-6 h-6" />}
          </button>
        </div>
        <nav className={`flex-1 mt-4 flex flex-col ${isSidebarCollapsed ? 'items-center' : ''} relative`}>
          {navigation.map((item) => {
            const Icon = item.icon
            return (
              <Link
                key={item.name}
                to={item.href}
                className={`flex items-center ${isSidebarCollapsed ? 'justify-center px-0' : 'px-6'} py-3 text-sm font-medium transition-all duration-200 ${
                  isActive(item.href)
                    ? 'bg-primary-50 text-primary-600 border-r-2 border-primary-600'
                    : 'text-gray-700 hover:bg-gray-50'
                }`}
                style={isSidebarCollapsed ? { width: '100%', justifyContent: 'center' } : {}}
              >
                <Icon className={`h-5 w-5 ${isSidebarCollapsed ? '' : 'mr-3'}`} />
                {!isSidebarCollapsed && item.name}
              </Link>
            )
          })}
          {/* Sticky user info at bottom */}
          <div className="absolute bottom-0 left-0 w-full">
            <div className={`flex items-center gap-3 px-3 py-4 border-t ${isSidebarCollapsed ? 'justify-center' : ''} ${isSidebarCollapsed ? 'flex-col' : 'flex'} bg-white`}>
              <UserIcon className="text-gray-400 h-7 w-7 min-w-[1.75rem] min-h-[1.75rem]" />
              {!isSidebarCollapsed && (
                <>
                  <div className="flex flex-col flex-1 min-w-0">
                    <span className="font-medium text-gray-900 truncate max-w-[8rem]">{user?.first_name} {user?.last_name}</span>
                    <span className="text-xs text-gray-500 truncate">{getUserRole().charAt(0).toUpperCase() + getUserRole().slice(1)}</span>
                  </div>
                </>
              )}
              <button
                onClick={logout}
                className="flex items-center justify-center p-2 text-red-600 transition rounded bg-red-50 hover:bg-red-100"
                title="Logout"
              >
                <LogOut className="w-5 h-5" />
              </button>
            </div>
          </div>
        </nav>
      </div>

      {/* Main content */}
      <div className={isSidebarCollapsed ? 'lg:ml-20' : 'lg:ml-64'}>
        {/* Top bar */}
        <div className="bg-white border-b shadow-sm">
          <div className="flex items-center justify-between px-4 py-4">
            <div className="flex items-center">
              <button
                className="p-2 lg:hidden"
                onClick={() => setIsSidebarOpen(true)}
              >
                <Menu className="w-6 h-6" />
              </button>
              <h2 className="ml-4 text-xl font-semibold text-gray-900 lg:ml-0">
                {navigation.find(item => isActive(item.href))?.name || 'Dashboard'}
              </h2>
            </div>
            <div className="flex items-center space-x-4">
              <button className="p-2 transition rounded hover:bg-gray-100" title="Notifications">
                <Bell className="w-6 h-6 text-gray-500" />
              </button>
              <button
                className="p-2 transition rounded hover:bg-gray-100"
                title="Settings"
                onClick={() => window.location.href = '/settings'}
              >
                <SettingsIcon className="w-6 h-6 text-gray-500" />
              </button>
            </div>
          </div>
        </div>

        {/* Page content */}
        <main className="p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}

export default Layout
