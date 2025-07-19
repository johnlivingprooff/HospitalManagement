import React from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { ArrowLeft, TestTube2, User, Stethoscope, Calendar, Clock, FileText } from 'lucide-react'
import api from '../lib/api'

interface LabTest {
  id: number
  patient_id: number
  doctor_id: number
  test_name: string
  test_type: string
  status: string
  ordered_date: string
  completed_date?: string
  results?: string
  notes?: string
  patient: {
    first_name: string
    last_name: string
    email: string
  }
  doctor: {
    first_name: string
    last_name: string
    specialization?: string
  }
}

const LabTestDetailPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const { data: test, isLoading, error } = useQuery<LabTest>(
    ['lab-test', id],
    async () => {
      const response = await api.get(`/api/lab-tests/${id}`)
      return response.data
    },
    {
      enabled: !!id
    }
  )

  const updateStatusMutation = useMutation(
    (newStatus: string) => api.put(`/api/lab-tests/${id}`, { status: newStatus }),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['lab-test', id])
        queryClient.invalidateQueries('lab-tests')
      }
    }
  )

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'pending': return 'bg-yellow-100 text-yellow-800'
      case 'in_progress': return 'bg-blue-100 text-blue-800'
      case 'completed': return 'bg-green-100 text-green-800'
      case 'cancelled': return 'bg-red-100 text-red-800'
      default: return 'bg-gray-100 text-gray-800'
    }
  }

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-cyan-600"></div>
      </div>
    )
  }

  if (error || !test) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-md p-4">
        <p className="text-red-800">Failed to load lab test details</p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center space-x-4">
        <button
          onClick={() => navigate('/lab')}
          className="bg-gray-100 hover:bg-gray-200 p-2 rounded-lg transition-colors duration-200"
        >
          <ArrowLeft className="h-5 w-5" />
        </button>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            Lab Test #{test.id}
          </h1>
          <p className="text-gray-600">{test.test_name}</p>
        </div>
      </div>

      {/* Lab Test Information Cards */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Test Information */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
            <TestTube2 className="h-5 w-5 mr-2 text-cyan-600" />
            Test Information
          </h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Test Name</label>
              <p className="text-gray-900 font-semibold">{test.test_name}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Test Type</label>
              <p className="text-gray-900">{test.test_type}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Status</label>
              <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(test.status)}`}>
                {test.status.replace('_', ' ').charAt(0).toUpperCase() + test.status.replace('_', ' ').slice(1)}
              </span>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Ordered Date</label>
              <p className="text-gray-900 flex items-center">
                <Calendar className="h-4 w-4 mr-2 text-gray-500" />
                {new Date(test.ordered_date).toLocaleDateString()}
              </p>
            </div>
            {test.completed_date && (
              <div>
                <label className="block text-sm font-medium text-gray-700">Completed Date</label>
                <p className="text-gray-900 flex items-center">
                  <Calendar className="h-4 w-4 mr-2 text-gray-500" />
                  {new Date(test.completed_date).toLocaleDateString()}
                </p>
              </div>
            )}
          </div>
        </div>

        {/* Patient & Doctor Information */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
            <User className="h-5 w-5 mr-2 text-cyan-600" />
            Patient & Doctor
          </h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Patient</label>
              <p className="text-gray-900">
                {test.patient.first_name} {test.patient.last_name}
              </p>
              <p className="text-sm text-gray-500">{test.patient.email}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Ordering Doctor</label>
              <p className="text-gray-900 flex items-center">
                <Stethoscope className="h-4 w-4 mr-2 text-gray-500" />
                Dr. {test.doctor.first_name} {test.doctor.last_name}
              </p>
              {test.doctor.specialization && (
                <p className="text-sm text-gray-500">{test.doctor.specialization}</p>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Results Section */}
      {test.results && (
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
            <FileText className="h-5 w-5 mr-2 text-cyan-600" />
            Test Results
          </h2>
          <div className="bg-green-50 border border-green-200 rounded-lg p-4">
            <p className="text-gray-900 whitespace-pre-wrap">{test.results}</p>
          </div>
        </div>
      )}

      {/* Notes Section */}
      {test.notes && (
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Notes</h2>
          <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
            <p className="text-gray-900 whitespace-pre-wrap">{test.notes}</p>
          </div>
        </div>
      )}

      {/* Action Buttons */}
      <div className="flex justify-end space-x-4">
        {test.status === 'pending' && (
          <button
            onClick={() => updateStatusMutation.mutate('in_progress')}
            className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg transition-colors duration-200"
            disabled={updateStatusMutation.isLoading}
          >
            Start Test
          </button>
        )}
        {test.status === 'in_progress' && (
          <button
            onClick={() => updateStatusMutation.mutate('completed')}
            className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-lg transition-colors duration-200"
            disabled={updateStatusMutation.isLoading}
          >
            Complete Test
          </button>
        )}
        {test.status === 'completed' && (
          <button
            onClick={() => window.print()}
            className="bg-gray-600 hover:bg-gray-700 text-white px-4 py-2 rounded-lg transition-colors duration-200"
          >
            Print Report
          </button>
        )}
        <button
          onClick={() => navigate(`/lab/edit/${test.id}`)}
          className="bg-cyan-600 hover:bg-cyan-700 text-white px-4 py-2 rounded-lg transition-colors duration-200"
        >
          Edit Test
        </button>
      </div>
    </div>
  )
}

export default LabTestDetailPage
