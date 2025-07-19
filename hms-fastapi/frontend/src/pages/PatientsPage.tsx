import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { useNavigate } from 'react-router-dom'
import { Search, Plus, Edit, Trash2, Eye } from 'lucide-react'
import { Patient } from '../types'
import api from '../lib/api'
import Modal from '../components/Modal'

const PatientsPage = () => {
  const [searchTerm, setSearchTerm] = useState('')
  const [showAddModal, setShowAddModal] = useState(false)
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

  const { data: patients, isLoading, error } = useQuery<Patient[]>(
    ['patients', searchTerm],
    async () => {
      const params = new URLSearchParams()
      if (searchTerm) params.append('search', searchTerm)
      
      const response = await api.get(`/api/patients?${params}`)
      return response.data
    },
    {
      enabled: true,
      staleTime: 5 * 60 * 1000, // 5 minutes
    }
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

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-64">
        <div className="w-12 h-12 border-b-2 rounded-full animate-spin border-primary-600"></div>
      </div>
    )
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
          <div className="relative flex-1">
            <Search className="absolute w-4 h-4 text-gray-400 transform -translate-y-1/2 left-3 top-1/2" />
            <input
              type="text"
              placeholder="Search patients..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10 input"
            />
          </div>
          <div className="flex gap-2">
            <select className="input">
              <option>All Patients</option>
              <option>Active</option>
              <option>Inactive</option>
            </select>
            <select className="input">
              <option>All Genders</option>
              <option>Male</option>
              <option>Female</option>
              <option>Other</option>
            </select>
          </div>
        </div>
      </div>

      {/* Patients Table */}
      <div className="overflow-hidden bg-white rounded-lg shadow">
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
              {patients?.map((patient) => (
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
                        className="bg-blue-100 hover:bg-blue-200 text-blue-700 p-2 rounded-lg transition-colors duration-200 flex items-center justify-center"
                      >
                        <Eye className="w-4 h-4" />
                      </button>
                      <button className="bg-green-100 hover:bg-green-200 text-green-700 p-2 rounded-lg transition-colors duration-200 flex items-center justify-center">
                        <Edit className="w-4 h-4" />
                      </button>
                      <button className="bg-red-100 hover:bg-red-200 text-red-700 p-2 rounded-lg transition-colors duration-200 flex items-center justify-center">
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Add Patient Modal */}
      <Modal
        isOpen={showAddModal}
        onClose={() => setShowAddModal(false)}
        title="Add New Patient"
      >
        <form onSubmit={handleAddPatient} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                First Name
              </label>
              <input
                type="text"
                required
                className="input"
                value={patientForm.first_name}
                onChange={(e) => setPatientForm({...patientForm, first_name: e.target.value})}
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
                value={patientForm.last_name}
                onChange={(e) => setPatientForm({...patientForm, last_name: e.target.value})}
              />
            </div>
          </div>
          
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Email
            </label>
            <input
              type="email"
              className="input"
              value={patientForm.email}
              onChange={(e) => setPatientForm({...patientForm, email: e.target.value})}
            />
          </div>
          
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Phone
              </label>
              <input
                type="tel"
                className="input"
                value={patientForm.phone}
                onChange={(e) => setPatientForm({...patientForm, phone: e.target.value})}
              />
            </div>
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Date of Birth
              </label>
              <input
                type="date"
                className="input"
                value={patientForm.date_of_birth}
                onChange={(e) => setPatientForm({...patientForm, date_of_birth: e.target.value})}
              />
            </div>
          </div>
          
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Gender
            </label>
            <select
              className="input"
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
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Address
            </label>
            <textarea
              className="input"
              rows={3}
              value={patientForm.address}
              onChange={(e) => setPatientForm({...patientForm, address: e.target.value})}
            />
          </div>
          
          <div className="flex justify-end pt-6 space-x-3 border-t border-gray-200">
            <button
              type="button"
              onClick={() => setShowAddModal(false)}
              className="inline-flex items-center px-4 py-2 text-sm font-medium text-gray-700 transition-all duration-200 bg-white border border-gray-300 rounded-lg shadow-sm hover:bg-gray-50 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2"
            >
              Cancel
            </button>
            <button 
              type="submit" 
              className="inline-flex items-center px-4 py-2 text-sm font-medium text-white transition-all duration-200 bg-green-600 border border-transparent rounded-lg shadow-sm hover:bg-green-700 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
              disabled={addPatientMutation.isLoading}
            >
              {addPatientMutation.isLoading && (
                <svg className="w-4 h-4 mr-2 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="m4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
              )}
              {addPatientMutation.isLoading ? 'Adding...' : 'Add Patient'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}

export default PatientsPage
