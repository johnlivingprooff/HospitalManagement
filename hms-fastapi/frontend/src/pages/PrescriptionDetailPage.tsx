import React from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { ArrowLeft, Pill, User, Stethoscope, Calendar, Clock } from 'lucide-react'
import api from '../lib/api'

interface Prescription {
  id: number
  patient_id: number
  doctor_id: number
  medication_name: string
  dosage: string
  frequency: string
  duration: string
  quantity: number
  instructions?: string
  status: string
  created_at: string
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

const PrescriptionDetailPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const { data: prescription, isLoading, error } = useQuery<Prescription>(
    ['prescription', id],
    async () => {
      const response = await api.get(`/api/prescriptions/${id}`)
      return response.data
    },
    {
      enabled: !!id
    }
  )

  const updateStatusMutation = useMutation(
    (newStatus: string) => api.put(`/api/prescriptions/${id}`, { status: newStatus }),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['prescription', id])
        queryClient.invalidateQueries('prescriptions')
      }
    }
  )

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'pending': return 'bg-yellow-100 text-yellow-800'
      case 'ready': return 'bg-blue-100 text-blue-800'
      case 'dispensed': return 'bg-green-100 text-green-800'
      case 'cancelled': return 'bg-red-100 text-red-800'
      default: return 'bg-gray-100 text-gray-800'
    }
  }

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600"></div>
      </div>
    )
  }

  if (error || !prescription) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-md p-4">
        <p className="text-red-800">Failed to load prescription details</p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center space-x-4">
        <button
          onClick={() => navigate('/pharmacy')}
          className="bg-gray-100 hover:bg-gray-200 p-2 rounded-lg transition-colors duration-200"
        >
          <ArrowLeft className="h-5 w-5" />
        </button>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            Prescription #{prescription.id}
          </h1>
          <p className="text-gray-600">{prescription.medication_name}</p>
        </div>
      </div>

      {/* Prescription Information Cards */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Medication Information */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
            <Pill className="h-5 w-5 mr-2 text-purple-600" />
            Medication Information
          </h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Medication</label>
              <p className="text-gray-900 font-semibold">{prescription.medication_name}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Dosage</label>
              <p className="text-gray-900">{prescription.dosage}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Frequency</label>
              <p className="text-gray-900 flex items-center">
                <Clock className="h-4 w-4 mr-2 text-gray-500" />
                {prescription.frequency}
              </p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Duration</label>
              <p className="text-gray-900">{prescription.duration}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Quantity</label>
              <p className="text-gray-900">{prescription.quantity} units</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Status</label>
              <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(prescription.status)}`}>
                {prescription.status.charAt(0).toUpperCase() + prescription.status.slice(1)}
              </span>
            </div>
          </div>
        </div>

        {/* Patient & Doctor Information */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
            <User className="h-5 w-5 mr-2 text-purple-600" />
            Patient & Doctor
          </h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Patient</label>
              <p className="text-gray-900">
                {prescription.patient.first_name} {prescription.patient.last_name}
              </p>
              <p className="text-sm text-gray-500">{prescription.patient.email}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Prescribing Doctor</label>
              <p className="text-gray-900 flex items-center">
                <Stethoscope className="h-4 w-4 mr-2 text-gray-500" />
                Dr. {prescription.doctor.first_name} {prescription.doctor.last_name}
              </p>
              {prescription.doctor.specialization && (
                <p className="text-sm text-gray-500">{prescription.doctor.specialization}</p>
              )}
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Prescribed Date</label>
              <p className="text-gray-900 flex items-center">
                <Calendar className="h-4 w-4 mr-2 text-gray-500" />
                {new Date(prescription.created_at).toLocaleDateString()}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Instructions */}
      {prescription.instructions && (
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Instructions</h2>
          <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
            <p className="text-gray-900 whitespace-pre-wrap">{prescription.instructions}</p>
          </div>
        </div>
      )}

      {/* Action Buttons */}
      <div className="flex justify-end space-x-4">
        {prescription.status === 'pending' && (
          <button
            onClick={() => updateStatusMutation.mutate('ready')}
            className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg transition-colors duration-200"
            disabled={updateStatusMutation.isLoading}
          >
            Mark Ready
          </button>
        )}
        {prescription.status === 'ready' && (
          <button
            onClick={() => updateStatusMutation.mutate('dispensed')}
            className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-lg transition-colors duration-200"
            disabled={updateStatusMutation.isLoading}
          >
            Dispense
          </button>
        )}
        <button
          onClick={() => window.print()}
          className="bg-gray-600 hover:bg-gray-700 text-white px-4 py-2 rounded-lg transition-colors duration-200"
        >
          Print Prescription
        </button>
      </div>
    </div>
  )
}

export default PrescriptionDetailPage
