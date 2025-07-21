import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { Users, Calendar, FileText, Activity, TestTube, Pill } from 'lucide-react'
import api from '../lib/api'
import Modal from '../components/Modal'
import { LoadingDashboardData } from '../components/loading/LoadingStates'

interface DashboardStats {
  total_patients: number
  total_active_patients: number
  todays_appointments: number
  total_appointments: number
  total_medical_records: number
  pending_lab_tests: number
  pending_prescriptions: number
  total_bills: number
  unpaid_bills: number
  revenue_this_month: number
}

interface RecentAppointment {
  id: number
  patient_name: string
  appointment_date: string
  appointment_type: string
  status: string
}

interface DashboardData {
  stats: DashboardStats
  recent_appointments: RecentAppointment[]
}

const DashboardPage = () => {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [dashboardData, setDashboardData] = useState<DashboardData | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  
  // Modal states
  const [showPatientModal, setShowPatientModal] = useState(false)
  const [showAppointmentModal, setShowAppointmentModal] = useState(false)
  const [showMedicalRecordModal, setShowMedicalRecordModal] = useState(false)
  
  // Form states
  const [patientForm, setPatientForm] = useState({
    first_name: '',
    last_name: '',
    email: '',
    phone: '',
    date_of_birth: '',
    gender: '',
    address: ''
  })
  
  const [appointmentForm, setAppointmentForm] = useState({
    patient_id: '',
    doctor_id: '',
    appointment_date: '',
    appointment_type: 'consultation',
    notes: ''
  })
  
  const [medicalRecordForm, setMedicalRecordForm] = useState({
    patient_id: '',
    doctor_id: '',
    record_type: 'consultation',
    title: '',
    description: '',
    diagnosis: ''
  })
  
  const [patients, setPatients] = useState([])
  const [doctors, setDoctors] = useState([])
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    fetchDashboardData()
    fetchPatientsAndDoctors()
  }, [])

  const fetchDashboardData = async () => {
    try {
      setLoading(true)
      const response = await api.get('/api/dashboard')
      setDashboardData(response.data)
    } catch (error: any) {
      setError('Failed to load dashboard data')
      console.error('Error fetching dashboard data:', error)
    } finally {
      setLoading(false)
    }
  }

  const fetchPatientsAndDoctors = async () => {
    try {
      const [patientsRes, doctorsRes] = await Promise.all([
        api.get('/api/patients'),
        api.get('/api/users?role=doctor')
      ])
      setPatients(patientsRes.data)
      setDoctors(doctorsRes.data)
    } catch (error) {
      console.error('Error fetching patients and doctors:', error)
    }
  }

  const handlePatientSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setSubmitting(true)
    try {
      await api.post('/api/patients', patientForm)
      setShowPatientModal(false)
      setPatientForm({
        first_name: '',
        last_name: '',
        email: '',
        phone: '',
        date_of_birth: '',
        gender: '',
        address: ''
      })
      fetchDashboardData() // Refresh data
      fetchPatientsAndDoctors() // Refresh patients list
    } catch (error: any) {
      console.error('Error creating patient:', error)
      alert('Failed to create patient')
    } finally {
      setSubmitting(false)
    }
  }

  const handleAppointmentSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setSubmitting(true)
    try {
      const appointmentData = {
        ...appointmentForm,
        appointment_date: new Date(appointmentForm.appointment_date).toISOString(),
        patient_id: parseInt(appointmentForm.patient_id),
        doctor_id: parseInt(appointmentForm.doctor_id)
      }
      await api.post('/api/appointments', appointmentData)
      setShowAppointmentModal(false)
      setAppointmentForm({
        patient_id: '',
        doctor_id: '',
        appointment_date: '',
        appointment_type: 'consultation',
        notes: ''
      })
      fetchDashboardData() // Refresh data
    } catch (error: any) {
      console.error('Error creating appointment:', error)
      alert('Failed to create appointment')
    } finally {
      setSubmitting(false)
    }
  }

  const handleMedicalRecordSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setSubmitting(true)
    try {
      const recordData = {
        ...medicalRecordForm,
        patient_id: parseInt(medicalRecordForm.patient_id),
        doctor_id: parseInt(medicalRecordForm.doctor_id)
      }
      await api.post('/api/medical-records', recordData)
      setShowMedicalRecordModal(false)
      setMedicalRecordForm({
        patient_id: '',
        doctor_id: '',
        record_type: 'consultation',
        title: '',
        description: '',
        diagnosis: ''
      })
      fetchDashboardData() // Refresh data
    } catch (error: any) {
      console.error('Error creating medical record:', error)
      alert('Failed to create medical record')
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) {
    return <LoadingDashboardData />
  }

  // Show skeleton UI even if there's an error or no data
  if (error || !dashboardData) {
    return <LoadingDashboardData />
    return (
      <div className="p-4 border border-red-200 rounded-md bg-red-50">
        <p className="text-red-800">{error || 'Failed to load dashboard'}</p>
      </div>
    )
  }

  const { stats } = dashboardData

  const statCards = [
    {
      name: 'Total Patients',
      value: stats.total_patients.toLocaleString(),
      icon: Users,
      color: 'bg-blue-500',
      route: '/patients',
      subtitle: `${stats.total_active_patients} active`
    },
    {
      name: 'Today\'s Appointments',
      value: stats.todays_appointments.toString(),
      icon: Calendar,
      color: 'bg-green-500',
      route: '/appointments',
      subtitle: `${stats.total_appointments} total`
    },
    {
      name: 'Medical Records',
      value: stats.total_medical_records.toLocaleString(),
      icon: FileText,
      color: 'bg-purple-500',
      route: '/medical-records',
      subtitle: 'All records'
    },
    {
      name: 'Pending Lab Tests',
      value: stats.pending_lab_tests.toString(),
      icon: TestTube,
      color: 'bg-orange-500',
      route: '/lab',
      subtitle: 'Awaiting results'
    },
    {
      name: 'Pending Prescriptions',
      value: stats.pending_prescriptions.toString(),
      icon: Pill,
      color: 'bg-red-500',
      route: '/pharmacy',
      subtitle: 'To be dispensed'
    },
    {
      name: 'Unpaid Bills',
      value: stats.unpaid_bills.toString(),
      icon: Activity,
      color: 'bg-yellow-500',
      route: '/bills',
      subtitle: `$${stats.revenue_this_month.toLocaleString()} this month`
    }
  ]

  return (
    <div className="space-y-6">
      {/* Welcome Section */}
      <div className="p-6 bg-white rounded-lg shadow">
        <h1 className="text-2xl font-bold text-gray-900">
          Welcome back, {user?.first_name}!
        </h1>
        <p className="mt-2 text-gray-600">
          Here's what's happening in your hospital today.
        </p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
        {statCards.map((stat) => {
          const Icon = stat.icon
          return (
            <div 
              key={stat.name} 
              className="p-6 transition-shadow bg-white rounded-lg shadow cursor-pointer hover:shadow-lg"
              onClick={() => navigate(stat.route)}
            >
              <div className="flex items-center">
                <div className={`p-3 rounded-full ${stat.color}`}>
                  <Icon className="w-6 h-6 text-white" />
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-600">{stat.name}</p>
                  <p className="text-2xl font-bold text-gray-900">{stat.value}</p>
                </div>
              </div>
              <div className="mt-4">
                <span className="text-sm text-gray-500">{stat.subtitle}</span>
              </div>
            </div>
          )
        })}
      </div>

      {/* Recent Activity */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Today's Appointments */}
        <div className="bg-white rounded-lg shadow">
          <div className="p-6 border-b border-gray-200">
            <h2 className="text-lg font-semibold text-gray-900">Today's Appointments</h2>
          </div>
          <div className="p-6">
            <div className="space-y-4">
              {dashboardData.recent_appointments.length === 0 ? (
                <p className="text-gray-500">No recent appointments</p>
              ) : (
                dashboardData.recent_appointments.map((appointment) => (
                  <div key={appointment.id} className="flex items-center justify-between p-4 rounded-lg bg-gray-50">
                    <div>
                      <p className="font-medium text-gray-900">{appointment.patient_name}</p>
                      <p className="text-sm text-gray-500">{appointment.appointment_type}</p>
                    </div>
                    <div className="text-right">
                      <p className="text-sm font-medium text-gray-900">
                        {new Date(appointment.appointment_date).toLocaleDateString()}
                      </p>
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                        appointment.status === 'completed' ? 'bg-green-100 text-green-800' :
                        appointment.status === 'scheduled' ? 'bg-blue-100 text-blue-800' :
                        appointment.status === 'cancelled' ? 'bg-red-100 text-red-800' :
                        'bg-yellow-100 text-yellow-800'
                      }`}>
                        {appointment.status}
                      </span>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>

        {/* Quick Actions */}
        <div className="bg-white rounded-lg shadow">
          <div className="p-6 border-b border-gray-200">
            <h2 className="text-lg font-semibold text-gray-900">Quick Actions</h2>
          </div>
          <div className="p-6">
            <div className="space-y-3">
              <button 
                className="w-full p-4 text-left transition-colors rounded-lg bg-blue-50 hover:bg-blue-100"
                onClick={() => setShowPatientModal(true)}
              >
                <div className="flex items-center">
                  <Users className="w-5 h-5 mr-3 text-blue-600" />
                  <span className="font-medium text-blue-900">Add New Patient</span>
                </div>
              </button>
              <button 
                className="w-full p-4 text-left transition-colors rounded-lg bg-green-50 hover:bg-green-100"
                onClick={() => setShowAppointmentModal(true)}
              >
                <div className="flex items-center">
                  <Calendar className="w-5 h-5 mr-3 text-green-600" />
                  <span className="font-medium text-green-900">Schedule Appointment</span>
                </div>
              </button>
              <button 
                className="w-full p-4 text-left transition-colors rounded-lg bg-purple-50 hover:bg-purple-100"
                onClick={() => setShowMedicalRecordModal(true)}
              >
                <div className="flex items-center">
                  <FileText className="w-5 h-5 mr-3 text-purple-600" />
                  <span className="font-medium text-purple-900">Create Medical Record</span>
                </div>
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Patient Modal */}
      <Modal
        isOpen={showPatientModal}
        onClose={() => setShowPatientModal(false)}
        title="Add New Patient"
      >
        <form onSubmit={handlePatientSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">First Name</label>
              <input
                type="text"
                required
                className="block w-full mt-1 border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500"
                value={patientForm.first_name}
                onChange={(e) => setPatientForm({...patientForm, first_name: e.target.value})}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Last Name</label>
              <input
                type="text"
                required
                className="block w-full mt-1 border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500"
                value={patientForm.last_name}
                onChange={(e) => setPatientForm({...patientForm, last_name: e.target.value})}
              />
            </div>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700">Email</label>
            <input
              type="email"
              className="block w-full mt-1 border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500"
              value={patientForm.email}
              onChange={(e) => setPatientForm({...patientForm, email: e.target.value})}
            />
          </div>
          
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Phone</label>
              <input
                type="tel"
                className="block w-full mt-1 border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500"
                value={patientForm.phone}
                onChange={(e) => setPatientForm({...patientForm, phone: e.target.value})}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Date of Birth</label>
              <input
                type="date"
                className="block w-full mt-1 border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500"
                value={patientForm.date_of_birth}
                onChange={(e) => setPatientForm({...patientForm, date_of_birth: e.target.value})}
              />
            </div>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700">Gender</label>
            <select
              className="block w-full mt-1 border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500"
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
            <label className="block text-sm font-medium text-gray-700">Address</label>
            <textarea
              className="block w-full mt-1 border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500"
              rows={3}
              value={patientForm.address}
              onChange={(e) => setPatientForm({...patientForm, address: e.target.value})}
            />
          </div>
          
          <div className="flex justify-end pt-4 space-x-3">
            <button
              type="button"
              onClick={() => setShowPatientModal(false)}
              className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-md hover:bg-gray-200"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700 disabled:opacity-50"
            >
              {submitting ? 'Creating...' : 'Create Patient'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Appointment Modal */}
      <Modal
        isOpen={showAppointmentModal}
        onClose={() => setShowAppointmentModal(false)}
        title="Schedule Appointment"
      >
        <form onSubmit={handleAppointmentSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700">Patient</label>
            <select
              required
              className="block w-full mt-1 border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500"
              value={appointmentForm.patient_id}
              onChange={(e) => setAppointmentForm({...appointmentForm, patient_id: e.target.value})}
            >
              <option value="">Select Patient</option>
              {patients.map((patient: any) => (
                <option key={patient.id} value={patient.id}>
                  {patient.first_name} {patient.last_name}
                </option>
              ))}
            </select>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700">Doctor</label>
            <select
              required
              className="block w-full mt-1 border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500"
              value={appointmentForm.doctor_id}
              onChange={(e) => setAppointmentForm({...appointmentForm, doctor_id: e.target.value})}
            >
              <option value="">Select Doctor</option>
              {doctors.map((doctor: any) => (
                <option key={doctor.id} value={doctor.id}>
                  Dr. {doctor.first_name} {doctor.last_name}
                </option>
              ))}
            </select>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700">Date & Time</label>
            <input
              type="datetime-local"
              required
              className="block w-full mt-1 border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500"
              value={appointmentForm.appointment_date}
              onChange={(e) => setAppointmentForm({...appointmentForm, appointment_date: e.target.value})}
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700">Appointment Type</label>
            <select
              className="block w-full mt-1 border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500"
              value={appointmentForm.appointment_type}
              onChange={(e) => setAppointmentForm({...appointmentForm, appointment_type: e.target.value})}
            >
              <option value="consultation">Consultation</option>
              <option value="follow_up">Follow-up</option>
              <option value="emergency">Emergency</option>
              <option value="routine_checkup">Routine Checkup</option>
            </select>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700">Notes</label>
            <textarea
              className="block w-full mt-1 border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500"
              rows={3}
              value={appointmentForm.notes}
              onChange={(e) => setAppointmentForm({...appointmentForm, notes: e.target.value})}
            />
          </div>
          
          <div className="flex justify-end pt-4 space-x-3">
            <button
              type="button"
              onClick={() => setShowAppointmentModal(false)}
              className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-md hover:bg-gray-200"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700 disabled:opacity-50"
            >
              {submitting ? 'Scheduling...' : 'Schedule Appointment'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Medical Record Modal */}
      <Modal
        isOpen={showMedicalRecordModal}
        onClose={() => setShowMedicalRecordModal(false)}
        title="Create Medical Record"
      >
        <form onSubmit={handleMedicalRecordSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700">Patient</label>
            <select
              required
              className="block w-full mt-1 border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500"
              value={medicalRecordForm.patient_id}
              onChange={(e) => setMedicalRecordForm({...medicalRecordForm, patient_id: e.target.value})}
            >
              <option value="">Select Patient</option>
              {patients.map((patient: any) => (
                <option key={patient.id} value={patient.id}>
                  {patient.first_name} {patient.last_name}
                </option>
              ))}
            </select>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700">Doctor</label>
            <select
              required
              className="block w-full mt-1 border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500"
              value={medicalRecordForm.doctor_id}
              onChange={(e) => setMedicalRecordForm({...medicalRecordForm, doctor_id: e.target.value})}
            >
              <option value="">Select Doctor</option>
              {doctors.map((doctor: any) => (
                <option key={doctor.id} value={doctor.id}>
                  Dr. {doctor.first_name} {doctor.last_name}
                </option>
              ))}
            </select>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700">Record Type</label>
            <select
              className="block w-full mt-1 border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500"
              value={medicalRecordForm.record_type}
              onChange={(e) => setMedicalRecordForm({...medicalRecordForm, record_type: e.target.value})}
            >
              <option value="consultation">Consultation</option>
              <option value="lab_result">Lab Result</option>
              <option value="prescription">Prescription</option>
              <option value="diagnosis">Diagnosis</option>
              <option value="treatment_plan">Treatment Plan</option>
            </select>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700">Title</label>
            <input
              type="text"
              required
              className="block w-full mt-1 border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500"
              value={medicalRecordForm.title}
              onChange={(e) => setMedicalRecordForm({...medicalRecordForm, title: e.target.value})}
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700">Description</label>
            <textarea
              className="block w-full mt-1 border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500"
              rows={3}
              value={medicalRecordForm.description}
              onChange={(e) => setMedicalRecordForm({...medicalRecordForm, description: e.target.value})}
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700">Diagnosis</label>
            <textarea
              className="block w-full mt-1 border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500"
              rows={2}
              value={medicalRecordForm.diagnosis}
              onChange={(e) => setMedicalRecordForm({...medicalRecordForm, diagnosis: e.target.value})}
            />
          </div>
          
          <div className="flex justify-end pt-4 space-x-3">
            <button
              type="button"
              onClick={() => setShowMedicalRecordModal(false)}
              className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-md hover:bg-gray-200"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700 disabled:opacity-50"
            >
              {submitting ? 'Creating...' : 'Create Record'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}

export default DashboardPage
