import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { useNavigate } from 'react-router-dom'
import { Calendar, Plus, Clock, User, FileText } from 'lucide-react'
import { Appointment } from '../types'
import api from '../lib/api'
import Modal from '../components/Modal'

const AppointmentsPage = () => {
  const [startDate, setStartDate] = useState(new Date().toISOString().split('T')[0])
  const [endDate, setEndDate] = useState(new Date().toISOString().split('T')[0])
  const [dateFilter, setDateFilter] = useState('today') // today, this_week, this_month, custom
  const [searchTerm, setSearchTerm] = useState('')
  const [showAddModal, setShowAddModal] = useState(false)
  const [showEditModal, setShowEditModal] = useState(false)
  const [selectedAppointment, setSelectedAppointment] = useState<Appointment | null>(null)
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const [appointmentForm, setAppointmentForm] = useState({
    patient_id: '',
    doctor_id: '',
    appointment_date: '',
    appointment_time: '',
    appointment_type: '',
    status: 'scheduled',
    notes: ''
  })

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

  // Helper functions for date calculations
  const getDateRange = (filter: string) => {
    const today = new Date()
    const currentDate = new Date()
    
    switch (filter) {
      case 'today':
        return {
          start: today.toISOString().split('T')[0],
          end: today.toISOString().split('T')[0]
        }
      case 'this_week':
        const startOfWeek = new Date(currentDate.setDate(currentDate.getDate() - currentDate.getDay()))
        const endOfWeek = new Date(currentDate.setDate(startOfWeek.getDate() + 6))
        return {
          start: startOfWeek.toISOString().split('T')[0],
          end: endOfWeek.toISOString().split('T')[0]
        }
      case 'this_month':
        const startOfMonth = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1)
        const endOfMonth = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0)
        return {
          start: startOfMonth.toISOString().split('T')[0],
          end: endOfMonth.toISOString().split('T')[0]
        }
      default: // custom
        return {
          start: startDate,
          end: endDate
        }
    }
  }

  // Update date range when filter changes
  const handleDateFilterChange = (filter: string) => {
    setDateFilter(filter)
    if (filter !== 'custom') {
      const range = getDateRange(filter)
      setStartDate(range.start)
      setEndDate(range.end)
    }
  }

  // Get current date range
  const currentDateRange = getDateRange(dateFilter)

  const { data: appointments, isLoading, error } = useQuery<Appointment[]>(
    ['appointments', currentDateRange.start, currentDateRange.end, searchTerm],
    async () => {
      const params = new URLSearchParams()
      
      if (currentDateRange.start) params.append('start_date', currentDateRange.start)
      if (currentDateRange.end) params.append('end_date', currentDateRange.end)
      if (searchTerm) params.append('search', searchTerm)
      
      const queryString = params.toString()
      const url = queryString ? `/api/appointments?${queryString}` : '/api/appointments'
      
      const response = await api.get(url)
      return response.data
    },
    {
      enabled: true,
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

  const addAppointmentMutation = useMutation(
    (appointmentData: typeof appointmentForm) => {
      const payload = {
        ...appointmentData,
        appointment_date: `${appointmentData.appointment_date}T${appointmentData.appointment_time}:00`,
        patient_id: parseInt(appointmentData.patient_id),
        doctor_id: parseInt(appointmentData.doctor_id)
      }
      return api.post('/api/appointments', payload)
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['appointments'])
        setShowAddModal(false)
        setAppointmentForm({
          patient_id: '',
          doctor_id: '',
          appointment_date: '',
          appointment_time: '',
          appointment_type: '',
          status: 'scheduled',
          notes: ''
        })
      },
      onError: (error: any) => {
        console.error('Error creating appointment:', error)
        alert('Failed to create appointment. Please try again.')
      }
    }
  )

  // Update appointment mutation
  const updateAppointmentMutation = useMutation(
    (updatedData: typeof editForm) => {
      if (!selectedAppointment) return Promise.reject('No appointment selected')
      return api.put(`/api/appointments/${selectedAppointment.id}`, {
        ...updatedData,
        appointment_date: new Date(updatedData.appointment_date).toISOString(),
        duration_minutes: Number(updatedData.duration_minutes)
      })
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['appointments'])
        setShowEditModal(false)
        setSelectedAppointment(null)
        alert('Appointment updated successfully!')
      },
      onError: (error: any) => {
        console.error('Error updating appointment:', error)
        alert('Failed to update appointment. Please try again.')
      }
    }
  )

  // Cancel appointment mutation
  const cancelAppointmentMutation = useMutation(
    (appointmentId: number) => api.put(`/api/appointments/${appointmentId}`, { status: 'cancelled' }),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['appointments'])
        alert('Appointment cancelled!')
      }
    }
  )

  const handleAddAppointment = async (e: React.FormEvent) => {
    e.preventDefault()
    addAppointmentMutation.mutate(appointmentForm)
  }

  const handleEditAppointment = (appointment: Appointment) => {
    setSelectedAppointment(appointment)
    setEditForm({
      appointment_date: appointment.appointment_date.split('T')[0] + 'T' + appointment.appointment_date.split('T')[1].slice(0, 5),
      duration_minutes: appointment.duration_minutes || 30,
      appointment_type: appointment.appointment_type,
      status: appointment.status,
      notes: appointment.notes || '',
      symptoms: appointment.symptoms || '',
      diagnosis: appointment.diagnosis || '',
      treatment_plan: appointment.treatment_plan || ''
    })
    setShowEditModal(true)
  }

  const handleEditSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    updateAppointmentMutation.mutate(editForm)
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'scheduled':
        return 'bg-blue-100 text-blue-800'
      case 'confirmed':
        return 'bg-green-100 text-green-800'
      case 'in_progress':
        return 'bg-yellow-100 text-yellow-800'
      case 'completed':
        return 'bg-gray-100 text-gray-800'
      case 'cancelled':
        return 'bg-red-100 text-red-800'
      default:
        return 'bg-gray-100 text-gray-800'
    }
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
        <p className="text-red-800">Error loading appointments. Please try again.</p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Appointments</h1>
        <button
          onClick={() => setShowAddModal(true)}
          className="inline-flex items-center px-4 py-2 text-sm font-medium text-white transition-all duration-200 bg-purple-600 border border-transparent rounded-lg shadow-sm hover:bg-purple-700 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <Plus className="w-4 h-4 mr-2" />
          Schedule Appointment
        </button>
      </div>

      {/* Date Filter */}
      <div className="p-6 bg-white rounded-lg shadow">
        <div className="flex flex-col gap-4">
          {/* Quick Filter Buttons */}
          <div>
            <label className="block mb-2 text-sm font-medium text-gray-700">
              Quick Filters
            </label>
            <div className="flex flex-wrap gap-2">
              {[
                { key: 'today', label: 'Today' },
                { key: 'this_week', label: 'This Week' },
                { key: 'this_month', label: 'This Month' },
                { key: 'custom', label: 'Custom Range' }
              ].map((filter) => (
                <button
                  key={filter.key}
                  onClick={() => handleDateFilterChange(filter.key)}
                  className={`px-3 py-2 text-sm font-medium rounded-md transition-colors ${
                    dateFilter === filter.key
                      ? 'bg-purple-100 text-purple-700 border-purple-300 border'
                      : 'bg-gray-100 text-gray-700 border border-gray-300 hover:bg-gray-200'
                  }`}
                >
                  {filter.label}
                </button>
              ))}
            </div>
          </div>

          {/* Custom Date Range */}
          {dateFilter === 'custom' && (
            <div className="flex flex-col gap-4 sm:flex-row">
              <div className="flex-1">
                <label className="block mb-2 text-sm font-medium text-gray-700">
                  Start Date
                </label>
                <input
                  type="date"
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-purple-500 focus:border-purple-500"
                />
              </div>
              <div className="flex-1">
                <label className="block mb-2 text-sm font-medium text-gray-700">
                  End Date
                </label>
                <input
                  type="date"
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  min={startDate}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-purple-500 focus:border-purple-500"
                />
              </div>
            </div>
          )}

          {/* Search */}
          <div>
            <label className="block mb-2 text-sm font-medium text-gray-700">
              Search Appointments
            </label>
            <input
              type="text"
              placeholder="Search by patient name, doctor, or notes..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-purple-500 focus:border-purple-500"
            />
          </div>
        </div>
      </div>

      {/* Appointments List */}
      <div className="bg-white rounded-lg shadow">
        <div className="p-6 border-b border-gray-200">
          <h2 className="text-lg font-semibold text-gray-900">
            Appointments for {
              dateFilter === 'today' ? 'Today' :
              dateFilter === 'this_week' ? 'This Week' :
              dateFilter === 'this_month' ? 'This Month' :
              `${currentDateRange.start} to ${currentDateRange.end}`
            }
          </h2>
        </div>
        
        {appointments && appointments.length > 0 ? (
          <div className="divide-y divide-gray-200">
            {appointments.map((appointment) => (
              <div key={appointment.id} className="p-6 hover:bg-gray-50">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-4">
                    <div className="flex-shrink-0">
                      <div className="flex items-center justify-center w-12 h-12 rounded-full bg-primary-100">
                        <Calendar className="w-6 h-6 text-primary-600" />
                      </div>
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center space-x-2">
                        <h3 className="text-sm font-medium text-gray-900">
                          Patient ID: {appointment.patient_id}
                        </h3>
                        <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(appointment.status)}`}>
                          {appointment.status}
                        </span>
                      </div>
                      <div className="flex items-center mt-1 space-x-4 text-sm text-gray-500">
                        <div className="flex items-center">
                          <Clock className="w-4 h-4 mr-1" />
                          {new Date(appointment.appointment_date).toLocaleTimeString()}
                        </div>
                        <div className="flex items-center">
                          <User className="w-4 h-4 mr-1" />
                          Dr. ID: {appointment.doctor_id}
                        </div>
                        <div className="flex items-center">
                          <FileText className="w-4 h-4 mr-1" />
                          {appointment.appointment_type}
                        </div>
                      </div>
                      {appointment.notes && (
                        <p className="mt-2 text-sm text-gray-600">{appointment.notes}</p>
                      )}
                    </div>
                  </div>
                  <div className="flex items-center space-x-2">
                    <button 
                      onClick={() => navigate(`/appointments/${appointment.id}`)}
                      className="bg-blue-100 hover:bg-blue-200 text-blue-700 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors duration-200"
                    >
                      View
                    </button>
                    <button 
                      onClick={() => handleEditAppointment(appointment)}
                      className="bg-green-100 hover:bg-green-200 text-green-700 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors duration-200"
                    >
                      Edit
                    </button>
                    <button 
                      onClick={() => cancelAppointmentMutation.mutate(appointment.id)}
                      disabled={cancelAppointmentMutation.isLoading}
                      className="bg-red-100 hover:bg-red-200 text-red-700 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors duration-200 disabled:opacity-50"
                    >
                      Cancel
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="p-6 text-center">
            <Calendar className="w-12 h-12 mx-auto mb-4 text-gray-400" />
            <p className="text-gray-500">No appointments scheduled for this date</p>
          </div>
        )}
      </div>

      {/* Schedule Appointment Modal */}
      <Modal
        isOpen={showAddModal}
        onClose={() => setShowAddModal(false)}
        title="Schedule New Appointment"
      >
        <form onSubmit={handleAddAppointment} className="space-y-4">
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Patient
            </label>
            <select 
              className="input"
              value={appointmentForm.patient_id}
              onChange={(e) => setAppointmentForm({...appointmentForm, patient_id: e.target.value})}
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
              Doctor
            </label>
            <select 
              className="input"
              value={appointmentForm.doctor_id}
              onChange={(e) => setAppointmentForm({...appointmentForm, doctor_id: e.target.value})}
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
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Date
              </label>
              <input 
                type="date" 
                className="input"
                value={appointmentForm.appointment_date}
                onChange={(e) => setAppointmentForm({...appointmentForm, appointment_date: e.target.value})}
                required
              />
            </div>
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Time
              </label>
              <input 
                type="time" 
                className="input"
                value={appointmentForm.appointment_time}
                onChange={(e) => setAppointmentForm({...appointmentForm, appointment_time: e.target.value})}
                required
              />
            </div>
          </div>
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Appointment Type
            </label>
            <select 
              className="input"
              value={appointmentForm.appointment_type}
              onChange={(e) => setAppointmentForm({...appointmentForm, appointment_type: e.target.value})}
              required
            >
              <option value="">Select Type</option>
              <option value="consultation">Consultation</option>
              <option value="follow_up">Follow-up</option>
              <option value="emergency">Emergency</option>
              <option value="routine_checkup">Routine Check-up</option>
            </select>
          </div>
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Notes
            </label>
            <textarea 
              className="h-24 input" 
              placeholder="Additional notes..."
              value={appointmentForm.notes}
              onChange={(e) => setAppointmentForm({...appointmentForm, notes: e.target.value})}
            ></textarea>
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
              className="inline-flex items-center px-4 py-2 text-sm font-medium text-white transition-all duration-200 bg-purple-600 border border-transparent rounded-lg shadow-sm hover:bg-purple-700 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
              disabled={addAppointmentMutation.isLoading}
            >
              {addAppointmentMutation.isLoading && (
                <svg className="w-4 h-4 mr-2 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="m4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
              )}
              {addAppointmentMutation.isLoading ? 'Scheduling...' : 'Schedule Appointment'}
            </button>
          </div>
        </form>
      </Modal>

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
              className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-purple-500 focus:border-purple-500 h-20"
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
              className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-purple-500 focus:border-purple-500 h-20"
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
              className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-purple-500 focus:border-purple-500 h-20"
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
              className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-purple-500 focus:border-purple-500 h-20"
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

export default AppointmentsPage
