import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { useNavigate } from 'react-router-dom'
import { 
  Users, 
  UserPlus, 
  Shield, 
  ShieldCheck, 
  ShieldAlert, 
  Edit, 
  Trash2,
  Search,
  Filter,
  MoreVertical,
  Eye,
  UserX
} from 'lucide-react'
import api from '../lib/api'
import Modal from '../components/Modal'
import SearchInput from '../components/SearchInput'
import { useRole } from '../contexts/RoleContext'
import ProtectedPage from '../components/ProtectedPage'
import { useClientSearch } from '../hooks/useOptimizedSearch'

interface User {
  id: number
  username: string
  email: string
  first_name: string
  last_name: string
  role: string
  is_active: boolean
  created_at: string
  last_login?: string
}

const UsersPage = () => {
  const { canAccess, isAdmin } = useRole()
  
  // Check if user has access to users page (only admin)
  if (!canAccess('users')) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <div className="text-gray-400 text-6xl mb-4">🚫</div>
          <h2 className="text-2xl font-semibold text-gray-800 mb-2">Access Denied</h2>
          <p className="text-gray-600">You don't have permission to access the User Management page.</p>
          <p className="text-gray-500 text-sm mt-2">Only administrators can manage users.</p>
        </div>
      </div>
    )
  }

  const [searchTerm, setSearchTerm] = useState('')
  const [roleFilter, setRoleFilter] = useState('all')
  const [statusFilter, setStatusFilter] = useState('all')
  const [showAddModal, setShowAddModal] = useState(false)
  const [showEditModal, setShowEditModal] = useState(false)
  const [showDeleteModal, setShowDeleteModal] = useState(false)
  const [selectedUser, setSelectedUser] = useState<User | null>(null)
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const [userForm, setUserForm] = useState({
    username: '',
    email: '',
    first_name: '',
    last_name: '',
    role: 'nurse',
    password: '',
    confirm_password: ''
  })

  const [editUserForm, setEditUserForm] = useState({
    username: '',
    email: '',
    first_name: '',
    last_name: '',
    role: 'nurse',
    is_active: true
  })

  const { data: users, isLoading, error } = useQuery<User[]>(
    'users',
    async () => {
      const response = await api.get('/api/users')
      return response.data
    },
    {
      staleTime: 2 * 60 * 1000, // 2 minutes
    }
  )

  const addUserMutation = useMutation(
    (userData: typeof userForm) => api.post('/api/users', userData),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('users')
        setShowAddModal(false)
        setUserForm({
          username: '',
          email: '',
          first_name: '',
          last_name: '',
          role: 'nurse',
          password: '',
          confirm_password: ''
        })
      },
      onError: (error: any) => {
        console.error('Error creating user:', error)
        alert('Failed to create user. Please check the details and try again.')
      }
    }
  )

  const editUserMutation = useMutation(
    (userData: { id: number } & typeof editUserForm) => {
      const { id, ...payload } = userData
      return api.put(`/api/users/${id}`, payload)
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries('users')
        setShowEditModal(false)
        setSelectedUser(null)
      },
      onError: (error: any) => {
        console.error('Error updating user:', error)
        alert('Failed to update user. Please try again.')
      }
    }
  )

  const deleteUserMutation = useMutation(
    (userId: number) => api.delete(`/api/users/${userId}`),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('users')
        setShowDeleteModal(false)
        setSelectedUser(null)
      },
      onError: (error: any) => {
        console.error('Error deleting user:', error)
        alert('Failed to delete user. Please try again.')
      }
    }
  )

  const toggleUserStatusMutation = useMutation(
    (userData: { id: number; is_active: boolean }) => 
      api.put(`/api/users/${userData.id}`, { is_active: userData.is_active }),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('users')
      }
    }
  )

  const handleAddUser = async (e: React.FormEvent) => {
    e.preventDefault()
    if (userForm.password !== userForm.confirm_password) {
      alert('Passwords do not match!')
      return
    }
    addUserMutation.mutate(userForm)
  }

  const handleEditUser = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedUser) return
    editUserMutation.mutate({ ...editUserForm, id: selectedUser.id })
  }

  const handleEditClick = (user: User) => {
    setSelectedUser(user)
    setEditUserForm({
      username: user.username,
      email: user.email,
      first_name: user.first_name,
      last_name: user.last_name,
      role: user.role,
      is_active: user.is_active
    })
    setShowEditModal(true)
  }

  const handleDeleteClick = (user: User) => {
    setSelectedUser(user)
    setShowDeleteModal(true)
  }

  const handleDeleteConfirm = () => {
    if (selectedUser) {
      deleteUserMutation.mutate(selectedUser.id)
    }
  }

  const toggleUserStatus = (user: User) => {
    toggleUserStatusMutation.mutate({
      id: user.id,
      is_active: !user.is_active
    })
  }

  const getRoleIcon = (role: string) => {
    switch (role.toLowerCase()) {
      case 'admin':
      case 'doctor':
        return <ShieldCheck className="w-4 h-4 text-red-600" />
      case 'nurse':
        return <Shield className="w-4 h-4 text-blue-600" />
      case 'receptionist':
        return <ShieldAlert className="w-4 h-4 text-green-600" />
      default:
        return <Shield className="w-4 h-4 text-gray-600" />
    }
  }

  const getRoleColor = (role: string) => {
    switch (role.toLowerCase()) {
      case 'admin':
      case 'doctor':
        return 'bg-red-100 text-red-800'
      case 'nurse':
        return 'bg-blue-100 text-blue-800'
      case 'receptionist':
        return 'bg-green-100 text-green-800'
      default:
        return 'bg-gray-100 text-gray-800'
    }
  }

  // Apply client-side filtering and search
  const filteredUsers = useClientSearch(
    users,
    searchTerm,
    ['username', 'email', 'first_name', 'last_name'],
    [
      // Role filter
      (user) => roleFilter === 'all' || user.role === roleFilter,
      // Status filter
      (user) => statusFilter === 'all' || 
        (statusFilter === 'active' && user.is_active) ||
        (statusFilter === 'inactive' && !user.is_active)
    ]
  ) || []

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="w-12 h-12 border-b-2 border-blue-600 rounded-full animate-spin"></div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="p-4 border border-red-200 rounded-md bg-red-50">
        <p className="text-red-800">Error loading users. Please try again.</p>
      </div>
    )
  }

  return (
    <ProtectedPage resource="users" action="read">
      <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">User Management</h1>
          <p className="text-gray-600">Manage system users and their roles</p>
        </div>
        <button 
          onClick={() => setShowAddModal(true)}
          className="inline-flex items-center px-4 py-2 text-sm font-medium text-white transition-all duration-200 bg-blue-600 border border-transparent rounded-lg shadow-sm hover:bg-blue-700 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
        >
          <UserPlus className="w-4 h-4 mr-2" />
          Add New User
        </button>
      </div>

      {/* Search and Filters */}
      <div className="p-4 bg-white rounded-lg shadow">
        <div className="flex flex-col gap-4 md:flex-row">
          <div className="flex-1">
            <SearchInput
              value={searchTerm}
              onChange={setSearchTerm}
              placeholder="Search by name, username, or email..."
              className="w-full"
            />
          </div>
          <div className="flex gap-4">
            <select
              className="input"
              value={roleFilter}
              onChange={(e) => setRoleFilter(e.target.value)}
            >
              <option value="all">All Roles</option>
              <option value="admin">Admin</option>
              <option value="doctor">Doctor</option>
              <option value="nurse">Nurse</option>
              <option value="receptionist">Receptionist</option>
            </select>
            <select
              className="input"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="all">All Status</option>
              <option value="active">Active</option>
              <option value="inactive">Inactive</option>
            </select>
          </div>
        </div>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 gap-4 md:grid-cols-4">
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center">
            <Users className="w-8 h-8 text-blue-600" />
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Total Users</p>
              <p className="text-2xl font-bold text-gray-900">
                {users?.length || 0}
              </p>
            </div>
          </div>
        </div>
        
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center">
            <ShieldCheck className="w-8 h-8 text-red-600" />
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Doctors</p>
              <p className="text-2xl font-bold text-gray-900">
                {users?.filter(u => u.role === 'doctor').length || 0}
              </p>
            </div>
          </div>
        </div>
        
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center">
            <Shield className="w-8 h-8 text-blue-600" />
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Nurses</p>
              <p className="text-2xl font-bold text-gray-900">
                {users?.filter(u => u.role === 'nurse').length || 0}
              </p>
            </div>
          </div>
        </div>
        
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center">
            <ShieldAlert className="w-8 h-8 text-green-600" />
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Receptionists</p>
              <p className="text-2xl font-bold text-gray-900">
                {users?.filter(u => u.role === 'receptionist').length || 0}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Users Table */}
      <div className="overflow-hidden bg-white rounded-lg shadow">
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-lg font-medium text-gray-900">
            Users ({filteredUsers.length})
          </h2>
        </div>
        
        {filteredUsers.length === 0 ? (
          <div className="p-6 text-center text-gray-500">
            No users found matching your criteria.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                    User
                  </th>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                    Role
                  </th>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                    Status
                  </th>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                    Created
                  </th>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                    Last Login
                  </th>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredUsers.map((user) => (
                  <tr key={user.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <div className="flex-shrink-0 w-10 h-10">
                          <div className="flex items-center justify-center w-10 h-10 bg-blue-100 rounded-full">
                            <span className="text-sm font-medium text-blue-600">
                              {user.first_name?.charAt(0)}{user.last_name?.charAt(0)}
                            </span>
                          </div>
                        </div>
                        <div className="ml-4">
                          <div className="text-sm font-medium text-gray-900">
                            {user.first_name} {user.last_name}
                          </div>
                          <div className="text-sm text-gray-500">
                            {user.email}
                          </div>
                          <div className="text-xs text-gray-400">
                            @{user.username}
                          </div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        {getRoleIcon(user.role)}
                        <span className={`ml-2 inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getRoleColor(user.role)}`}>
                          {user.role.charAt(0).toUpperCase() + user.role.slice(1)}
                        </span>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                        user.is_active 
                          ? 'bg-green-100 text-green-800' 
                          : 'bg-red-100 text-red-800'
                      }`}>
                        {user.is_active ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-900 whitespace-nowrap">
                      {new Date(user.created_at).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-500 whitespace-nowrap">
                      {user.last_login ? new Date(user.last_login).toLocaleDateString() : 'Never'}
                    </td>
                    <td className="px-6 py-4 text-sm font-medium whitespace-nowrap">
                      <div className="flex items-center space-x-2">
                        <button 
                          onClick={() => handleEditClick(user)}
                          className="bg-blue-100 hover:bg-blue-200 text-blue-700 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors duration-200"
                        >
                          <Edit className="w-4 h-4" />
                        </button>
                        <button 
                          onClick={() => toggleUserStatus(user)}
                          className={`px-3 py-1.5 rounded-lg text-sm font-medium transition-colors duration-200 ${
                            user.is_active 
                              ? 'bg-orange-100 hover:bg-orange-200 text-orange-700' 
                              : 'bg-green-100 hover:bg-green-200 text-green-700'
                          }`}
                        >
                          {user.is_active ? <UserX className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                        </button>
                        <button 
                          onClick={() => handleDeleteClick(user)}
                          className="bg-red-100 hover:bg-red-200 text-red-700 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors duration-200"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Add User Modal */}
      <Modal
        isOpen={showAddModal}
        onClose={() => setShowAddModal(false)}
        title="Add New User"
      >
        <form onSubmit={handleAddUser} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                First Name
              </label>
              <input
                type="text"
                required
                className="input"
                value={userForm.first_name}
                onChange={(e) => setUserForm({...userForm, first_name: e.target.value})}
                placeholder="John"
              />
            </div>
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Last Name
              </label>
              <input
                type="text"
                required
                className="input"
                value={userForm.last_name}
                onChange={(e) => setUserForm({...userForm, last_name: e.target.value})}
                placeholder="Doe"
              />
            </div>
          </div>
          
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Username
              </label>
              <input
                type="text"
                required
                className="input"
                value={userForm.username}
                onChange={(e) => setUserForm({...userForm, username: e.target.value})}
                placeholder="johndoe"
              />
            </div>
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Email
              </label>
              <input
                type="email"
                required
                className="input"
                value={userForm.email}
                onChange={(e) => setUserForm({...userForm, email: e.target.value})}
                placeholder="john.doe@hospital.com"
              />
            </div>
          </div>
          
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Role
            </label>
            <select 
              className="input"
              value={userForm.role}
              onChange={(e) => setUserForm({...userForm, role: e.target.value})}
              required
            >
              <option value="receptionist">Receptionist</option>
              <option value="nurse">Nurse</option>
              <option value="doctor">Doctor</option>
              <option value="admin">Admin</option>
            </select>
          </div>
          
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Password
              </label>
              <input
                type="password"
                required
                className="input"
                value={userForm.password}
                onChange={(e) => setUserForm({...userForm, password: e.target.value})}
                placeholder="••••••••"
                minLength={6}
              />
            </div>
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Confirm Password
              </label>
              <input
                type="password"
                required
                className="input"
                value={userForm.confirm_password}
                onChange={(e) => setUserForm({...userForm, confirm_password: e.target.value})}
                placeholder="••••••••"
                minLength={6}
              />
            </div>
          </div>
          
          <div className="flex justify-end pt-6 space-x-3 border-t border-gray-200">
            <button
              type="button"
              onClick={() => setShowAddModal(false)}
              className="btn-secondary"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn-primary"
              disabled={addUserMutation.isLoading}
            >
              {addUserMutation.isLoading ? 'Creating...' : 'Create User'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Edit User Modal */}
      <Modal
        isOpen={showEditModal}
        onClose={() => setShowEditModal(false)}
        title="Edit User"
      >
        <form onSubmit={handleEditUser} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                First Name
              </label>
              <input
                type="text"
                required
                className="input"
                value={editUserForm.first_name}
                onChange={(e) => setEditUserForm({...editUserForm, first_name: e.target.value})}
              />
            </div>
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Last Name
              </label>
              <input
                type="text"
                required
                className="input"
                value={editUserForm.last_name}
                onChange={(e) => setEditUserForm({...editUserForm, last_name: e.target.value})}
              />
            </div>
          </div>
          
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Username
              </label>
              <input
                type="text"
                required
                className="input"
                value={editUserForm.username}
                onChange={(e) => setEditUserForm({...editUserForm, username: e.target.value})}
              />
            </div>
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Email
              </label>
              <input
                type="email"
                required
                className="input"
                value={editUserForm.email}
                onChange={(e) => setEditUserForm({...editUserForm, email: e.target.value})}
              />
            </div>
          </div>
          
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Role
              </label>
              <select 
                className="input"
                value={editUserForm.role}
                onChange={(e) => setEditUserForm({...editUserForm, role: e.target.value})}
                required
              >
                <option value="receptionist">Receptionist</option>
                <option value="nurse">Nurse</option>
                <option value="doctor">Doctor</option>
                <option value="admin">Admin</option>
              </select>
            </div>
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Status
              </label>
              <select 
                className="input"
                value={editUserForm.is_active.toString()}
                onChange={(e) => setEditUserForm({...editUserForm, is_active: e.target.value === 'true'})}
              >
                <option value="true">Active</option>
                <option value="false">Inactive</option>
              </select>
            </div>
          </div>
          
          <div className="flex justify-end pt-6 space-x-3 border-t border-gray-200">
            <button
              type="button"
              onClick={() => setShowEditModal(false)}
              className="btn-secondary"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn-primary"
              disabled={editUserMutation.isLoading}
            >
              {editUserMutation.isLoading ? 'Updating...' : 'Update User'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Delete Confirmation Modal */}
      <Modal
        isOpen={showDeleteModal}
        onClose={() => setShowDeleteModal(false)}
        title="Delete User"
      >
        <div className="space-y-4">
          <div className="p-4 border border-red-200 rounded-lg bg-red-50">
            <p className="text-red-800">
              Are you sure you want to delete <strong>{selectedUser?.first_name} {selectedUser?.last_name}</strong>?
              This action cannot be undone.
            </p>
          </div>
          
          <div className="flex justify-end space-x-3">
            <button
              type="button"
              onClick={() => setShowDeleteModal(false)}
              className="btn-secondary"
            >
              Cancel
            </button>
            <button
              onClick={handleDeleteConfirm}
              className="px-4 py-2 text-white transition-colors duration-200 bg-red-600 rounded-lg hover:bg-red-700"
              disabled={deleteUserMutation.isLoading}
            >
              {deleteUserMutation.isLoading ? 'Deleting...' : 'Delete User'}
            </button>
          </div>
        </div>
      </Modal>
    </div>
  </ProtectedPage>
  )
}

export default UsersPage
