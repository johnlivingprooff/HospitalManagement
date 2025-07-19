import React, { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { ArrowLeft, Calendar, Clock, User, Stethoscope, FileText } from 'lucide-react'
import api from '../lib/api'
import Modal from '../components/Modal'

interface Appointment {
  id: number
  patient_id: number
  doctor_id: number
  appointment_date: string
  duration_minutes: number
  appointment_type: string
  status: string
  notes?: string
  symptoms?: string
  diagnosis?: string
  treatment_plan?: string
  created_at: string
  updated_at: string
  patient?: {
    first_name: string
    last_name: string
    email: string
  }
  doctor?: {
    first_name: string
    last_name: string
    specialization?: string
  }
}

const AppointmentDetailPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [showEditModal, setShowEditModal] = useState(false)
  const [editForm, setEditForm] = useState({
    appointment_date: '',
    duration_minutes: 30,
    appointment_type: '',
    status: '',
    notes: '',
    symptoms: '',
    diagnosis: '',
    treatment_plan: ''
  })

  const { data: appointment, isLoading, error } = useQuery<Appointment>(
    ['appointment', id],
    async () => {
      const response = await api.get(`/api/appointments/${id}`)
      return response.data
    },
    {
      enabled: !!id,
      onSuccess: (data) => {
        // Populate edit form when appointment data is loaded
        setEditForm({
          appointment_date: data.appointment_date.split('T')[0] + 'T' + data.appointment_date.split('T')[1].slice(0, 5),
          duration_minutes: data.duration_minutes,
          appointment_type: data.appointment_type,
          status: data.status,
          notes: data.notes || '',
          symptoms: data.symptoms || '',
          diagnosis: data.diagnosis || '',
          treatment_plan: data.treatment_plan || ''
        })
      }
    }
  )

  // Update appointment mutation
  const updateAppointmentMutation = useMutation(
    (updatedData: typeof editForm) => {
      return api.put(`/api/appointments/${id}`, {
        ...updatedData,
        appointment_date: new Date(updatedData.appointment_date).toISOString(),
        duration_minutes: Number(updatedData.duration_minutes)
      })
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['appointment', id])
        queryClient.invalidateQueries(['appointments'])
        setShowEditModal(false)
        alert('Appointment updated successfully!')
      },
      onError: (error: any) => {
        console.error('Error updating appointment:', error)
        alert('Failed to update appointment. Please try again.')
      }
    }
  )

  // Complete appointment mutation
  const completeAppointmentMutation = useMutation(
    () => api.put(`/api/appointments/${id}`, { status: 'completed' }),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['appointment', id])
        queryClient.invalidateQueries(['appointments'])
        alert('Appointment marked as completed!')
      }
    }
  )

  // Cancel appointment mutation
  const cancelAppointmentMutation = useMutation(
    () => api.put(`/api/appointments/${id}`, { status: 'cancelled' }),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['appointment', id])
        queryClient.invalidateQueries(['appointments'])
        alert('Appointment cancelled!')
      }
    }
  )

  const handleEditSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    updateAppointmentMutation.mutate(editForm)
  }

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
      <div className="flex items-center justify-center h-64">
        <div className="w-12 h-12 border-b-2 border-purple-600 rounded-full animate-spin"></div>
      </div>
    )
  }

  if (error || !appointment) {
    return (
      <div className="p-4 border border-red-200 rounded-md bg-red-50">
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
          className="p-2 transition-colors duration-200 bg-gray-100 rounded-lg hover:bg-gray-200"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            Appointment #{appointment.id}
          </h1>
          <p className="text-gray-600">Appointment Details</p>
        </div>
      </div>

      {/* Appointment Information Cards */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Appointment Information */}
        <div className="p-6 bg-white rounded-lg shadow">
          <h2 className="flex items-center mb-4 text-lg font-semibold text-gray-900">
            <Calendar className="w-5 h-5 mr-2 text-purple-600" />
            Appointment Information
          </h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Date & Time</label>
              <p className="flex items-center text-gray-900">
                <Calendar className="w-4 h-4 mr-2 text-gray-500" />
                {new Date(appointment.appointment_date).toLocaleString()}
              </p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Duration</label>
              <p className="flex items-center text-gray-900">
                <Clock className="w-4 h-4 mr-2 text-gray-500" />
                {appointment.duration_minutes} minutes
              </p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Status</label>
              <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(appointment.status)}`}>
                {appointment.status.charAt(0).toUpperCase() + appointment.status.slice(1)}
              </span>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Type</label>
              <p className="text-gray-900">{appointment.appointment_type}</p>
            </div>
          </div>
        </div>

        {/* Patient & Doctor */}
        <div className="p-6 bg-white rounded-lg shadow">
          <h2 className="flex items-center mb-4 text-lg font-semibold text-gray-900">
            <User className="w-5 h-5 mr-2 text-purple-600" />
            Patient & Doctor
          </h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Patient</label>
              <p className="text-gray-900">
                {appointment.patient ? 
                  `${appointment.patient.first_name} ${appointment.patient.last_name}` :
                  `Patient ID: ${appointment.patient_id}`
                }
              </p>
              {appointment.patient?.email && (
                <p className="text-sm text-gray-500">{appointment.patient.email}</p>
              )}
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Doctor</label>
              <p className="flex items-center text-gray-900">
                <Stethoscope className="w-4 h-4 mr-2 text-gray-500" />
                {appointment.doctor ?
                  `Dr. ${appointment.doctor.first_name} ${appointment.doctor.last_name}` :
                  `Doctor ID: ${appointment.doctor_id}`
                }
              </p>
              {appointment.doctor?.specialization && (
                <p className="text-sm text-gray-500">{appointment.doctor.specialization}</p>
              )}
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

      {/* Notes, Symptoms, Diagnosis, Treatment Section */}
      {(appointment.notes || appointment.symptoms || appointment.diagnosis || appointment.treatment_plan) && (
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
          {/* Notes & Symptoms */}
          {(appointment.notes || appointment.symptoms) && (
            <div className="p-6 bg-white rounded-lg shadow">
              <h2 className="flex items-center mb-4 text-lg font-semibold text-gray-900">
                <FileText className="w-5 h-5 mr-2 text-purple-600" />
                Notes & Symptoms
              </h2>
              {appointment.symptoms && (
                <div className="mb-4">
                  <label className="block mb-1 text-sm font-medium text-gray-700">Symptoms</label>
                  <p className="text-gray-900 whitespace-pre-wrap">{appointment.symptoms}</p>
                </div>
              )}
              {appointment.notes && (
                <div>
                  <label className="block mb-1 text-sm font-medium text-gray-700">Notes</label>
                  <p className="text-gray-900 whitespace-pre-wrap">{appointment.notes}</p>
                </div>
              )}
            </div>
          )}

          {/* Diagnosis & Treatment */}
          {(appointment.diagnosis || appointment.treatment_plan) && (
            <div className="p-6 bg-white rounded-lg shadow">
              <h2 className="flex items-center mb-4 text-lg font-semibold text-gray-900">
                <Stethoscope className="w-5 h-5 mr-2 text-purple-600" />
                Diagnosis & Treatment
              </h2>
              {appointment.diagnosis && (
                <div className="mb-4">
                  <label className="block mb-1 text-sm font-medium text-gray-700">Diagnosis</label>
                  <p className="text-gray-900 whitespace-pre-wrap">{appointment.diagnosis}</p>
                </div>
              )}
              {appointment.treatment_plan && (
                <div>
                  <label className="block mb-1 text-sm font-medium text-gray-700">Treatment Plan</label>
                  <p className="text-gray-900 whitespace-pre-wrap">{appointment.treatment_plan}</p>
                </div>
              )}
            </div>
          )}
        </div>
      )}

      {/* Action Buttons */}
      <div className="flex justify-end space-x-4">
        {appointment.status === 'scheduled' && (
          <>
            <button
              onClick={() => completeAppointmentMutation.mutate()}
              disabled={completeAppointmentMutation.isLoading}
              className="px-4 py-2 text-white transition-colors duration-200 bg-green-600 rounded-lg hover:bg-green-700 disabled:opacity-50"
            >
              {completeAppointmentMutation.isLoading ? 'Updating...' : 'Mark Complete'}
            </button>
            <button
              onClick={() => cancelAppointmentMutation.mutate()}
              disabled={cancelAppointmentMutation.isLoading}
              className="px-4 py-2 text-white transition-colors duration-200 bg-red-600 rounded-lg hover:bg-red-700 disabled:opacity-50"
            >
              {cancelAppointmentMutation.isLoading ? 'Updating...' : 'Cancel Appointment'}
            </button>
          </>
        )}
        <button
          onClick={() => setShowEditModal(true)}
          className="px-4 py-2 text-white transition-colors duration-200 bg-purple-600 rounded-lg hover:bg-purple-700"
        >
          Edit Appointment
        </button>
      </div>

      {/* Edit Appointment Modal */}
      <Modal
        isOpen={showEditModal}
        onClose={() => setShowEditModal(false)}
        title="Edit Appointment"
      >
        <form onSubmit={handleEditSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Date & Time
              </label>
              <input
                type="datetime-local"
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-purple-500 focus:border-purple-500"
                value={editForm.appointment_date}
                onChange={(e) => setEditForm({...editForm, appointment_date: e.target.value})}
                required
              />
            </div>
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Duration (minutes)
              </label>
              <input
                type="number"
                min="15"
                step="15"
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-purple-500 focus:border-purple-500"
                value={editForm.duration_minutes}
                onChange={(e) => setEditForm({...editForm, duration_minutes: Number(e.target.value)})}
                required
              />
            </div>
          </div>
          
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Appointment Type
              </label>
              <select
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-purple-500 focus:border-purple-500"
                value={editForm.appointment_type}
                onChange={(e) => setEditForm({...editForm, appointment_type: e.target.value})}
                required
              >
                <option value="">Select Type</option>
                <option value="consultation">Consultation</option>
                <option value="follow_up">Follow-up</option>
                <option value="emergency">Emergency</option>
                <option value="routine_checkup">Routine Check-up</option>
                <option value="surgery">Surgery</option>
                <option value="therapy">Therapy</option>
              </select>
            </div>
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Status
              </label>
              <select
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-purple-500 focus:border-purple-500"
                value={editForm.status}
                onChange={(e) => setEditForm({...editForm, status: e.target.value})}
                required
              >
                <option value="scheduled">Scheduled</option>
                <option value="confirmed">Confirmed</option>
                <option value="in_progress">In Progress</option>
                <option value="completed">Completed</option>
                <option value="cancelled">Cancelled</option>
                <option value="no_show">No Show</option>
              </select>
            </div>
          </div>

          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Symptoms
            </label>
            <textarea
              className="w-full h-20 px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-purple-500 focus:border-purple-500"
              placeholder="Patient symptoms..."
              value={editForm.symptoms}
              onChange={(e) => setEditForm({...editForm, symptoms: e.target.value})}
            />
          </div>

          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Notes
            </label>
            <textarea
              className="w-full h-20 px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-purple-500 focus:border-purple-500"
              placeholder="Additional notes..."
              value={editForm.notes}
              onChange={(e) => setEditForm({...editForm, notes: e.target.value})}
            />
          </div>

          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Diagnosis
            </label>
            <textarea
              className="w-full h-20 px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-purple-500 focus:border-purple-500"
              placeholder="Medical diagnosis..."
              value={editForm.diagnosis}
              onChange={(e) => setEditForm({...editForm, diagnosis: e.target.value})}
            />
          </div>

          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Treatment Plan
            </label>
            <textarea
              className="w-full h-20 px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-purple-500 focus:border-purple-500"
              placeholder="Treatment plan and recommendations..."
              value={editForm.treatment_plan}
              onChange={(e) => setEditForm({...editForm, treatment_plan: e.target.value})}
            />
          </div>

          <div className="flex justify-end pt-6 space-x-3 border-t border-gray-200">
            <button
              type="button"
              onClick={() => setShowEditModal(false)}
              className="inline-flex items-center px-4 py-2 text-sm font-medium text-gray-700 transition-all duration-200 bg-white border border-gray-300 rounded-lg shadow-sm hover:bg-gray-50 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="inline-flex items-center px-4 py-2 text-sm font-medium text-white transition-all duration-200 bg-purple-600 border border-transparent rounded-lg shadow-sm hover:bg-purple-700 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
              disabled={updateAppointmentMutation.isLoading}
            >
              {updateAppointmentMutation.isLoading && (
                <svg className="w-4 h-4 mr-2 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="m4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
              )}
              {updateAppointmentMutation.isLoading ? 'Updating...' : 'Update Appointment'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}

export default AppointmentDetailPage
