import React from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery } from 'react-query'
import { ArrowLeft, Calendar, Clock, User, Stethoscope, FileText } from 'lucide-react'
import api from '../services/api'

interface Appointment {
  id: number
  patient_id: number
  doctor_id: number
  appointment_date: string
  appointment_time: string
  reason: string
  status: string
  notes: string
  created_at: string
  patient: {
    first_name: string
    last_name: string
    email: string
  }
  doctor: {
    first_name: string
    last_name: string
    specialization: string
  }
}

const AppointmentDetailPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()

  const { data: appointment, isLoading, error } = useQuery<Appointment>(
    ['appointment', id],
    async () => {
      const response = await api.get(`/api/appointments/${id}`)
      return response.data
    },
    {
      enabled: !!id
    }
  )

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'scheduled': return 'bg-blue-100 text-blue-800'
      case 'completed': return 'bg-green-100 text-green-800'
      case 'cancelled': return 'bg-red-100 text-red-800'
      case 'no-show': return 'bg-gray-100 text-gray-800'
      default: return 'bg-yellow-100 text-yellow-800'
    }
  }

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600"></div>
      </div>
    )
  }

  if (error || !appointment) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-md p-4">
        <p className="text-red-800">Failed to load appointment details</p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center space-x-4">
        <button
          onClick={() => navigate('/appointments')}
          className="bg-gray-100 hover:bg-gray-200 p-2 rounded-lg transition-colors duration-200"
        >
          <ArrowLeft className="h-5 w-5" />
        </button>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            Appointment #{appointment.id}
          </h1>
          <p className="text-gray-600">Appointment Details</p>
        </div>
      </div>

      {/* Appointment Information Cards */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Appointment Information */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
            <Calendar className="h-5 w-5 mr-2 text-purple-600" />
            Appointment Information
          </h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Date</label>
              <p className="text-gray-900 flex items-center">
                <Calendar className="h-4 w-4 mr-2 text-gray-500" />
                {new Date(appointment.appointment_date).toLocaleDateString()}
              </p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Time</label>
              <p className="text-gray-900 flex items-center">
                <Clock className="h-4 w-4 mr-2 text-gray-500" />
                {appointment.appointment_time}
              </p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Status</label>
              <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(appointment.status)}`}>
                {appointment.status.charAt(0).toUpperCase() + appointment.status.slice(1)}
              </span>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Reason</label>
              <p className="text-gray-900">{appointment.reason}</p>
            </div>
          </div>
        </div>

        {/* Patient & Doctor */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
            <User className="h-5 w-5 mr-2 text-purple-600" />
            Patient & Doctor
          </h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Patient</label>
              <p className="text-gray-900">
                {appointment.patient.first_name} {appointment.patient.last_name}
              </p>
              <p className="text-sm text-gray-500">{appointment.patient.email}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Doctor</label>
              <p className="text-gray-900 flex items-center">
                <Stethoscope className="h-4 w-4 mr-2 text-gray-500" />
                Dr. {appointment.doctor.first_name} {appointment.doctor.last_name}
              </p>
              <p className="text-sm text-gray-500">{appointment.doctor.specialization}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Scheduled</label>
              <p className="text-gray-900">
                {new Date(appointment.created_at).toLocaleDateString()}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Notes Section */}
      {appointment.notes && (
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
            <FileText className="h-5 w-5 mr-2 text-purple-600" />
            Notes
          </h2>
          <p className="text-gray-900 whitespace-pre-wrap">{appointment.notes}</p>
        </div>
      )}

      {/* Action Buttons */}
      <div className="flex justify-end space-x-4">
        {appointment.status === 'scheduled' && (
          <>
            <button
              onClick={() => {/* Implement complete logic */}}
              className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-lg transition-colors duration-200"
            >
              Mark Complete
            </button>
            <button
              onClick={() => {/* Implement cancel logic */}}
              className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-lg transition-colors duration-200"
            >
              Cancel Appointment
            </button>
          </>
        )}
        <button
          onClick={() => navigate(`/appointments/edit/${appointment.id}`)}
          className="bg-purple-600 hover:bg-purple-700 text-white px-4 py-2 rounded-lg transition-colors duration-200"
        >
          Edit Appointment
        </button>
      </div>
    </div>
  )
}

export default AppointmentDetailPage
