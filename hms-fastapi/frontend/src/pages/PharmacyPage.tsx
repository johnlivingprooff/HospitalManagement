import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { useNavigate } from 'react-router-dom'
import { PillIcon, ClockIcon, CheckCircleIcon, XCircleIcon } from 'lucide-react'
import api from '../lib/api'
import Modal from '../components/Modal'
import SearchInput from '../components/SearchInput'
import { useClientSearch } from '../hooks/useOptimizedSearch'

interface Prescription {
  id: number
  patient_id: number
  doctor_id: number
  medication_name: string
  dosage: string
  frequency: string
  duration: string
  quantity: number
  status: string
  instructions?: string
  created_at: string
  patient?: {
    first_name: string
    last_name: string
  }
  doctor?: {
    first_name: string
    last_name: string
  }
}

const PharmacyPage = () => {
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedStatus, setSelectedStatus] = useState('all')
  const [showAddModal, setShowAddModal] = useState(false)
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const [prescriptionForm, setPrescriptionForm] = useState({
    patient_id: '',
    doctor_id: '',
    medication_name: '',
    dosage: '',
    frequency: '',
    duration: '',
    quantity: '',
    instructions: '',
    status: 'pending'
  })

  const { data: prescriptions, isLoading, error } = useQuery<Prescription[]>(
    'prescriptions',
    async () => {
      const response = await api.get('/api/prescriptions')
      return response.data
    },
    {
      staleTime: 5 * 60 * 1000, // 5 minutes
    }
  )

  // Fetch patients and doctors for dropdowns
  const { data: patients } = useQuery('patients', async () => {
    const response = await api.get('/api/patients')
    return response.data
  })

  const { data: doctors } = useQuery('doctors', async () => {
    const response = await api.get('/api/users?role=doctor')
    return response.data
  })

  const addPrescriptionMutation = useMutation(
    (prescriptionData: typeof prescriptionForm) => {
      const payload = {
        ...prescriptionData,
        patient_id: parseInt(prescriptionData.patient_id),
        doctor_id: parseInt(prescriptionData.doctor_id),
        quantity: parseInt(prescriptionData.quantity)
      }
      return api.post('/api/prescriptions', payload)
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries('prescriptions')
        setShowAddModal(false)
        setPrescriptionForm({
          patient_id: '',
          doctor_id: '',
          medication_name: '',
          dosage: '',
          frequency: '',
          duration: '',
          quantity: '',
          instructions: '',
          status: 'pending'
        })
      },
      onError: (error: any) => {
        console.error('Error creating prescription:', error)
        alert('Failed to create prescription. Please try again.')
      }
    }
  )

  const handleAddPrescription = async (e: React.FormEvent) => {
    e.preventDefault()
    addPrescriptionMutation.mutate(prescriptionForm)
  }

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'pending': return 'bg-yellow-100 text-yellow-800'
      case 'dispensed': return 'bg-green-100 text-green-800'
      case 'ready': return 'bg-blue-100 text-blue-800'
      case 'cancelled': return 'bg-red-100 text-red-800'
      default: return 'bg-gray-100 text-gray-800'
    }
  }

  const getStatusIcon = (status: string) => {
    switch (status.toLowerCase()) {
      case 'pending': return <ClockIcon className="w-4 h-4" />
      case 'dispensed': return <CheckCircleIcon className="w-4 h-4" />
      case 'ready': return <PillIcon className="w-4 h-4" />
      case 'cancelled': return <XCircleIcon className="w-4 h-4" />
      default: return <ClockIcon className="w-4 h-4" />
    }
  }

  const filteredPrescriptions = useClientSearch(
    prescriptions,
    searchTerm,
    ['medication_name', 'dosage', 'instructions'],
    [
      // Status filter
      (prescription) => selectedStatus === 'all' || prescription.status === selectedStatus,
      // Manual search for nested patient and doctor fields
      (prescription) => {
        if (!searchTerm) return true
        const searchLower = searchTerm.toLowerCase()
        
        // Search in patient name
        const patientMatch = prescription.patient ? 
          `${prescription.patient.first_name} ${prescription.patient.last_name}`.toLowerCase().includes(searchLower) : false
        
        // Search in doctor name
        const doctorMatch = prescription.doctor ? 
          `${prescription.doctor.first_name} ${prescription.doctor.last_name}`.toLowerCase().includes(searchLower) : false
        
        return patientMatch || doctorMatch
      }
    ]
  )

  const updatePrescriptionMutation = useMutation(
    ({ id, status }: { id: number; status: string }) =>
      api.put(`/api/prescriptions/${id}`, { status }),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('prescriptions')
      },
      onError: (error: any) => {
        console.error('Error updating prescription status:', error)
        alert('Failed to update prescription status')
      }
    }
  )

  const updateStatus = (id: number, newStatus: string) => {
    updatePrescriptionMutation.mutate({ id, status: newStatus })
  }

  const getNextStatus = (currentStatus: string) => {
    switch (currentStatus.toLowerCase()) {
      case 'pending': return 'ready'
      case 'ready': return 'dispensed'
      case 'dispensed': return 'dispensed' // Already at final status
      default: return 'pending'
    }
  }

  const getNextStatusLabel = (currentStatus: string) => {
    switch (currentStatus.toLowerCase()) {
      case 'pending': return 'Mark Ready'
      case 'ready': return 'Dispense'
      case 'dispensed': return 'Dispensed'
      default: return 'Update Status'
    }
  }

  const getNextStatusColor = (currentStatus: string) => {
    switch (currentStatus.toLowerCase()) {
      case 'pending': return 'bg-blue-100 hover:bg-blue-200 text-blue-700'
      case 'ready': return 'bg-green-100 hover:bg-green-200 text-green-700'
      case 'dispensed': return 'bg-gray-100 text-gray-400 cursor-not-allowed'
      default: return 'bg-purple-100 hover:bg-purple-200 text-purple-700'
    }
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="w-32 h-32 border-b-2 border-purple-600 rounded-full animate-spin"></div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="p-4 border border-red-200 rounded-md bg-red-50">
        <p className="text-red-800">Failed to load prescriptions</p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Pharmacy</h1>
        <button 
          onClick={() => setShowAddModal(true)}
          className="bg-purple-600 hover:bg-purple-700 text-white font-semibold py-2.5 px-6 rounded-lg 
                   shadow-lg hover:shadow-xl transition-all duration-200 transform hover:scale-105 
                   focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2
                   flex items-center space-x-2 group"
        >
          <svg 
            className="w-5 h-5 transition-transform duration-200 group-hover:rotate-90" 
            fill="none" 
            stroke="currentColor" 
            viewBox="0 0 24 24"
          >
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
          <span>New Prescription</span>
          {addPrescriptionMutation.isLoading && (
            <div className="w-4 h-4 ml-1 border-2 border-white rounded-full animate-spin border-t-transparent"></div>
          )}
        </button>
      </div>

      {/* Filters */}
      <div className="p-4 bg-white rounded-lg shadow">
        <div className="flex flex-col gap-4 md:flex-row">
          <div className="flex-1">
            <SearchInput
              value={searchTerm}
              onChange={setSearchTerm}
              placeholder="Search by medication, dosage, patient, or doctor..."
              className="w-full"
            />
          </div>
          <div>
            <select
              className="input"
              value={selectedStatus}
              onChange={(e) => setSelectedStatus(e.target.value)}
            >
              <option value="all">All Status</option>
              <option value="pending">Pending</option>
              <option value="ready">Ready</option>
              <option value="dispensed">Dispensed</option>
              <option value="cancelled">Cancelled</option>
            </select>
          </div>
        </div>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 gap-4 md:grid-cols-4">
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center">
            <ClockIcon className="w-8 h-8 text-yellow-600" />
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Pending</p>
              <p className="text-2xl font-bold text-gray-900">
                {(prescriptions || []).filter(p => p.status === 'pending').length}
              </p>
            </div>
          </div>
        </div>
        
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center">
            <PillIcon className="w-8 h-8 text-blue-600" />
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Ready</p>
              <p className="text-2xl font-bold text-gray-900">
                {(prescriptions || []).filter(p => p.status === 'ready').length}
              </p>
            </div>
          </div>
        </div>
        
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center">
            <CheckCircleIcon className="w-8 h-8 text-green-600" />
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Dispensed</p>
              <p className="text-2xl font-bold text-gray-900">
                {(prescriptions || []).filter(p => p.status === 'dispensed').length}
              </p>
            </div>
          </div>
        </div>
        
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center">
            <PillIcon className="w-8 h-8 text-purple-600" />
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Total Today</p>
              <p className="text-2xl font-bold text-gray-900">
                {(prescriptions || []).filter(p => 
                  new Date(p.created_at).toDateString() === new Date().toDateString()
                ).length}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Prescriptions Table */}
      <div className="overflow-hidden bg-white rounded-lg shadow">
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-lg font-medium text-gray-900">Prescriptions ({filteredPrescriptions.length})</h2>
        </div>
        
        {filteredPrescriptions.length === 0 ? (
          <div className="p-6 text-center text-gray-500">
            No prescriptions found.
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
                    Medication
                  </th>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                    Dosage
                  </th>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                    Quantity
                  </th>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                    Status
                  </th>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                    Doctor
                  </th>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                    Date
                  </th>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredPrescriptions.map((prescription) => (
                  <tr key={prescription.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">
                        {prescription.patient ? 
                          `${prescription.patient.first_name} ${prescription.patient.last_name}` : 
                          'Unknown'
                        }
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">{prescription.medication_name}</div>
                      <div className="text-sm text-gray-500">{prescription.frequency}</div>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-900 whitespace-nowrap">
                      {prescription.dosage}
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-900 whitespace-nowrap">
                      {prescription.quantity}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <span className={`inline-flex items-center px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(prescription.status)}`}>
                          {getStatusIcon(prescription.status)}
                          <span className="ml-1">{prescription.status}</span>
                        </span>
                      </div>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-900 whitespace-nowrap">
                      {prescription.doctor ? 
                        `Dr. ${prescription.doctor.first_name} ${prescription.doctor.last_name}` : 
                        'N/A'
                      }
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-900 whitespace-nowrap">
                      {new Date(prescription.created_at).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 text-sm font-medium whitespace-nowrap">
                      <div className="flex space-x-2">
                        <button 
                          onClick={() => updateStatus(prescription.id, getNextStatus(prescription.status))}
                          disabled={prescription.status === 'dispensed'}
                          className={`px-3 py-1.5 rounded-lg text-sm font-medium transition-colors duration-200 ${getNextStatusColor(prescription.status)}`}
                        >
                          {getNextStatusLabel(prescription.status)}
                        </button>
                        <button 
                          onClick={() => navigate(`/pharmacy/${prescription.id}`)}
                          className="bg-purple-100 hover:bg-purple-200 text-purple-700 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors duration-200"
                        >
                          View
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

      {/* Add Prescription Modal */}
      <Modal
        isOpen={showAddModal}
        onClose={() => setShowAddModal(false)}
        title="Create New Prescription"
      >
        <form onSubmit={handleAddPrescription} className="space-y-4">
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Patient *
              </label>
              <select
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                value={prescriptionForm.patient_id}
                onChange={(e) => setPrescriptionForm({...prescriptionForm, patient_id: e.target.value})}
                required
              >
                <option value="">Select Patient</option>
                {(patients || []).map((patient: any) => (
                  <option key={patient.id} value={patient.id}>
                    {patient.first_name} {patient.last_name} - {patient.email}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Doctor *
              </label>
              <select
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                value={prescriptionForm.doctor_id}
                onChange={(e) => setPrescriptionForm({...prescriptionForm, doctor_id: e.target.value})}
                required
              >
                <option value="">Select Doctor</option>
                {(doctors || []).map((doctor: any) => (
                  <option key={doctor.id} value={doctor.id}>
                    Dr. {doctor.first_name} {doctor.last_name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Medication Name *
              </label>
              <input
                type="text"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                value={prescriptionForm.medication_name}
                onChange={(e) => setPrescriptionForm({...prescriptionForm, medication_name: e.target.value})}
                required
                placeholder="Enter medication name"
              />
            </div>

            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Dosage *
              </label>
              <input
                type="text"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                value={prescriptionForm.dosage}
                onChange={(e) => setPrescriptionForm({...prescriptionForm, dosage: e.target.value})}
                required
                placeholder="e.g., 500mg"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Frequency *
              </label>
              <input
                type="text"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                value={prescriptionForm.frequency}
                onChange={(e) => setPrescriptionForm({...prescriptionForm, frequency: e.target.value})}
                required
                placeholder="e.g., Twice daily"
              />
            </div>

            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Duration *
              </label>
              <input
                type="text"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                value={prescriptionForm.duration}
                onChange={(e) => setPrescriptionForm({...prescriptionForm, duration: e.target.value})}
                required
                placeholder="e.g., 7 days"
              />
            </div>

            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Quantity *
              </label>
              <input
                type="number"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                value={prescriptionForm.quantity}
                onChange={(e) => setPrescriptionForm({...prescriptionForm, quantity: e.target.value})}
                required
                placeholder="Enter quantity"
              />
            </div>
          </div>

          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Instructions
            </label>
            <textarea
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent"
              value={prescriptionForm.instructions}
              onChange={(e) => setPrescriptionForm({...prescriptionForm, instructions: e.target.value})}
              placeholder="Additional instructions for the patient"
              rows={3}
            />
          </div>

          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Status
            </label>
            <select
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent"
              value={prescriptionForm.status}
              onChange={(e) => setPrescriptionForm({...prescriptionForm, status: e.target.value})}
            >
              <option value="pending">Pending</option>
              <option value="ready">Ready</option>
              <option value="dispensed">Dispensed</option>
            </select>
          </div>

          <div className="flex justify-end pt-4 space-x-3">
            <button
              type="button"
              onClick={() => setShowAddModal(false)}
              className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 border border-gray-300 rounded-md hover:bg-gray-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500"
              disabled={addPrescriptionMutation.isLoading}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="flex items-center px-4 py-2 space-x-2 text-sm font-medium text-white bg-purple-600 border border-transparent rounded-md hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500 disabled:opacity-50 disabled:cursor-not-allowed"
              disabled={addPrescriptionMutation.isLoading}
            >
              {addPrescriptionMutation.isLoading && (
                <div className="w-4 h-4 border-2 border-white rounded-full animate-spin border-t-transparent"></div>
              )}
              <span>{addPrescriptionMutation.isLoading ? 'Creating...' : 'Create Prescription'}</span>
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}

export default PharmacyPage
