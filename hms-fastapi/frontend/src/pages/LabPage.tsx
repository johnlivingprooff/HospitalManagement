import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { useNavigate } from 'react-router-dom'
import { TestTubeIcon, ClockIcon, CheckCircleIcon, AlertCircleIcon, Plus } from 'lucide-react'
import api from '../lib/api'
import Modal from '../components/Modal'
import SearchInput from '../components/SearchInput'
import { useClientSearch } from '../hooks/useOptimizedSearch'

interface LabTest {
  id: number
  patient_id: number
  doctor_id: number
  test_name: string
  test_type: string
  status: string
  result?: string
  normal_range?: string
  notes?: string
  ordered_date: string
  completed_date?: string
  patient?: {
    first_name: string
    last_name: string
  }
  doctor?: {
    first_name: string
    last_name: string
  }
}

const LabPage = () => {
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedStatus, setSelectedStatus] = useState('all')
  const [showAddModal, setShowAddModal] = useState(false)
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const [labTestForm, setLabTestForm] = useState({
    patient_id: '',
    doctor_id: '',
    test_name: '',
    test_type: '',
    normal_range: '',
    notes: '',
    status: 'pending'
  })

  const { data: labTests, isLoading, error } = useQuery<LabTest[]>(
    'lab-tests',
    async () => {
      const response = await api.get('/api/lab-tests')
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

  const addLabTestMutation = useMutation(
    (testData: typeof labTestForm) => {
      const payload = {
        ...testData,
        patient_id: parseInt(testData.patient_id),
        doctor_id: parseInt(testData.doctor_id)
      }
      return api.post('/api/lab-tests', payload)
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries('lab-tests')
        setShowAddModal(false)
        setLabTestForm({
          patient_id: '',
          doctor_id: '',
          test_name: '',
          test_type: '',
          normal_range: '',
          notes: '',
          status: 'pending'
        })
      },
      onError: (error: any) => {
        console.error('Error creating lab test:', error)
        alert('Failed to create lab test. Please try again.')
      }
    }
  )

  const handleAddLabTest = async (e: React.FormEvent) => {
    e.preventDefault()
    addLabTestMutation.mutate(labTestForm)
  }

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'pending': return 'bg-yellow-100 text-yellow-800'
      case 'in_progress': return 'bg-blue-100 text-blue-800'
      case 'completed': return 'bg-green-100 text-green-800'
      case 'cancelled': return 'bg-red-100 text-red-800'
      case 'abnormal': return 'bg-orange-100 text-orange-800'
      default: return 'bg-gray-100 text-gray-800'
    }
  }

  const getStatusIcon = (status: string) => {
    switch (status.toLowerCase()) {
      case 'pending': return <ClockIcon className="w-4 h-4" />
      case 'in_progress': return <TestTubeIcon className="w-4 h-4" />
      case 'completed': return <CheckCircleIcon className="w-4 h-4" />
      case 'abnormal': return <AlertCircleIcon className="w-4 h-4" />
      default: return <ClockIcon className="w-4 h-4" />
    }
  }

  const filteredTests = useClientSearch(
    labTests,
    searchTerm,
    ['test_name', 'test_type', 'notes', 'result'],
    [
      // Status filter
      (test) => selectedStatus === 'all' || test.status === selectedStatus,
      // Manual search for nested patient and doctor fields
      (test) => {
        if (!searchTerm) return true
        const searchLower = searchTerm.toLowerCase()
        
        // Search in patient name
        const patientMatch = test.patient ? 
          `${test.patient.first_name} ${test.patient.last_name}`.toLowerCase().includes(searchLower) : false
        
        // Search in doctor name
        const doctorMatch = test.doctor ? 
          `${test.doctor.first_name} ${test.doctor.last_name}`.toLowerCase().includes(searchLower) : false
        
        return patientMatch || doctorMatch
      }
    ]
  )

  const updateStatus = async (id: number, newStatus: string) => {
    try {
      await api.put(`/api/lab-tests/${id}`, { status: newStatus })
      queryClient.invalidateQueries('lab-tests')
    } catch (error) {
      console.error('Error updating lab test status:', error)
    }
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="w-32 h-32 border-b-2 rounded-full animate-spin border-primary-600"></div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="p-4 border border-red-200 rounded-md bg-red-50">
        <p className="text-red-800">Error loading lab tests. Please try again.</p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Laboratory</h1>
        <button 
          className="inline-flex items-center px-4 py-2 text-sm font-medium text-white transition-all duration-200 border border-transparent rounded-lg shadow-sm bg-cyan-600 hover:bg-cyan-700 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-cyan-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
          onClick={() => setShowAddModal(true)}
        >
          <Plus className="w-4 h-4 mr-2" />
          Order New Test
        </button>
      </div>

      {/* Filters */}
      <div className="p-4 bg-white rounded-lg shadow">
        <div className="flex flex-col gap-4 md:flex-row">
          <div className="flex-1">
            <SearchInput
              value={searchTerm}
              onChange={setSearchTerm}
              placeholder="Search by test name, type, patient, or doctor..."
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
              <option value="in_progress">In Progress</option>
              <option value="completed">Completed</option>
              <option value="abnormal">Abnormal</option>
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
                {labTests?.filter(t => t.status === 'pending').length || 0}
              </p>
            </div>
          </div>
        </div>
        
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center">
            <TestTubeIcon className="w-8 h-8 text-blue-600" />
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">In Progress</p>
              <p className="text-2xl font-bold text-gray-900">
                {labTests?.filter(t => t.status === 'in_progress').length || 0}
              </p>
            </div>
          </div>
        </div>
        
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center">
            <CheckCircleIcon className="w-8 h-8 text-green-600" />
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Completed</p>
              <p className="text-2xl font-bold text-gray-900">
                {labTests?.filter(t => t.status === 'completed').length || 0}
              </p>
            </div>
          </div>
        </div>
        
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center">
            <AlertCircleIcon className="w-8 h-8 text-orange-600" />
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Abnormal</p>
              <p className="text-2xl font-bold text-gray-900">
                {labTests?.filter(t => t.status === 'abnormal').length || 0}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Tests Table */}
      <div className="overflow-hidden bg-white rounded-lg shadow">
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-lg font-medium text-gray-900">Lab Tests ({filteredTests.length})</h2>
        </div>
        
        {filteredTests.length === 0 ? (
          <div className="p-6 text-center text-gray-500">
            No lab tests found.
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
                    Test Name
                  </th>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                    Type
                  </th>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                    Status
                  </th>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                    Ordered By
                  </th>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                    Order Date
                  </th>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredTests.map((test) => (
                  <tr key={test.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">
                        {test.patient ? 
                          `${test.patient.first_name} ${test.patient.last_name}` : 
                          'Unknown'
                        }
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">{test.test_name}</div>
                      {test.normal_range && (
                        <div className="text-sm text-gray-500">Normal: {test.normal_range}</div>
                      )}
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-900 whitespace-nowrap">
                      {test.test_type}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <span className={`inline-flex items-center px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(test.status)}`}>
                          {getStatusIcon(test.status)}
                          <span className="ml-1">{test.status.replace('_', ' ')}</span>
                        </span>
                      </div>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-900 whitespace-nowrap">
                      {test.doctor ? 
                        `Dr. ${test.doctor.first_name} ${test.doctor.last_name}` : 
                        'N/A'
                      }
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-900 whitespace-nowrap">
                      {new Date(test.ordered_date).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 text-sm font-medium whitespace-nowrap">
                      <div className="flex space-x-2">
                        {test.status === 'pending' && (
                          <button 
                            onClick={() => updateStatus(test.id, 'in_progress')}
                            className="bg-blue-100 hover:bg-blue-200 text-blue-700 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors duration-200"
                          >
                            Start
                          </button>
                        )}
                        {test.status === 'in_progress' && (
                          <button 
                            onClick={() => updateStatus(test.id, 'completed')}
                            className="bg-green-100 hover:bg-green-200 text-green-700 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors duration-200"
                          >
                            Complete
                          </button>
                        )}
                        <button 
                          onClick={() => navigate(`/lab/${test.id}`)}
                          className="bg-cyan-100 hover:bg-cyan-200 text-cyan-700 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors duration-200"
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

      {/* Order Lab Test Modal */}
      <Modal
        isOpen={showAddModal}
        onClose={() => setShowAddModal(false)}
        title="Order New Lab Test"
      >
        <form onSubmit={handleAddLabTest} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Patient
              </label>
              <select 
                className="input"
                value={labTestForm.patient_id}
                onChange={(e) => setLabTestForm({...labTestForm, patient_id: e.target.value})}
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
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Ordering Doctor
              </label>
              <select 
                className="input"
                value={labTestForm.doctor_id}
                onChange={(e) => setLabTestForm({...labTestForm, doctor_id: e.target.value})}
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
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Test Name
              </label>
              <input
                type="text"
                required
                className="input"
                value={labTestForm.test_name}
                onChange={(e) => setLabTestForm({...labTestForm, test_name: e.target.value})}
                placeholder="e.g., Complete Blood Count"
              />
            </div>
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Test Type
              </label>
              <select 
                className="input"
                value={labTestForm.test_type}
                onChange={(e) => setLabTestForm({...labTestForm, test_type: e.target.value})}
                required
              >
                <option value="">Select Test Type</option>
                <option value="blood">Blood Test</option>
                <option value="urine">Urine Test</option>
                <option value="stool">Stool Test</option>
                <option value="culture">Culture</option>
                <option value="biopsy">Biopsy</option>
                <option value="imaging">Imaging</option>
                <option value="pathology">Pathology</option>
                <option value="microbiology">Microbiology</option>
              </select>
            </div>
          </div>
          
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Normal Range (Optional)
            </label>
            <input
              type="text"
              className="input"
              value={labTestForm.normal_range}
              onChange={(e) => setLabTestForm({...labTestForm, normal_range: e.target.value})}
              placeholder="e.g., 4.5-11.0 x10³/μL"
            />
          </div>
          
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Special Instructions
            </label>
            <textarea
              className="input"
              rows={3}
              value={labTestForm.notes}
              onChange={(e) => setLabTestForm({...labTestForm, notes: e.target.value})}
              placeholder="Any special instructions or notes for the lab..."
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
              className="inline-flex items-center px-4 py-2 text-sm font-medium text-white transition-all duration-200 border border-transparent rounded-lg shadow-sm bg-cyan-600 hover:bg-cyan-700 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-cyan-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
              disabled={addLabTestMutation.isLoading}
            >
              {addLabTestMutation.isLoading && (
                <svg className="w-4 h-4 mr-2 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="m4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
              )}
              {addLabTestMutation.isLoading ? 'Ordering...' : 'Order Test'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}

export default LabPage
