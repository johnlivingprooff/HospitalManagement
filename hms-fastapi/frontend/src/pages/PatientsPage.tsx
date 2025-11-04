import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { useNavigate } from 'react-router-dom'
import { Plus, Edit, Eye, UserX, UserCheck } from 'lucide-react'
import { Patient } from '../types'
import api from '../lib/api'
import Modal from '../components/Modal'
import SearchInput from '../components/SearchInput'
import { useClientSearch } from '../hooks/useOptimizedSearch'
import { LoadingPatientsOverview } from '../components/loading/PatientLoadingStates'

const PatientsPage = () => {
  const [searchTerm, setSearchTerm] = useState('')
  const [showAddModal, setShowAddModal] = useState(false)
  const [showEditModal, setShowEditModal] = useState(false)
  const [editingPatient, setEditingPatient] = useState<Patient | null>(null)
  const [statusFilter, setStatusFilter] = useState('all')
  const [genderFilter, setGenderFilter] = useState('all')
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  
  const [patientForm, setPatientForm] = useState({
    first_name: '',
    last_name: '',
    email: '',
    phone: '',
    date_of_birth: '',
    gender: '',
    address: ''
  })

  // Fetch all patients with caching
  const { data: allPatients, isLoading, error, isFetching } = useQuery<Patient[]>(
    ['patients'],
    async () => {
      const response = await api.get('/api/patients')
      return response.data
    },
    {
      staleTime: 5 * 60 * 1000, // 5 minutes
      cacheTime: 10 * 60 * 1000, // 10 minutes
    }
  )

  // Apply client-side filtering
  const filteredPatients = useClientSearch(
    allPatients,
    searchTerm,
    ['first_name', 'last_name', 'email', 'phone'] as (keyof Patient)[],
    [
      // Status filter
      (patient) => {
        if (statusFilter === 'all') return true
        if (statusFilter === 'active') {
          return patient.is_active === true
        }
        if (statusFilter === 'inactive') {
          return patient.is_active === false
        }
        return true
      },
      // Gender filter - compare by first letter
      (patient) => {
        if (genderFilter === 'all') return true
        if (!patient.gender) return false
        
        const patientGenderFirstLetter = patient.gender.charAt(0).toLowerCase()
        const filterFirstLetter = genderFilter.charAt(0).toLowerCase()
        
        return patientGenderFirstLetter === filterFirstLetter
      }
    ]
  )

  const addPatientMutation = useMutation(
    (patientData: typeof patientForm) => api.post('/api/patients', patientData),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['patients'])
        setShowAddModal(false)
        setPatientForm({
          first_name: '',
          last_name: '',
          email: '',
          phone: '',
          date_of_birth: '',
          gender: '',
          address: ''
        })
      },
      onError: (error: any) => {
        console.error('Error adding patient:', error)
        alert('Failed to add patient. Please try again.')
      }
    }
  )

  const handleAddPatient = async (e: React.FormEvent) => {
    e.preventDefault()
    addPatientMutation.mutate(patientForm)
  }

  // Edit patient mutation
  const editPatientMutation = useMutation(
    ({ id, patientData }: { id: number; patientData: typeof patientForm }) =>
      api.put(`/api/patients/${id}`, patientData),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['patients'])
        setShowEditModal(false)
        setEditingPatient(null)
        setPatientForm({
          first_name: '',
          last_name: '',
          email: '',
          phone: '',
          date_of_birth: '',
          gender: '',
          address: ''
        })
      },
      onError: (error: any) => {
        console.error('Error updating patient:', error)
        alert('Failed to update patient. Please try again.')
      }
    }
  )

  // Deactivate patient mutation (toggle is_active status)
  const togglePatientStatusMutation = useMutation(
    ({ id, is_active }: { id: number; is_active: boolean }) =>
      api.put(`/api/patients/${id}`, { is_active }),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['patients'])
      },
      onError: (error: any) => {
        console.error('Error updating patient status:', error)
        alert('Failed to update patient status. Please try again.')
      }
    }
  )

  const handleEditPatient = (patient: Patient) => {
    setEditingPatient(patient)
    setPatientForm({
      first_name: patient.first_name,
      last_name: patient.last_name,
      email: patient.email || '',
      phone: patient.phone || '',
      date_of_birth: patient.date_of_birth || '',
      gender: patient.gender || '',
      address: patient.address || ''
    })
    setShowEditModal(true)
  }

  const handleEditSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (editingPatient) {
      editPatientMutation.mutate({ id: editingPatient.id, patientData: patientForm })
    }
  }

  const handleToggleStatus = (patient: Patient) => {
    const action = patient.is_active ? 'deactivate' : 'activate'
    const confirmMessage = `Are you sure you want to ${action} ${patient.first_name} ${patient.last_name}?`
    
    if (confirm(confirmMessage)) {
      togglePatientStatusMutation.mutate({ id: patient.id, is_active: !patient.is_active })
    }
  }

  if (isLoading || error) {
    return <LoadingPatientsOverview />
  }

  if (error) {
    return (
      <div className="p-4 border border-red-200 rounded-md bg-red-50">
        <p className="text-red-800">Error loading patients. Please try again.</p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Patients</h1>
        <button
          onClick={() => setShowAddModal(true)}
          className="inline-flex items-center px-4 py-2 text-sm font-medium text-white transition-all duration-200 bg-green-600 border border-transparent rounded-lg shadow-sm hover:bg-green-700 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <Plus className="w-4 h-4 mr-2" />
          Add Patient
        </button>
      </div>

      {/* Search and Filters */}
      <div className="p-6 bg-white rounded-lg shadow">
        <div className="flex flex-col gap-4 sm:flex-row">
          <div className="flex-1">
            <SearchInput
              value={searchTerm}
              onChange={setSearchTerm}
              placeholder="Search patients by name, email, or phone..."
              isLoading={isFetching}
              className="w-full"
            />
          </div>
          <div className="flex gap-2">
            <select 
              className={`input ${statusFilter !== 'all' ? 'ring-2 ring-blue-500 ring-opacity-50' : ''}`}
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="all">All Patients</option>
              <option value="active">Active</option>
              <option value="inactive">Inactive</option>
            </select>
            <select 
              className={`input ${genderFilter !== 'all' ? 'ring-2 ring-blue-500 ring-opacity-50' : ''}`}
              value={genderFilter}
              onChange={(e) => setGenderFilter(e.target.value)}
            >
              <option value="all">All Genders</option>
              <option value="male">Male</option>
              <option value="female">Female</option>
              <option value="other">Other</option>
            </select>
          </div>
        </div>
      </div>

      {/* Patients Table */}
      <div className="overflow-hidden bg-white rounded-lg shadow">
        <div className="px-6 py-4 border-b border-gray-200">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-medium text-gray-900">
              Patients ({filteredPatients?.length || 0} 
              {filteredPatients?.length !== allPatients?.length && allPatients?.length ? 
                ` of ${allPatients.length}` : ''})
            </h2>
            <div className="flex items-center justify-between">
              <div className="text-sm text-gray-500">
                {searchTerm && `Searching: "${searchTerm}"`}
                {statusFilter !== 'all' && ` • Status: ${statusFilter}`}
                {genderFilter !== 'all' && ` • Gender: ${genderFilter}`}
              </div>
              
              {(statusFilter !== 'all' || genderFilter !== 'all' || searchTerm) && (
                <button
                  onClick={() => {
                    setStatusFilter('all')
                    setGenderFilter('all')
                    setSearchTerm('')
                  }}
                  className="group relative px-3 py-1.5 text-xs font-medium text-white bg-gradient-to-r from-red-500 to-pink-500 hover:from-red-600 hover:to-pink-600 rounded-md shadow-sm hover:shadow-md transform hover:scale-105 transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-red-300 focus:ring-offset-1"
                >
                  <span className="flex items-center space-x-1.5">
                    <svg 
                      className="w-3 h-3 transition-transform duration-200 group-hover:rotate-12" 
                      fill="none" 
                      stroke="currentColor" 
                      viewBox="0 0 24 24"
                    >
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                    </svg>
                    <span>Clear</span>
                  </span>
                  <div className="absolute inset-0 transition-opacity duration-200 bg-white rounded-md opacity-0 group-hover:opacity-20"></div>
                </button>
              )}
            </div>
          </div>
        </div>
        
        {filteredPatients?.length === 0 ? (
          <div className="p-6 text-center text-gray-500">
            <div className="mb-2">No patients found</div>
            {(searchTerm || statusFilter !== 'all' || genderFilter !== 'all') && (
              <div className="text-sm">
                Try adjusting your search or filter criteria
              </div>
            )}
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                  Patient
                </th>
                <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                  Contact
                </th>
                <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                  Date of Birth
                </th>
                <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                  Gender
                </th>
                <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                  Status
                </th>
                <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {filteredPatients?.map((patient) => (
                <tr key={patient.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div>
                      <div className="text-sm font-medium text-gray-900">
                        {patient.first_name} {patient.last_name}
                      </div>
                      <div className="text-sm text-gray-500">ID: {patient.id}</div>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">{patient.email}</div>
                    <div className="text-sm text-gray-500">{patient.phone}</div>
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-900 whitespace-nowrap">
                    {patient.date_of_birth
                      ? new Date(patient.date_of_birth).toLocaleDateString()
                      : 'N/A'}
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-900 whitespace-nowrap">
                    {patient.gender || 'N/A'}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                      patient.is_active
                        ? 'bg-green-100 text-green-800'
                        : 'bg-red-100 text-red-800'
                    }`}>
                      {patient.is_active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-sm font-medium whitespace-nowrap">
                    <div className="flex space-x-2">
                      <button 
                        onClick={() => navigate(`/patients/${patient.id}`)}
                        className="flex items-center justify-center p-2 text-blue-700 transition-colors duration-200 bg-blue-100 rounded-lg hover:bg-blue-200"
                        title="View Details"
                      >
                        <Eye className="w-4 h-4" />
                      </button>
                      <button 
                        onClick={() => handleEditPatient(patient)}
                        className="flex items-center justify-center p-2 text-green-700 transition-colors duration-200 bg-green-100 rounded-lg hover:bg-green-200"
                        title="Edit Patient"
                        disabled={editPatientMutation.isLoading}
                      >
                        <Edit className="w-4 h-4" />
                      </button>
                      <button 
                        onClick={() => handleToggleStatus(patient)}
                        className={`flex items-center justify-center p-2 transition-colors duration-200 rounded-lg ${
                          patient.is_active 
                            ? 'text-red-700 bg-red-100 hover:bg-red-200' 
                            : 'text-green-700 bg-green-100 hover:bg-green-200'
                        }`}
                        title={patient.is_active ? 'Deactivate Patient' : 'Activate Patient'}
                        disabled={togglePatientStatusMutation.isLoading}
                      >
                        {patient.is_active ? (
                          <UserX className="w-4 h-4" />
                        ) : (
                          <UserCheck className="w-4 h-4" />
                        )}
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

      {/* Add Patient Modal */}
      <Modal
        isOpen={showAddModal}
        onClose={() => setShowAddModal(false)}
        title="Add New Patient"
        size="max-w-3xl"
      >
        <form onSubmit={handleAddPatient} className="space-y-6">
          <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
            <div>
              <label className="block mb-2 text-sm font-semibold text-gray-700">
                First Name <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                required
                className="w-full px-4 py-3 text-sm transition-all duration-200 border-2 border-gray-300 rounded-lg outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-100"
                value={patientForm.first_name}
                onChange={(e) => setPatientForm({...patientForm, first_name: e.target.value})}
                placeholder="Enter first name"
              />
            </div>
            <div>
              <label className="block mb-2 text-sm font-semibold text-gray-700">
                Last Name <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                required
                className="w-full px-4 py-3 text-sm transition-all duration-200 border-2 border-gray-300 rounded-lg outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-100"
                value={patientForm.last_name}
                onChange={(e) => setPatientForm({...patientForm, last_name: e.target.value})}
                placeholder="Enter last name"
              />
            </div>
          </div>
          
          <div>
            <label className="block mb-2 text-sm font-semibold text-gray-700">
              Email
            </label>
            <input
              type="email"
              className="w-full px-4 py-3 text-sm transition-all duration-200 border-2 border-gray-300 rounded-lg outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-100"
              value={patientForm.email}
              onChange={(e) => setPatientForm({...patientForm, email: e.target.value})}
              placeholder="patient@example.com"
            />
          </div>
          
          <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
            <div>
              <label className="block mb-2 text-sm font-semibold text-gray-700">
                Phone
              </label>
              <input
                type="tel"
                className="w-full px-4 py-3 text-sm transition-all duration-200 border-2 border-gray-300 rounded-lg outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-100"
                value={patientForm.phone}
                onChange={(e) => setPatientForm({...patientForm, phone: e.target.value})}
                placeholder="+1 (555) 123-4567"
              />
            </div>
            <div>
              <label className="block mb-2 text-sm font-semibold text-gray-700">
                Date of Birth
              </label>
              <input
                type="date"
                className="w-full px-4 py-3 text-sm transition-all duration-200 border-2 border-gray-300 rounded-lg outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-100"
                value={patientForm.date_of_birth}
                onChange={(e) => setPatientForm({...patientForm, date_of_birth: e.target.value})}
              />
            </div>
          </div>
          
          <div>
            <label className="block mb-2 text-sm font-semibold text-gray-700">
              Gender
            </label>
            <select
              className="w-full px-4 py-3 text-sm transition-all duration-200 border-2 border-gray-300 rounded-lg outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-100"
              value={patientForm.gender}
              onChange={(e) => setPatientForm({...patientForm, gender: e.target.value})}
            >
              <option value="">Select Gender</option>
              <option value="M">Male</option>
              <option value="F">Female</option>
              <option value="O">Other</option>
            </select>
          </div>
          
          <div>
            <label className="block mb-2 text-sm font-semibold text-gray-700">
              Address
            </label>
            <textarea
              className="w-full px-4 py-3 text-sm transition-all duration-200 border-2 border-gray-300 rounded-lg outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-100 resize-none"
              rows={3}
              value={patientForm.address}
              onChange={(e) => setPatientForm({...patientForm, address: e.target.value})}
              placeholder="Enter patient's address"
            />
          </div>
          
          <div className="flex justify-end gap-4 pt-6 border-t-2 border-primary-100">
            <button
              type="button"
              onClick={() => setShowAddModal(false)}
              className="px-6 py-3 text-sm font-semibold text-gray-700 transition-all duration-200 border-2 border-gray-300 rounded-lg hover:bg-gray-50 hover:border-gray-400 focus:outline-none focus:ring-2 focus:ring-gray-300"
            >
              Cancel
            </button>
            <button 
              type="submit" 
              className="px-6 py-3 text-sm font-semibold text-white transition-all duration-200 rounded-lg shadow-md bg-primary-500 hover:bg-primary-600 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-primary-300 disabled:opacity-50 disabled:cursor-not-allowed"
              disabled={addPatientMutation.isLoading}
            >
              {addPatientMutation.isLoading ? 'Adding...' : 'Add Patient'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Edit Patient Modal */}
      <Modal
        isOpen={showEditModal}
        onClose={() => {
          setShowEditModal(false)
          setEditingPatient(null)
          setPatientForm({
            first_name: '',
            last_name: '',
            email: '',
            phone: '',
            date_of_birth: '',
            gender: '',
            address: ''
          })
        }}
        title={`Edit Patient - ${editingPatient?.first_name} ${editingPatient?.last_name}`}
        size="max-w-3xl"
      >
        <form onSubmit={handleEditSubmit} className="space-y-6">
          <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
            <div>
              <label className="block mb-2 text-sm font-semibold text-gray-700">
                First Name <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                required
                className="w-full px-4 py-3 text-sm transition-all duration-200 border-2 border-gray-300 rounded-lg outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-100"
                value={patientForm.first_name}
                onChange={(e) => setPatientForm({...patientForm, first_name: e.target.value})}
                placeholder="Enter first name"
              />
            </div>
            
            <div>
              <label className="block mb-2 text-sm font-semibold text-gray-700">
                Last Name <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                required
                className="w-full px-4 py-3 text-sm transition-all duration-200 border-2 border-gray-300 rounded-lg outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-100"
                value={patientForm.last_name}
                onChange={(e) => setPatientForm({...patientForm, last_name: e.target.value})}
                placeholder="Enter last name"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
            <div>
              <label className="block mb-2 text-sm font-semibold text-gray-700">Email</label>
              <input
                type="email"
                className="w-full px-4 py-3 text-sm transition-all duration-200 border-2 border-gray-300 rounded-lg outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-100"
                value={patientForm.email}
                onChange={(e) => setPatientForm({...patientForm, email: e.target.value})}
                placeholder="patient@example.com"
              />
            </div>
            
            <div>
              <label className="block mb-2 text-sm font-semibold text-gray-700">Phone</label>
              <input
                type="tel"
                className="w-full px-4 py-3 text-sm transition-all duration-200 border-2 border-gray-300 rounded-lg outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-100"
                value={patientForm.phone}
                onChange={(e) => setPatientForm({...patientForm, phone: e.target.value})}
                placeholder="+1 (555) 123-4567"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
            <div>
              <label className="block mb-2 text-sm font-semibold text-gray-700">Date of Birth</label>
              <input
                type="date"
                className="w-full px-4 py-3 text-sm transition-all duration-200 border-2 border-gray-300 rounded-lg outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-100"
                value={patientForm.date_of_birth}
                onChange={(e) => setPatientForm({...patientForm, date_of_birth: e.target.value})}
              />
            </div>
            
            <div>
              <label className="block mb-2 text-sm font-semibold text-gray-700">Gender</label>
              <select
                className="w-full px-4 py-3 text-sm transition-all duration-200 border-2 border-gray-300 rounded-lg outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-100"
                value={patientForm.gender}
                onChange={(e) => setPatientForm({...patientForm, gender: e.target.value})}
              >
                <option value="">Select Gender</option>
                <option value="M">Male</option>
                <option value="F">Female</option>
                <option value="O">Other</option>
              </select>
            </div>
          </div>

          <div>
            <label className="block mb-2 text-sm font-semibold text-gray-700">Address</label>
            <textarea
              className="w-full px-4 py-3 text-sm transition-all duration-200 border-2 border-gray-300 rounded-lg outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-100 resize-none"
              placeholder="Enter patient's address"
              rows={3}
              value={patientForm.address}
              onChange={(e) => setPatientForm({...patientForm, address: e.target.value})}
            />
          </div>
          
          <div className="flex justify-end gap-4 pt-6 border-t-2 border-primary-100">
            <button
              type="button"
              onClick={() => {
                setShowEditModal(false)
                setEditingPatient(null)
              }}
              className="px-6 py-3 text-sm font-semibold text-gray-700 transition-all duration-200 border-2 border-gray-300 rounded-lg hover:bg-gray-50 hover:border-gray-400 focus:outline-none focus:ring-2 focus:ring-gray-300"
              disabled={editPatientMutation.isLoading}
            >
              Cancel
            </button>
            <button 
              type="submit" 
              className="px-6 py-3 text-sm font-semibold text-white transition-all duration-200 rounded-lg shadow-md bg-primary-500 hover:bg-primary-600 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-primary-300 disabled:opacity-50 disabled:cursor-not-allowed"
              disabled={editPatientMutation.isLoading}
            >
              {editPatientMutation.isLoading ? 'Updating...' : 'Update Patient'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}

export default PatientsPage
