import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { 
  ArrowLeft, 
  Building2, 
  Bed, 
  Users, 
  MapPin, 
  Calendar,
  Clock,
  Stethoscope,
  FileText,
  UserPlus,
  UserMinus,
  Edit,
  AlertCircle,
  CheckCircle,
  Activity,
  Phone,
  Mail,
  User,
  Download
} from 'lucide-react'
import jsPDF from 'jspdf'
import api from '../lib/api'
import Modal from '../components/Modal'
import ProtectedPage from '../components/ProtectedPage'

interface WardDetail {
  id: number
  name: string
  type: string
  capacity: number
  current_occupancy: number
  floor: number
  description?: string
  is_active: boolean
  created_at: string
  patients: WardPatient[]
}

interface WardPatient {
  id: number
  patient_id: number
  ward_id: number
  bed_number: string
  admission_date: string
  discharge_date?: string
  status: string
  notes?: string
  patient: {
    id: number
    first_name: string
    last_name: string
    email: string
    date_of_birth: string
    phone?: string
    emergency_contact?: string
  }
  doctor?: {
    id: number
    first_name: string
    last_name: string
  }
}

const WardDetailPage = () => {
  const { wardId } = useParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [showAdmitModal, setShowAdmitModal] = useState(false)
  const [showDischargeModal, setShowDischargeModal] = useState(false)
  const [selectedPatient, setSelectedPatient] = useState<WardPatient | null>(null)

  const [admitForm, setAdmitForm] = useState({
    patient_id: '',
    doctor_id: '',
    bed_number: '',
    notes: '',
    status: 'admitted'
  })

  const [dischargeForm, setDischargeForm] = useState({
    discharge_date: new Date().toISOString().split('T')[0],
    notes: ''
  })

  // Fetch ward details
  const { data: ward, isLoading, error } = useQuery<WardDetail>(
    ['ward', wardId],
    async () => {
      const response = await api.get(`/api/wards/${wardId}`)
      return response.data
    },
    {
      enabled: !!wardId,
      staleTime: 2 * 60 * 1000,
    }
  )

  // Fetch available patients
  const { data: availablePatients } = useQuery('available-patients', async () => {
    const response = await api.get('/api/patients?status=available')
    return response.data
  })

  // Fetch doctors
  const { data: doctors } = useQuery('doctors', async () => {
    const response = await api.get('/api/users?role=doctor')
    return response.data
  })

  // Admit patient mutation
  const admitPatientMutation = useMutation(
    (admissionData: typeof admitForm) => {
      return api.post(`/api/wards/${wardId}/patients`, {
        ...admissionData,
        patient_id: parseInt(admissionData.patient_id),
        doctor_id: parseInt(admissionData.doctor_id)
      })
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['ward', wardId])
        queryClient.invalidateQueries('available-patients')
        setShowAdmitModal(false)
        setAdmitForm({
          patient_id: '',
          doctor_id: '',
          bed_number: '',
          notes: '',
          status: 'admitted'
        })
      }
    }
  )

  // Discharge patient mutation
  const dischargePatientMutation = useMutation(
    (dischargeData: { patientId: number } & typeof dischargeForm) => {
      const { patientId, ...payload } = dischargeData
      return api.put(`/api/ward-patients/${patientId}/discharge`, payload)
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['ward', wardId])
        queryClient.invalidateQueries('available-patients')
        setShowDischargeModal(false)
        setSelectedPatient(null)
        setDischargeForm({
          discharge_date: new Date().toISOString().split('T')[0],
          notes: ''
        })
      }
    }
  )

  // Update patient status mutation
  const updatePatientStatusMutation = useMutation(
    (data: { patientId: number; status: string }) => {
      return api.put(`/api/ward-patients/${data.patientId}`, { status: data.status })
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['ward', wardId])
      }
    }
  )

  const handleAdmitPatient = (e: React.FormEvent) => {
    e.preventDefault()
    admitPatientMutation.mutate(admitForm)
  }

  const handleDischargePatient = (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedPatient) return
    dischargePatientMutation.mutate({
      patientId: selectedPatient.id,
      ...dischargeForm
    })
  }

  const handleDischargeClick = (patient: WardPatient) => {
    setSelectedPatient(patient)
    setShowDischargeModal(true)
  }

  const handleStatusChange = (patient: WardPatient, newStatus: string) => {
    updatePatientStatusMutation.mutate({
      patientId: patient.id,
      status: newStatus
    })
  }

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'admitted': return 'bg-blue-100 text-blue-800'
      case 'stable': return 'bg-green-100 text-green-800'
      case 'critical': return 'bg-red-100 text-red-800'
      case 'recovering': return 'bg-yellow-100 text-yellow-800'
      case 'discharged': return 'bg-gray-100 text-gray-800'
      default: return 'bg-gray-100 text-gray-800'
    }
  }

  const getWardTypeColor = (type: string) => {
    switch (type.toLowerCase()) {
      case 'icu': return 'bg-red-100 text-red-800'
      case 'emergency': return 'bg-orange-100 text-orange-800'
      case 'surgery': return 'bg-purple-100 text-purple-800'
      case 'maternity': return 'bg-pink-100 text-pink-800'
      case 'pediatric': return 'bg-blue-100 text-blue-800'
      case 'general': return 'bg-green-100 text-green-800'
      default: return 'bg-gray-100 text-gray-800'
    }
  }

  const generateWardReport = () => {
    if (!ward) return

    const doc = new jsPDF()
    const pageWidth = doc.internal.pageSize.width
    const pageHeight = doc.internal.pageSize.height

    // Header
    doc.setFontSize(20)
    doc.setFont('helvetica', 'bold')
    doc.text('Ward Report', pageWidth / 2, 20, { align: 'center' })
    
    doc.setFontSize(12)
    doc.setFont('helvetica', 'normal')
    doc.text(`Generated on: ${new Date().toLocaleDateString()}`, pageWidth / 2, 30, { align: 'center' })

    // Ward Information
    let yPosition = 50
    doc.setFontSize(14)
    doc.setFont('helvetica', 'bold')
    doc.text('Ward Information', 20, yPosition)
    
    yPosition += 10
    doc.setFontSize(10)
    doc.setFont('helvetica', 'normal')
    doc.text(`Ward Name: ${ward.name}`, 20, yPosition)
    yPosition += 7
    doc.text(`Type: ${ward.type.toUpperCase()}`, 20, yPosition)
    yPosition += 7
    doc.text(`Floor: Level ${ward.floor}`, 20, yPosition)
    yPosition += 7
    doc.text(`Capacity: ${ward.capacity} beds`, 20, yPosition)
    yPosition += 7
    doc.text(`Current Occupancy: ${ward.current_occupancy} beds`, 20, yPosition)
    yPosition += 7
    doc.text(`Available Beds: ${ward.capacity - ward.current_occupancy} beds`, 20, yPosition)
    
    if (ward.description) {
      yPosition += 7
      doc.text(`Description: ${ward.description}`, 20, yPosition)
    }

    // Patient List
    yPosition += 20
    doc.setFontSize(14)
    doc.setFont('helvetica', 'bold')
    doc.text('Current Patients', 20, yPosition)
    
    if (ward.patients.length === 0) {
      yPosition += 10
      doc.setFontSize(10)
      doc.setFont('helvetica', 'italic')
      doc.text('No patients currently admitted to this ward.', 20, yPosition)
    } else {
      yPosition += 10
      
      ward.patients.forEach((wp, index) => {
        if (yPosition > pageHeight - 40) {
          doc.addPage()
          yPosition = 20
        }
        
        doc.setFontSize(12)
        doc.setFont('helvetica', 'bold')
        doc.text(`${index + 1}. ${wp.patient.first_name} ${wp.patient.last_name}`, 20, yPosition)
        
        yPosition += 8
        doc.setFontSize(10)
        doc.setFont('helvetica', 'normal')
        doc.text(`Bed: ${wp.bed_number}`, 25, yPosition)
        yPosition += 6
        doc.text(`Status: ${wp.status.charAt(0).toUpperCase() + wp.status.slice(1)}`, 25, yPosition)
        yPosition += 6
        doc.text(`Admission Date: ${new Date(wp.admission_date).toLocaleDateString()}`, 25, yPosition)
        
        if (wp.doctor) {
          yPosition += 6
          doc.text(`Attending Doctor: Dr. ${wp.doctor.first_name} ${wp.doctor.last_name}`, 25, yPosition)
        }
        
        if (wp.notes) {
          yPosition += 6
          const notesLines = doc.splitTextToSize(`Notes: ${wp.notes}`, pageWidth - 50)
          doc.text(notesLines, 25, yPosition)
          yPosition += notesLines.length * 6
        }
        
        yPosition += 8
      })
    }

    // Statistics
    yPosition += 10
    if (yPosition > pageHeight - 60) {
      doc.addPage()
      yPosition = 20
    }

    doc.setFontSize(14)
    doc.setFont('helvetica', 'bold')
    doc.text('Ward Statistics', 20, yPosition)
    
    yPosition += 10
    doc.setFontSize(10)
    doc.setFont('helvetica', 'normal')
    doc.text(`Occupancy Rate: ${((ward.current_occupancy / ward.capacity) * 100).toFixed(1)}%`, 20, yPosition)
    
    const statusCounts = ward.patients.reduce((acc, p) => {
      acc[p.status] = (acc[p.status] || 0) + 1
      return acc
    }, {} as Record<string, number>)
    
    Object.entries(statusCounts).forEach(([status, count]) => {
      yPosition += 7
      doc.text(`${status.charAt(0).toUpperCase() + status.slice(1)}: ${count} patient${count > 1 ? 's' : ''}`, 20, yPosition)
    })

    doc.save(`ward-${ward.name.replace(/\s+/g, '-').toLowerCase()}-report.pdf`)
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="w-12 h-12 border-b-2 border-blue-600 rounded-full animate-spin"></div>
      </div>
    )
  }

  if (error || !ward) {
    return (
      <div className="p-4 border border-red-200 rounded-md bg-red-50">
        <p className="text-red-800">Error loading ward details. Please try again.</p>
      </div>
    )
  }

  return (
    <ProtectedPage resource="wards" action="read">
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-4">
            <button
              onClick={() => navigate('/wards')}
              className="p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors duration-200"
            >
              <ArrowLeft className="w-5 h-5" />
            </button>
            <div>
              <div className="flex items-center space-x-3">
                <h1 className="text-2xl font-bold text-gray-900">{ward.name}</h1>
                <span className={`inline-flex px-3 py-1 text-sm font-semibold rounded-full ${getWardTypeColor(ward.type)}`}>
                  {ward.type.toUpperCase()}
                </span>
                <span className={`inline-flex px-3 py-1 text-sm font-semibold rounded-full ${
                  ward.is_active ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                }`}>
                  {ward.is_active ? 'Active' : 'Inactive'}
                </span>
              </div>
              <p className="text-gray-600">Ward details and patient management</p>
            </div>
          </div>
          <div className="flex space-x-3">
            <button
              onClick={generateWardReport}
              className="inline-flex items-center px-4 py-2 text-sm font-medium text-gray-700 transition-all duration-200 bg-white border border-gray-300 rounded-lg shadow-sm hover:bg-gray-50 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
            >
              <Download className="w-4 h-4 mr-2" />
              Export Report
            </button>
            <button
              onClick={() => setShowAdmitModal(true)}
              disabled={ward.current_occupancy >= ward.capacity}
              className="inline-flex items-center px-4 py-2 text-sm font-medium text-white transition-all duration-200 bg-blue-600 border border-transparent rounded-lg shadow-sm hover:bg-blue-700 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <UserPlus className="w-4 h-4 mr-2" />
              Admit Patient
            </button>
          </div>
        </div>

        {/* Ward Overview Cards */}
        <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-4">
          <div className="p-6 bg-white rounded-lg shadow">
            <div className="flex items-center">
              <Building2 className="w-8 h-8 text-blue-600" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Floor Level</p>
                <p className="text-2xl font-bold text-gray-900">Level {ward.floor}</p>
              </div>
            </div>
          </div>
          
          <div className="p-6 bg-white rounded-lg shadow">
            <div className="flex items-center">
              <Bed className="w-8 h-8 text-green-600" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Total Capacity</p>
                <p className="text-2xl font-bold text-gray-900">{ward.capacity} beds</p>
              </div>
            </div>
          </div>
          
          <div className="p-6 bg-white rounded-lg shadow">
            <div className="flex items-center">
              <Users className="w-8 h-8 text-orange-600" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Current Occupancy</p>
                <p className="text-2xl font-bold text-gray-900">{ward.current_occupancy} patients</p>
              </div>
            </div>
          </div>
          
          <div className="p-6 bg-white rounded-lg shadow">
            <div className="flex items-center">
              <CheckCircle className="w-8 h-8 text-purple-600" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Available Beds</p>
                <p className="text-2xl font-bold text-gray-900">{ward.capacity - ward.current_occupancy}</p>
              </div>
            </div>
          </div>
        </div>

        {/* Occupancy Status */}
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-medium text-gray-900">Occupancy Status</h3>
            <span className={`text-sm font-medium ${
              (ward.current_occupancy / ward.capacity) >= 0.9 ? 'text-red-600' :
              (ward.current_occupancy / ward.capacity) >= 0.75 ? 'text-orange-600' : 'text-green-600'
            }`}>
              {((ward.current_occupancy / ward.capacity) * 100).toFixed(1)}% occupied
            </span>
          </div>
          <div className="w-full bg-gray-200 rounded-full h-4">
            <div 
              className={`h-4 rounded-full transition-all duration-300 ${
                (ward.current_occupancy / ward.capacity) >= 0.9 ? 'bg-red-500' :
                (ward.current_occupancy / ward.capacity) >= 0.75 ? 'bg-orange-500' : 'bg-green-500'
              }`}
              style={{ width: `${Math.min((ward.current_occupancy / ward.capacity) * 100, 100)}%` }}
            ></div>
          </div>
          <div className="flex justify-between mt-2 text-sm text-gray-600">
            <span>0 beds</span>
            <span>{ward.capacity} beds</span>
          </div>
        </div>

        {/* Ward Description */}
        {ward.description && (
          <div className="p-6 bg-white rounded-lg shadow">
            <h3 className="text-lg font-medium text-gray-900 mb-3">Ward Description</h3>
            <p className="text-gray-600">{ward.description}</p>
          </div>
        )}

        {/* Current Patients */}
        <div className="bg-white rounded-lg shadow">
          <div className="px-6 py-4 border-b border-gray-200">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-medium text-gray-900">
                Current Patients ({ward.patients.length})
              </h3>
            </div>
          </div>
          
          {ward.patients.length === 0 ? (
            <div className="p-6 text-center text-gray-500">
              <Bed className="w-12 h-12 text-gray-400 mx-auto mb-4" />
              <p>No patients currently admitted to this ward.</p>
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
                      Bed
                    </th>
                    <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                      Status
                    </th>
                    <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                      Doctor
                    </th>
                    <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                      Admission Date
                    </th>
                    <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {ward.patients.map((wardPatient) => (
                    <tr key={wardPatient.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <div className="flex-shrink-0 w-10 h-10">
                            <div className="flex items-center justify-center w-10 h-10 bg-blue-100 rounded-full">
                              <span className="text-sm font-medium text-blue-600">
                                {wardPatient.patient.first_name?.charAt(0)}{wardPatient.patient.last_name?.charAt(0)}
                              </span>
                            </div>
                          </div>
                          <div className="ml-4">
                            <div className="text-sm font-medium text-gray-900">
                              {wardPatient.patient.first_name} {wardPatient.patient.last_name}
                            </div>
                            <div className="text-sm text-gray-500">
                              {wardPatient.patient.email}
                            </div>
                            {wardPatient.patient.phone && (
                              <div className="text-xs text-gray-400 flex items-center">
                                <Phone className="w-3 h-3 mr-1" />
                                {wardPatient.patient.phone}
                              </div>
                            )}
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <Bed className="w-4 h-4 text-gray-400 mr-2" />
                          <span className="text-sm font-medium text-gray-900">
                            {wardPatient.bed_number}
                          </span>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <select
                          value={wardPatient.status}
                          onChange={(e) => handleStatusChange(wardPatient, e.target.value)}
                          className={`text-xs font-semibold rounded-full px-2 py-1 border-none ${getStatusColor(wardPatient.status)}`}
                        >
                          <option value="admitted">Admitted</option>
                          <option value="stable">Stable</option>
                          <option value="critical">Critical</option>
                          <option value="recovering">Recovering</option>
                        </select>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {wardPatient.doctor ? (
                          <div className="flex items-center">
                            <Stethoscope className="w-4 h-4 text-gray-400 mr-2" />
                            <span className="text-sm text-gray-900">
                              Dr. {wardPatient.doctor.first_name} {wardPatient.doctor.last_name}
                            </span>
                          </div>
                        ) : (
                          <span className="text-sm text-gray-500">No doctor assigned</span>
                        )}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <Calendar className="w-4 h-4 text-gray-400 mr-2" />
                          <div>
                            <div className="text-sm text-gray-900">
                              {new Date(wardPatient.admission_date).toLocaleDateString()}
                            </div>
                            <div className="text-xs text-gray-500">
                              {Math.floor((new Date().getTime() - new Date(wardPatient.admission_date).getTime()) / (1000 * 60 * 60 * 24))} days ago
                            </div>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 text-sm font-medium whitespace-nowrap">
                        <button
                          onClick={() => handleDischargeClick(wardPatient)}
                          className="bg-orange-100 hover:bg-orange-200 text-orange-700 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors duration-200 flex items-center"
                        >
                          <UserMinus className="w-4 h-4 mr-1" />
                          Discharge
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Admit Patient Modal */}
        <Modal
          isOpen={showAdmitModal}
          onClose={() => setShowAdmitModal(false)}
          title={`Admit Patient to ${ward.name}`}
        >
          <form onSubmit={handleAdmitPatient} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block mb-1 text-sm font-medium text-gray-700">
                  Patient
                </label>
                <select 
                  className="input"
                  value={admitForm.patient_id}
                  onChange={(e) => setAdmitForm({...admitForm, patient_id: e.target.value})}
                  required
                >
                  <option value="">Select Patient</option>
                  {availablePatients?.map((patient: any) => (
                    <option key={patient.id} value={patient.id}>
                      {patient.first_name} {patient.last_name}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block mb-1 text-sm font-medium text-gray-700">
                  Attending Doctor
                </label>
                <select 
                  className="input"
                  value={admitForm.doctor_id}
                  onChange={(e) => setAdmitForm({...admitForm, doctor_id: e.target.value})}
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
                  Bed Number
                </label>
                <input
                  type="text"
                  required
                  className="input"
                  value={admitForm.bed_number}
                  onChange={(e) => setAdmitForm({...admitForm, bed_number: e.target.value})}
                  placeholder="e.g., A1-01"
                />
              </div>
              <div>
                <label className="block mb-1 text-sm font-medium text-gray-700">
                  Initial Status
                </label>
                <select 
                  className="input"
                  value={admitForm.status}
                  onChange={(e) => setAdmitForm({...admitForm, status: e.target.value})}
                  required
                >
                  <option value="admitted">Admitted</option>
                  <option value="stable">Stable</option>
                  <option value="critical">Critical</option>
                  <option value="recovering">Recovering</option>
                </select>
              </div>
            </div>
            
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Admission Notes
              </label>
              <textarea
                className="input"
                rows={3}
                value={admitForm.notes}
                onChange={(e) => setAdmitForm({...admitForm, notes: e.target.value})}
                placeholder="Reason for admission, special instructions..."
              />
            </div>
            
            <div className="flex justify-end pt-6 space-x-3 border-t border-gray-200">
              <button
                type="button"
                onClick={() => setShowAdmitModal(false)}
                className="btn-secondary"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="btn-primary"
                disabled={admitPatientMutation.isLoading}
              >
                {admitPatientMutation.isLoading ? 'Admitting...' : 'Admit Patient'}
              </button>
            </div>
          </form>
        </Modal>

        {/* Discharge Patient Modal */}
        <Modal
          isOpen={showDischargeModal}
          onClose={() => setShowDischargeModal(false)}
          title="Discharge Patient"
        >
          <form onSubmit={handleDischargePatient} className="space-y-4">
            {selectedPatient && (
              <div className="p-4 bg-blue-50 border border-blue-200 rounded-lg">
                <h4 className="font-medium text-blue-900 flex items-center">
                  <User className="w-4 h-4 mr-2" />
                  {selectedPatient.patient.first_name} {selectedPatient.patient.last_name}
                </h4>
                <div className="mt-2 text-sm text-blue-700 space-y-1">
                  <p className="flex items-center">
                    <Bed className="w-3 h-3 mr-1" />
                    Bed: {selectedPatient.bed_number}
                  </p>
                  <p className="flex items-center">
                    <Calendar className="w-3 h-3 mr-1" />
                    Admitted: {new Date(selectedPatient.admission_date).toLocaleDateString()}
                  </p>
                  <p className="flex items-center">
                    <Clock className="w-3 h-3 mr-1" />
                    Length of Stay: {Math.floor((new Date().getTime() - new Date(selectedPatient.admission_date).getTime()) / (1000 * 60 * 60 * 24))} days
                  </p>
                </div>
              </div>
            )}
            
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Discharge Date
              </label>
              <input
                type="date"
                required
                className="input"
                value={dischargeForm.discharge_date}
                onChange={(e) => setDischargeForm({...dischargeForm, discharge_date: e.target.value})}
              />
            </div>
            
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Discharge Summary & Notes
              </label>
              <textarea
                className="input"
                rows={4}
                value={dischargeForm.notes}
                onChange={(e) => setDischargeForm({...dischargeForm, notes: e.target.value})}
                placeholder="Discharge summary, follow-up instructions, medications, etc..."
              />
            </div>
            
            <div className="flex justify-end pt-6 space-x-3 border-t border-gray-200">
              <button
                type="button"
                onClick={() => setShowDischargeModal(false)}
                className="btn-secondary"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="bg-orange-600 hover:bg-orange-700 text-white px-4 py-2 rounded-lg transition-colors duration-200"
                disabled={dischargePatientMutation.isLoading}
              >
                {dischargePatientMutation.isLoading ? 'Discharging...' : 'Discharge Patient'}
              </button>
            </div>
          </form>
        </Modal>
      </div>
    </ProtectedPage>
  )
}

export default WardDetailPage
