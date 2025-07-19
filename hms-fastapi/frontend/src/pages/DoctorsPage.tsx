import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { User, AppointmentWithRelations, Patient } from '../types'
import api from '../lib/api'
import { UserIcon, MailIcon, Plus, Calendar, ChevronLeft, ChevronRight } from 'lucide-react'
import Modal from '../components/Modal'
import SearchInput from '../components/SearchInput'
import { useClientSearch } from '../hooks/useOptimizedSearch'

const DoctorsPage = () => {
  const [showAddModal, setShowAddModal] = useState(false)
  const [showEditModal, setShowEditModal] = useState(false)
  const [showScheduleModal, setShowScheduleModal] = useState(false)
  const [selectedDoctor, setSelectedDoctor] = useState<User | null>(null)
  const [editingDoctor, setEditingDoctor] = useState<User | null>(null)
  const [showAddAppointmentModal, setShowAddAppointmentModal] = useState(false)
  const [searchTerm, setSearchTerm] = useState('')
  const [specializationFilter, setSpecializationFilter] = useState('all')
  const [currentWeekStart, setCurrentWeekStart] = useState(() => {
    const today = new Date()
    const startOfWeek = new Date(today)
    startOfWeek.setDate(today.getDate() - today.getDay()) // Start from Sunday
    return startOfWeek
  })
  const queryClient = useQueryClient()

  const [doctorForm, setDoctorForm] = useState({
    first_name: '',
    last_name: '',
    email: '',
    phone: '',
    specialization: '',
    license_number: ''
  })

  const [editDoctorForm, setEditDoctorForm] = useState({
    first_name: '',
    last_name: '',
    email: '',
    phone: '',
    specialization: '',
    license_number: ''
  })

  const [appointmentForm, setAppointmentForm] = useState({
    patient_id: '',
    appointment_date: '',
    appointment_time: '',
    appointment_type: '',
    notes: '',
    symptoms: ''
  })

  const { data: doctors, isLoading, error } = useQuery<User[]>(
    'doctors',
    async () => {
      const response = await api.get('/api/users?role=doctor')
      return response.data
    },
    {
      staleTime: 5 * 60 * 1000, // 5 minutes
    }
  )

  // Get appointments for selected doctor
  const { data: doctorAppointments } = useQuery<AppointmentWithRelations[]>(
    ['doctor-appointments', selectedDoctor?.id, currentWeekStart],
    async () => {
      if (!selectedDoctor) return []
      
      const weekEnd = new Date(currentWeekStart)
      weekEnd.setDate(weekEnd.getDate() + 6)
      
      const params = new URLSearchParams({
        doctor_id: selectedDoctor.id.toString(),
        start_date: currentWeekStart.toISOString().split('T')[0],
        end_date: weekEnd.toISOString().split('T')[0]
      })
      
      const response = await api.get(`/api/appointments?${params}`)
      return response.data
    },
    {
      enabled: !!selectedDoctor,
      staleTime: 2 * 60 * 1000, // 2 minutes
    }
  )

  // Get patients for appointment creation
  const { data: patientsData } = useQuery<Patient[]>('patients', async () => {
    const response = await api.get('/api/patients')
    return response.data
  }, {
    enabled: showAddAppointmentModal
  })

  const addDoctorMutation = useMutation(
    (doctorData: typeof doctorForm) => {
      const payload = {
        ...doctorData,
        role: 'doctor',
        password: 'temp123' // Temporary password - should be changed on first login
      }
      return api.post('/api/users', payload)
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries('doctors')
        setShowAddModal(false)
        setDoctorForm({
          first_name: '',
          last_name: '',
          email: '',
          phone: '',
          specialization: '',
          license_number: ''
        })
      },
      onError: (error: any) => {
        console.error('Error adding doctor:', error)
        alert('Failed to add doctor. Please try again.')
      }
    }
  )

  const editDoctorMutation = useMutation(
    (doctorData: { id: number } & typeof editDoctorForm) => {
      const payload = {
        first_name: doctorData.first_name,
        last_name: doctorData.last_name,
        email: doctorData.email,
        phone: doctorData.phone,
        specialization: doctorData.specialization,
        license_number: doctorData.license_number
      }
      return api.put(`/api/users/${doctorData.id}`, payload)
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries('doctors')
        setShowEditModal(false)
        setEditingDoctor(null)
        setEditDoctorForm({
          first_name: '',
          last_name: '',
          email: '',
          phone: '',
          specialization: '',
          license_number: ''
        })
      },
      onError: (error: any) => {
        console.error('Error updating doctor:', error)
        alert('Failed to update doctor. Please try again.')
      }
    }
  )

  const addAppointmentMutation = useMutation(
    (appointmentData: typeof appointmentForm) => {
      const payload = {
        ...appointmentData,
        appointment_date: `${appointmentData.appointment_date}T${appointmentData.appointment_time}:00`,
        patient_id: parseInt(appointmentData.patient_id),
        doctor_id: selectedDoctor!.id,
        duration_minutes: 30
      }
      return api.post('/api/appointments', payload)
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['doctor-appointments', selectedDoctor?.id])
        queryClient.invalidateQueries(['appointments'])
        setShowAddAppointmentModal(false)
        setAppointmentForm({
          patient_id: '',
          appointment_date: '',
          appointment_time: '',
          appointment_type: '',
          notes: '',
          symptoms: ''
        })
      },
      onError: (error: any) => {
        console.error('Error creating appointment:', error)
        alert('Failed to create appointment. Please try again.')
      }
    }
  )

  const handleAddDoctor = async (e: React.FormEvent) => {
    e.preventDefault()
    addDoctorMutation.mutate(doctorForm)
  }

  const handleEditDoctor = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!editingDoctor) return
    editDoctorMutation.mutate({ ...editDoctorForm, id: editingDoctor.id })
  }

  const handleAddAppointment = async (e: React.FormEvent) => {
    e.preventDefault()
    addAppointmentMutation.mutate(appointmentForm)
  }

  const handleViewSchedule = (doctor: User) => {
    setSelectedDoctor(doctor)
    setShowScheduleModal(true)
  }

  const handleEditClick = (doctor: User) => {
    setEditingDoctor(doctor)
    setEditDoctorForm({
      first_name: doctor.first_name,
      last_name: doctor.last_name,
      email: doctor.email,
      phone: doctor.phone || '',
      specialization: doctor.specialization || '',
      license_number: doctor.license_number || ''
    })
    setShowEditModal(true)
  }

  // Calendar helper functions
  const getWeekDays = (weekStart: Date) => {
    const days = []
    for (let i = 0; i < 7; i++) {
      const day = new Date(weekStart)
      day.setDate(weekStart.getDate() + i)
      days.push(day)
    }
    return days
  }

  const navigateWeek = (direction: 'prev' | 'next') => {
    const newWeekStart = new Date(currentWeekStart)
    newWeekStart.setDate(currentWeekStart.getDate() + (direction === 'next' ? 7 : -7))
    setCurrentWeekStart(newWeekStart)
  }

  const goToToday = () => {
    const today = new Date()
    const startOfWeek = new Date(today)
    startOfWeek.setDate(today.getDate() - today.getDay())
    setCurrentWeekStart(startOfWeek)
  }

  const getAppointmentColor = (type: string) => {
    switch (type.toLowerCase()) {
      case 'consultation':
        return 'bg-blue-100 border-blue-300 text-blue-800'
      case 'follow_up':
        return 'bg-green-100 border-green-300 text-green-800'
      case 'emergency':
        return 'bg-red-100 border-red-300 text-red-800'
      case 'routine_checkup':
        return 'bg-purple-100 border-purple-300 text-purple-800'
      case 'surgery':
        return 'bg-orange-100 border-orange-300 text-orange-800'
      case 'therapy':
        return 'bg-teal-100 border-teal-300 text-teal-800'
      default:
        return 'bg-gray-100 border-gray-300 text-gray-800'
    }
  }

  const getAppointmentsForDay = (day: Date) => {
    if (!doctorAppointments) return []
    
    const dayString = day.toISOString().split('T')[0]
    return doctorAppointments.filter(appointment => {
      const appointmentDate = new Date(appointment.appointment_date).toISOString().split('T')[0]
      return appointmentDate === dayString
    }).sort((a, b) => new Date(a.appointment_date).getTime() - new Date(b.appointment_date).getTime())
  }

  // Apply client-side filtering and search
  const filteredDoctors = useClientSearch(
    doctors,
    searchTerm,
    ['first_name', 'last_name', 'email', 'phone', 'specialization', 'license_number'],
    [
      // Specialization filter
      (doctor) => specializationFilter === 'all' || doctor.specialization === specializationFilter
    ]
  ) || []

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
        <p className="text-red-800">Error loading doctors. Please try again.</p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Doctors</h1>
        <button 
          className="inline-flex items-center px-4 py-2 text-sm font-medium text-white transition-all duration-200 border border-transparent rounded-lg shadow-sm bg-emerald-600 hover:bg-emerald-700 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
          onClick={() => setShowAddModal(true)}
        >
          <Plus className="w-4 h-4 mr-2" />
          Add New Doctor
        </button>
      </div>

      {/* Search and Filters */}
      <div className="p-4 bg-white rounded-lg shadow">
        <div className="flex flex-col gap-4 md:flex-row">
          <div className="flex-1">
            <SearchInput
              value={searchTerm}
              onChange={setSearchTerm}
              placeholder="Search by name, email, phone, or specialization..."
              className="w-full"
            />
          </div>
          <div>
            <select
              className="input"
              value={specializationFilter}
              onChange={(e) => setSpecializationFilter(e.target.value)}
            >
              <option value="all">All Specializations</option>
              <option value="Cardiology">Cardiology</option>
              <option value="Neurology">Neurology</option>
              <option value="Orthopedics">Orthopedics</option>
              <option value="Pediatrics">Pediatrics</option>
              <option value="General Medicine">General Medicine</option>
              <option value="Surgery">Surgery</option>
              <option value="Dermatology">Dermatology</option>
              <option value="Psychiatry">Psychiatry</option>
            </select>
          </div>
        </div>
      </div>

      <div className="bg-white rounded-lg shadow">
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-lg font-medium text-gray-900">Medical Staff ({filteredDoctors.length})</h2>
        </div>
        
        {filteredDoctors.length === 0 ? (
          <div className="p-6 text-center text-gray-500">
            No doctors found.
          </div>
        ) : (
          <div className="divide-y divide-gray-200">
            {filteredDoctors.map((doctor) => (
              <div key={doctor.id} className="p-6">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-4">
                    <div className="flex-shrink-0">
                      <div className="flex items-center justify-center w-12 h-12 rounded-full bg-primary-100">
                        <UserIcon className="w-6 h-6 text-primary-600" />
                      </div>
                    </div>
                    <div>
                      <h3 className="text-lg font-medium text-gray-900">
                        Dr. {doctor.first_name} {doctor.last_name}
                      </h3>
                      {doctor.specialization && (
                        <p className="mb-1 text-sm text-gray-600">{doctor.specialization}</p>
                      )}
                      <div className="flex items-center mt-1 space-x-4">
                        <div className="flex items-center text-sm text-gray-500">
                          <MailIcon className="w-4 h-4 mr-1" />
                          {doctor.email}
                        </div>
                        <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                          doctor.is_active 
                            ? 'bg-green-100 text-green-800'
                            : 'bg-red-100 text-red-800'
                        }`}>
                          {doctor.is_active ? 'Active' : 'Inactive'}
                        </span>
                      </div>
                    </div>
                  </div>
                  
                  <div className="flex space-x-2">
                    <button 
                      onClick={() => handleViewSchedule(doctor)}
                      className="bg-emerald-100 hover:bg-emerald-200 text-emerald-700 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors duration-200 flex items-center"
                    >
                      <Calendar className="w-4 h-4 mr-1" />
                      View Schedule
                    </button>
                    <button 
                      onClick={() => handleEditClick(doctor)}
                      className="bg-green-100 hover:bg-green-200 text-green-700 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors duration-200"
                    >
                      Edit
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Schedule Modal */}
      <Modal
        isOpen={showScheduleModal}
        onClose={() => setShowScheduleModal(false)}
        title={`${selectedDoctor?.first_name} ${selectedDoctor?.last_name}'s Schedule`}
        size="max-w-7xl"
      >
        <div className="space-y-6">
          {/* Calendar Navigation */}
          <div className="flex items-center justify-between py-4 border-b border-gray-200">
            <div className="flex items-center space-x-4">
              <button
                onClick={() => navigateWeek('prev')}
                className="p-2 text-gray-500 transition-colors rounded-lg hover:text-gray-700 hover:bg-gray-100"
              >
                <ChevronLeft className="w-5 h-5" />
              </button>
              
              <div className="text-lg font-semibold text-gray-900">
                {currentWeekStart.toLocaleDateString('en-US', { 
                  month: 'long', 
                  day: 'numeric',
                  year: 'numeric' 
                })} - {new Date(currentWeekStart.getTime() + 6 * 24 * 60 * 60 * 1000).toLocaleDateString('en-US', { 
                  month: 'long', 
                  day: 'numeric',
                  year: 'numeric' 
                })}
              </div>
              
              <button
                onClick={() => navigateWeek('next')}
                className="p-2 text-gray-500 transition-colors rounded-lg hover:text-gray-700 hover:bg-gray-100"
              >
                <ChevronRight className="w-5 h-5" />
              </button>
            </div>

            <div className="flex items-center space-x-3">
              <button
                onClick={goToToday}
                className="px-4 py-2 text-sm font-medium text-gray-700 transition-colors bg-white border border-gray-300 rounded-lg hover:bg-gray-50"
              >
                Today
              </button>
              
              <button
                onClick={() => setShowAddAppointmentModal(true)}
                className="flex items-center px-4 py-2 text-sm font-medium text-white transition-colors rounded-lg bg-emerald-600 hover:bg-emerald-700"
              >
                <Plus className="w-4 h-4 mr-2" />
                Add Appointment
              </button>
            </div>
          </div>

          {/* Calendar Grid */}
          <div className="min-h-[600px] bg-white border border-gray-200 rounded-lg overflow-hidden">
            {/* Day Headers */}
            <div className="grid grid-cols-7 border-b border-gray-200 bg-gray-50">
              {getWeekDays(currentWeekStart).map((day, index) => (
                <div key={index} className="p-4 text-center border-r border-gray-200 last:border-r-0">
                  <div className="text-sm font-medium text-gray-900">
                    {day.toLocaleDateString('en-US', { weekday: 'short' })}
                  </div>
                  <div className={`text-lg font-semibold mt-1 ${
                    day.toDateString() === new Date().toDateString() 
                      ? 'text-emerald-600' 
                      : 'text-gray-700'
                  }`}>
                    {day.getDate()}
                  </div>
                </div>
              ))}
            </div>

            {/* Calendar Body */}
            <div className="grid grid-cols-7 h-[500px]">
              {getWeekDays(currentWeekStart).map((day, index) => {
                const dayAppointments = getAppointmentsForDay(day)
                
                return (
                  <div 
                    key={index} 
                    className="p-2 overflow-y-auto border-r border-gray-200 last:border-r-0"
                  >
                    <div className="space-y-1">
                      {dayAppointments.map((appointment) => {
                        const appointmentTime = new Date(appointment.appointment_date)
                        return (
                          <div
                            key={appointment.id}
                            className={`p-2 rounded-lg border text-xs ${getAppointmentColor(appointment.appointment_type)} hover:shadow-sm cursor-pointer transition-all`}
                            title={`${appointment.appointment_type} - ${appointment.patient?.first_name} ${appointment.patient?.last_name}`}
                          >
                            <div className="font-semibold">
                              {appointmentTime.toLocaleTimeString('en-US', { 
                                hour: 'numeric', 
                                minute: '2-digit' 
                              })}
                            </div>
                            <div className="truncate">
                              {appointment.patient?.first_name} {appointment.patient?.last_name}
                            </div>
                            <div className="text-xs capitalize opacity-75">
                              {appointment.appointment_type.replace('_', ' ')}
                            </div>
                          </div>
                        )
                      })}
                      
                      {dayAppointments.length === 0 && (
                        <div className="py-8 text-sm text-center text-gray-400">
                          No appointments
                        </div>
                      )}
                    </div>
                  </div>
                )
              })}
            </div>
          </div>

          {/* Legend */}
          <div className="p-4 rounded-lg bg-gray-50">
            <h4 className="mb-3 text-sm font-medium text-gray-900">Appointment Types</h4>
            <div className="grid grid-cols-2 gap-3 md:grid-cols-4">
              <div className="flex items-center space-x-2">
                <div className="w-4 h-4 bg-blue-100 border border-blue-300 rounded"></div>
                <span className="text-sm text-gray-700">Consultation</span>
              </div>
              <div className="flex items-center space-x-2">
                <div className="w-4 h-4 bg-green-100 border border-green-300 rounded"></div>
                <span className="text-sm text-gray-700">Follow Up</span>
              </div>
              <div className="flex items-center space-x-2">
                <div className="w-4 h-4 bg-red-100 border border-red-300 rounded"></div>
                <span className="text-sm text-gray-700">Emergency</span>
              </div>
              <div className="flex items-center space-x-2">
                <div className="w-4 h-4 bg-purple-100 border border-purple-300 rounded"></div>
                <span className="text-sm text-gray-700">Routine Checkup</span>
              </div>
              <div className="flex items-center space-x-2">
                <div className="w-4 h-4 bg-orange-100 border border-orange-300 rounded"></div>
                <span className="text-sm text-gray-700">Surgery</span>
              </div>
              <div className="flex items-center space-x-2">
                <div className="w-4 h-4 bg-teal-100 border border-teal-300 rounded"></div>
                <span className="text-sm text-gray-700">Therapy</span>
              </div>
            </div>
          </div>
        </div>
      </Modal>

      {/* Add Appointment Modal */}
      <Modal
        isOpen={showAddAppointmentModal}
        onClose={() => setShowAddAppointmentModal(false)}
        title="Schedule New Appointment"
      >
        <form onSubmit={handleAddAppointment} className="space-y-4">
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Patient
            </label>
            <select
              required
              className="input"
              value={appointmentForm.patient_id}
              onChange={(e) => setAppointmentForm({...appointmentForm, patient_id: e.target.value})}
            >
              <option value="">Select Patient</option>
              {patientsData?.map(patient => (
                <option key={patient.id} value={patient.id}>
                  {patient.first_name} {patient.last_name}
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
                required
                className="input"
                value={appointmentForm.appointment_date}
                onChange={(e) => setAppointmentForm({...appointmentForm, appointment_date: e.target.value})}
              />
            </div>
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Time
              </label>
              <input
                type="time"
                required
                className="input"
                value={appointmentForm.appointment_time}
                onChange={(e) => setAppointmentForm({...appointmentForm, appointment_time: e.target.value})}
              />
            </div>
          </div>
          
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Appointment Type
            </label>
            <select
              required
              className="input"
              value={appointmentForm.appointment_type}
              onChange={(e) => setAppointmentForm({...appointmentForm, appointment_type: e.target.value})}
            >
              <option value="">Select Type</option>
              <option value="consultation">Consultation</option>
              <option value="follow_up">Follow Up</option>
              <option value="emergency">Emergency</option>
              <option value="routine_checkup">Routine Checkup</option>
              <option value="surgery">Surgery</option>
              <option value="therapy">Therapy</option>
            </select>
          </div>
          
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Symptoms
            </label>
            <textarea
              className="resize-none input"
              rows={3}
              value={appointmentForm.symptoms}
              onChange={(e) => setAppointmentForm({...appointmentForm, symptoms: e.target.value})}
              placeholder="Patient symptoms..."
            />
          </div>
          
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Notes
            </label>
            <textarea
              className="resize-none input"
              rows={2}
              value={appointmentForm.notes}
              onChange={(e) => setAppointmentForm({...appointmentForm, notes: e.target.value})}
              placeholder="Additional notes..."
            />
          </div>
          
          <div className="flex justify-end pt-6 space-x-3 border-t border-gray-200">
            <button
              type="button"
              onClick={() => setShowAddAppointmentModal(false)}
              className="inline-flex items-center px-4 py-2 text-sm font-medium text-gray-700 transition-all duration-200 bg-white border border-gray-300 rounded-lg shadow-sm hover:bg-gray-50 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2"
            >
              Cancel
            </button>
            <button 
              type="submit" 
              className="inline-flex items-center px-4 py-2 text-sm font-medium text-white transition-all duration-200 border border-transparent rounded-lg shadow-sm bg-emerald-600 hover:bg-emerald-700 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
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

      {/* Edit Doctor Modal */}
      <Modal
        isOpen={showEditModal}
        onClose={() => setShowEditModal(false)}
        title="Edit Doctor"
      >
        <form onSubmit={handleEditDoctor} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                First Name
              </label>
              <input
                type="text"
                required
                className="input"
                value={editDoctorForm.first_name}
                onChange={(e) => setEditDoctorForm({...editDoctorForm, first_name: e.target.value})}
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
                value={editDoctorForm.last_name}
                onChange={(e) => setEditDoctorForm({...editDoctorForm, last_name: e.target.value})}
              />
            </div>
          </div>
          
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Email
            </label>
            <input
              type="email"
              required
              className="input"
              value={editDoctorForm.email}
              onChange={(e) => setEditDoctorForm({...editDoctorForm, email: e.target.value})}
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
                value={editDoctorForm.phone}
                onChange={(e) => setEditDoctorForm({...editDoctorForm, phone: e.target.value})}
              />
            </div>
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                License Number
              </label>
              <input
                type="text"
                className="input"
                value={editDoctorForm.license_number}
                onChange={(e) => setEditDoctorForm({...editDoctorForm, license_number: e.target.value})}
                placeholder="Medical license number"
              />
            </div>
          </div>
          
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Specialization
            </label>
            <select
              className="input"
              value={editDoctorForm.specialization}
              onChange={(e) => setEditDoctorForm({...editDoctorForm, specialization: e.target.value})}
              required
            >
              <option value="">Select Specialization</option>
              <option value="General Medicine">General Medicine</option>
              <option value="Cardiology">Cardiology</option>
              <option value="Neurology">Neurology</option>
              <option value="Orthopedics">Orthopedics</option>
              <option value="Pediatrics">Pediatrics</option>
              <option value="Surgery">Surgery</option>
              <option value="Dermatology">Dermatology</option>
              <option value="Psychiatry">Psychiatry</option>
              <option value="Radiology">Radiology</option>
              <option value="Emergency Medicine">Emergency Medicine</option>
            </select>
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
              className="inline-flex items-center px-4 py-2 text-sm font-medium text-white transition-all duration-200 border border-transparent rounded-lg shadow-sm bg-emerald-600 hover:bg-emerald-700 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
              disabled={editDoctorMutation.isLoading}
            >
              {editDoctorMutation.isLoading && (
                <svg className="w-4 h-4 mr-2 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="m4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
              )}
              {editDoctorMutation.isLoading ? 'Updating...' : 'Update Doctor'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Add Doctor Modal */}
      <Modal
        isOpen={showAddModal}
        onClose={() => setShowAddModal(false)}
        title="Add New Doctor"
      >
        <form onSubmit={handleAddDoctor} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                First Name
              </label>
              <input
                type="text"
                required
                className="input"
                value={doctorForm.first_name}
                onChange={(e) => setDoctorForm({...doctorForm, first_name: e.target.value})}
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
                value={doctorForm.last_name}
                onChange={(e) => setDoctorForm({...doctorForm, last_name: e.target.value})}
              />
            </div>
          </div>
          
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Email
            </label>
            <input
              type="email"
              required
              className="input"
              value={doctorForm.email}
              onChange={(e) => setDoctorForm({...doctorForm, email: e.target.value})}
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
                value={doctorForm.phone}
                onChange={(e) => setDoctorForm({...doctorForm, phone: e.target.value})}
              />
            </div>
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                License Number
              </label>
              <input
                type="text"
                className="input"
                value={doctorForm.license_number}
                onChange={(e) => setDoctorForm({...doctorForm, license_number: e.target.value})}
                placeholder="Medical license number"
              />
            </div>
          </div>
          
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Specialization
            </label>
            <select
              className="input"
              value={doctorForm.specialization}
              onChange={(e) => setDoctorForm({...doctorForm, specialization: e.target.value})}
              required
            >
              <option value="">Select Specialization</option>
              <option value="General Medicine">General Medicine</option>
              <option value="Cardiology">Cardiology</option>
              <option value="Neurology">Neurology</option>
              <option value="Orthopedics">Orthopedics</option>
              <option value="Pediatrics">Pediatrics</option>
              <option value="Surgery">Surgery</option>
              <option value="Dermatology">Dermatology</option>
              <option value="Psychiatry">Psychiatry</option>
              <option value="Radiology">Radiology</option>
              <option value="Emergency Medicine">Emergency Medicine</option>
            </select>
          </div>
          
          <div className="p-3 rounded-md bg-blue-50">
            <p className="text-sm text-blue-800">
              <strong>Note:</strong> A temporary password "temp123" will be assigned. 
              The doctor should change it upon first login.
            </p>
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
              className="inline-flex items-center px-4 py-2 text-sm font-medium text-white transition-all duration-200 border border-transparent rounded-lg shadow-sm bg-emerald-600 hover:bg-emerald-700 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
              disabled={addDoctorMutation.isLoading}
            >
              {addDoctorMutation.isLoading && (
                <svg className="w-4 h-4 mr-2 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="m4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
              )}
              {addDoctorMutation.isLoading ? 'Adding...' : 'Add Doctor'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}

export default DoctorsPage
