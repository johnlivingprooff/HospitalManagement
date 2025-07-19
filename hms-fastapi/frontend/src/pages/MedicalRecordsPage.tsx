import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { useNavigate } from 'react-router-dom'
import { FileTextIcon, CalendarIcon, UserIcon, Plus } from 'lucide-react'
import api from '../lib/api'
import Modal from '../components/Modal'
import SearchInput from '../components/SearchInput'
import { useClientSearch } from '../hooks/useOptimizedSearch'

interface MedicalRecord {
  id: number
  patient_id: number
  doctor_id: number
  appointment_id?: number
  record_type: string
  title: string
  description: string
  diagnosis?: string
  treatment?: string
  medications?: string
  lab_results?: string
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

const MedicalRecordsPage = () => {
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedType, setSelectedType] = useState('all')
  const [showAddModal, setShowAddModal] = useState(false)
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const [recordForm, setRecordForm] = useState({
    patient_id: '',
    doctor_id: '',
    appointment_id: '',
    record_type: '',
    title: '',
    description: '',
    diagnosis: '',
    treatment: '',
    medications: '',
    lab_results: ''
  })

  const { data: records, isLoading, error } = useQuery<MedicalRecord[]>(
    'medical-records',
    async () => {
      const response = await api.get('/api/medical-records')
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

  const addRecordMutation = useMutation(
    (recordData: typeof recordForm) => {
      const payload = {
        ...recordData,
        patient_id: parseInt(recordData.patient_id),
        doctor_id: parseInt(recordData.doctor_id),
        appointment_id: recordData.appointment_id ? parseInt(recordData.appointment_id) : undefined
      }
      return api.post('/api/medical-records', payload)
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries('medical-records')
        setShowAddModal(false)
        setRecordForm({
          patient_id: '',
          doctor_id: '',
          appointment_id: '',
          record_type: '',
          title: '',
          description: '',
          diagnosis: '',
          treatment: '',
          medications: '',
          lab_results: ''
        })
      },
      onError: (error: any) => {
        console.error('Error creating medical record:', error)
        alert('Failed to create medical record. Please try again.')
      }
    }
  )

  const handleAddRecord = async (e: React.FormEvent) => {
    e.preventDefault()
    addRecordMutation.mutate(recordForm)
  }

  const getTypeColor = (type: string) => {
    switch (type.toLowerCase()) {
      case 'consultation': return 'bg-blue-100 text-blue-800'
      case 'lab_result': return 'bg-green-100 text-green-800'
      case 'prescription': return 'bg-purple-100 text-purple-800'
      case 'diagnosis': return 'bg-red-100 text-red-800'
      case 'surgery': return 'bg-orange-100 text-orange-800'
      default: return 'bg-gray-100 text-gray-800'
    }
  }

  const filteredRecords = useClientSearch(
    records,
    searchTerm,
    ['title', 'description', 'diagnosis', 'patient.first_name', 'patient.last_name', 'doctor.first_name', 'doctor.last_name'] as (keyof MedicalRecord)[],
    [
      // Record type filter
      (record) => selectedType === 'all' || record.record_type === selectedType
    ]
  )

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-primary-600"></div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-md p-4">
        <p className="text-red-800">Error loading medical records. Please try again.</p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-900">Medical Records</h1>
        <button 
          className="inline-flex items-center px-4 py-2 text-sm font-medium text-white transition-all duration-200 bg-indigo-600 border border-transparent rounded-lg shadow-sm hover:bg-indigo-700 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
          onClick={() => setShowAddModal(true)}
        >
          <Plus className="h-4 w-4 mr-2" />
          Add New Record
        </button>
      </div>

      {/* Filters */}
      <div className="bg-white p-4 rounded-lg shadow">
        <div className="flex flex-col md:flex-row gap-4">
          <div className="flex-1">
            <SearchInput
              value={searchTerm}
              onChange={setSearchTerm}
              placeholder="Search by patient name, record title, or diagnosis..."
              className="w-full"
            />
          </div>
          <div>
            <select
              className="input"
              value={selectedType}
              onChange={(e) => setSelectedType(e.target.value)}
            >
              <option value="all">All Types</option>
              <option value="consultation">Consultation</option>
              <option value="lab_result">Lab Result</option>
              <option value="prescription">Prescription</option>
              <option value="diagnosis">Diagnosis</option>
              <option value="surgery">Surgery</option>
            </select>
          </div>
        </div>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-white p-6 rounded-lg shadow">
          <div className="flex items-center">
            <FileTextIcon className="h-8 w-8 text-blue-600" />
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Total Records</p>
              <p className="text-2xl font-bold text-gray-900">{records?.length || 0}</p>
            </div>
          </div>
        </div>
        
        <div className="bg-white p-6 rounded-lg shadow">
          <div className="flex items-center">
            <UserIcon className="h-8 w-8 text-green-600" />
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Consultations</p>
              <p className="text-2xl font-bold text-gray-900">
                {records?.filter(r => r.record_type === 'consultation').length || 0}
              </p>
            </div>
          </div>
        </div>
        
        <div className="bg-white p-6 rounded-lg shadow">
          <div className="flex items-center">
            <FileTextIcon className="h-8 w-8 text-purple-600" />
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Lab Results</p>
              <p className="text-2xl font-bold text-gray-900">
                {records?.filter(r => r.record_type === 'lab_result').length || 0}
              </p>
            </div>
          </div>
        </div>
        
        <div className="bg-white p-6 rounded-lg shadow">
          <div className="flex items-center">
            <CalendarIcon className="h-8 w-8 text-orange-600" />
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">This Month</p>
              <p className="text-2xl font-bold text-gray-900">
                {records?.filter(r => new Date(r.created_at).getMonth() === new Date().getMonth()).length || 0}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Records Table */}
      <div className="bg-white shadow rounded-lg overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-lg font-medium text-gray-900">Medical Records ({filteredRecords.length})</h2>
        </div>
        
        {filteredRecords.length === 0 ? (
          <div className="p-6 text-center text-gray-500">
            No medical records found.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Patient
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Record Title
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Type
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Doctor
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Date
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredRecords.map((record) => (
                  <tr key={record.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">
                        {record.patient ? `${record.patient.first_name} ${record.patient.last_name}` : 'Unknown'}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">{record.title}</div>
                      {record.description && (
                        <div className="text-sm text-gray-500 truncate max-w-xs">
                          {record.description}
                        </div>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getTypeColor(record.record_type)}`}>
                        {record.record_type.replace('_', ' ')}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {record.doctor ? `Dr. ${record.doctor.first_name} ${record.doctor.last_name}` : 'N/A'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {new Date(record.created_at).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <div className="flex space-x-2">
                        <button 
                          onClick={() => navigate(`/medical-records/${record.id}`)}
                          className="bg-indigo-100 hover:bg-indigo-200 text-indigo-700 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors duration-200"
                        >
                          View
                        </button>
                        <button className="bg-green-100 hover:bg-green-200 text-green-700 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors duration-200">
                          Edit
                        </button>
                        <button 
                          onClick={() => window.print()}
                          className="bg-gray-100 hover:bg-gray-200 text-gray-700 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors duration-200"
                        >
                          Print
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

      {/* Add Medical Record Modal */}
      <Modal
        isOpen={showAddModal}
        onClose={() => setShowAddModal(false)}
        title="Add New Medical Record"
      >
        <form onSubmit={handleAddRecord} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Patient
              </label>
              <select 
                className="input"
                value={recordForm.patient_id}
                onChange={(e) => setRecordForm({...recordForm, patient_id: e.target.value})}
                required
              >
                <option value="">Select Patient</option>
                {patients?.map((patient: any) => (
                  <option key={patient.id} value={patient.id}>
                    {patient.first_name} {patient.last_name}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Doctor
              </label>
              <select 
                className="input"
                value={recordForm.doctor_id}
                onChange={(e) => setRecordForm({...recordForm, doctor_id: e.target.value})}
                required
              >
                <option value="">Select Doctor</option>
                {doctors?.map((doctor: any) => (
                  <option key={doctor.id} value={doctor.id}>
                    Dr. {doctor.first_name} {doctor.last_name}
                  </option>
                ))}
              </select>
            </div>
          </div>
          
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Record Type
              </label>
              <select 
                className="input"
                value={recordForm.record_type}
                onChange={(e) => setRecordForm({...recordForm, record_type: e.target.value})}
                required
              >
                <option value="">Select Type</option>
                <option value="consultation">Consultation</option>
                <option value="lab_result">Lab Result</option>
                <option value="prescription">Prescription</option>
                <option value="diagnosis">Diagnosis</option>
                <option value="surgery">Surgery</option>
                <option value="follow_up">Follow-up</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Appointment ID (Optional)
              </label>
              <input
                type="number"
                className="input"
                value={recordForm.appointment_id}
                onChange={(e) => setRecordForm({...recordForm, appointment_id: e.target.value})}
                placeholder="Appointment ID"
              />
            </div>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Title
            </label>
            <input
              type="text"
              required
              className="input"
              value={recordForm.title}
              onChange={(e) => setRecordForm({...recordForm, title: e.target.value})}
              placeholder="Record title"
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Description
            </label>
            <textarea
              className="input"
              rows={3}
              value={recordForm.description}
              onChange={(e) => setRecordForm({...recordForm, description: e.target.value})}
              placeholder="Detailed description..."
              required
            />
          </div>
          
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Diagnosis
              </label>
              <textarea
                className="input"
                rows={2}
                value={recordForm.diagnosis}
                onChange={(e) => setRecordForm({...recordForm, diagnosis: e.target.value})}
                placeholder="Diagnosis details..."
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Treatment
              </label>
              <textarea
                className="input"
                rows={2}
                value={recordForm.treatment}
                onChange={(e) => setRecordForm({...recordForm, treatment: e.target.value})}
                placeholder="Treatment plan..."
              />
            </div>
          </div>
          
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Medications
              </label>
              <textarea
                className="input"
                rows={2}
                value={recordForm.medications}
                onChange={(e) => setRecordForm({...recordForm, medications: e.target.value})}
                placeholder="Prescribed medications..."
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Lab Results
              </label>
              <textarea
                className="input"
                rows={2}
                value={recordForm.lab_results}
                onChange={(e) => setRecordForm({...recordForm, lab_results: e.target.value})}
                placeholder="Lab test results..."
              />
            </div>
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
              className="inline-flex items-center px-4 py-2 text-sm font-medium text-white transition-all duration-200 bg-indigo-600 border border-transparent rounded-lg shadow-sm hover:bg-indigo-700 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
              disabled={addRecordMutation.isLoading}
            >
              {addRecordMutation.isLoading && (
                <svg className="w-4 h-4 mr-2 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="m4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
              )}
              {addRecordMutation.isLoading ? 'Creating...' : 'Create Record'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}

export default MedicalRecordsPage
