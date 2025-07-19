import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { ArrowLeft, FileText, User, Stethoscope, Calendar, Download } from 'lucide-react'
import jsPDF from 'jspdf'
import api from '../lib/api'
import Modal from '../components/Modal'
import { MedicalRecord } from '../types'

const MedicalRecordDetailPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [showEditModal, setShowEditModal] = useState(false)
  
  const [editRecordForm, setEditRecordForm] = useState({
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

  const { data: record, isLoading, error } = useQuery<MedicalRecord>(
    ['medical-record', id],
    async () => {
      const response = await api.get(`/api/medical-records/${id}`)
      return response.data
    },
    {
      enabled: !!id
    }
  )

  // Fetch patients and doctors for edit form dropdowns
  const { data: patients } = useQuery('patients', async () => {
    const response = await api.get('/api/patients')
    return response.data
  })

  const { data: doctors } = useQuery('doctors', async () => {
    const response = await api.get('/api/users?role=doctor')
    return response.data
  })

  const editRecordMutation = useMutation(
    (recordData: { id: number } & typeof editRecordForm) => {
      const payload = {
        patient_id: parseInt(recordData.patient_id),
        doctor_id: parseInt(recordData.doctor_id),
        appointment_id: recordData.appointment_id ? parseInt(recordData.appointment_id) : undefined,
        record_type: recordData.record_type,
        title: recordData.title,
        description: recordData.description,
        diagnosis: recordData.diagnosis,
        treatment: recordData.treatment,
        medications: recordData.medications,
        lab_results: recordData.lab_results
      }
      return api.put(`/api/medical-records/${recordData.id}`, payload)
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['medical-record', id])
        setShowEditModal(false)
        setEditRecordForm({
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
        console.error('Error updating medical record:', error)
        alert('Failed to update medical record. Please try again.')
      }
    }
  )

  const handleEditRecord = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!record) return
    editRecordMutation.mutate({ ...editRecordForm, id: record.id })
  }

  const handleEditClick = () => {
    if (!record) return
    setEditRecordForm({
      patient_id: record.patient_id.toString(),
      doctor_id: record.doctor_id.toString(),
      appointment_id: record.appointment_id?.toString() || '',
      record_type: record.record_type,
      title: record.title,
      description: record.description,
      diagnosis: record.diagnosis || '',
      treatment: record.treatment || '',
      medications: record.medications || '',
      lab_results: record.lab_results || ''
    })
    setShowEditModal(true)
  }

  const generateSingleRecordReport = () => {
    if (!record) return

    const doc = new jsPDF()
    const pageWidth = doc.internal.pageSize.width
    const pageHeight = doc.internal.pageSize.height
    const margin = 20
    let yPosition = 30

    // Helper function to add text and handle page breaks
    const addText = (text: string, fontSize = 10, isBold = false) => {
      if (yPosition > pageHeight - 30) {
        doc.addPage()
        yPosition = 30
      }
      
      doc.setFontSize(fontSize)
      doc.setFont('helvetica', isBold ? 'bold' : 'normal')
      doc.text(text, margin, yPosition)
      yPosition += fontSize * 0.5 + 5
    }

    const addSection = (label: string, content: string) => {
      if (content && content.trim()) {
        addText(`${label}:`, 12, true)
        // Split long content into multiple lines
        const splitContent = doc.splitTextToSize(content, pageWidth - margin * 2)
        splitContent.forEach((line: string) => {
          addText(`  ${line}`, 11, false)
        })
        yPosition += 8 // Add some space after section
      }
    }

    // Header
    doc.setFontSize(20)
    doc.setFont('helvetica', 'bold')
    doc.text('Medical Record Report', pageWidth / 2, 20, { align: 'center' })
    
    doc.setFontSize(12)
    doc.setFont('helvetica', 'normal')
    doc.text(`Generated on: ${new Date().toLocaleDateString()}`, pageWidth / 2, 35, { align: 'center' })
    
    yPosition = 50

    // Record header
    doc.setFontSize(16)
    doc.setFont('helvetica', 'bold')
    doc.text(`Record #${record.id} - ${record.title}`, margin, yPosition)
    yPosition += 20

    doc.setFontSize(12)
    doc.setFont('helvetica', 'normal')
    doc.text(`Date Created: ${new Date(record.created_at).toLocaleDateString()}`, margin, yPosition)
    yPosition += 15

    // Patient Information
    doc.setFontSize(14)
    doc.setFont('helvetica', 'bold')
    doc.text('Patient Information', margin, yPosition)
    yPosition += 15

    addSection('Patient Name', record.patient ? `${record.patient.first_name} ${record.patient.last_name}` : 'Unknown')
    addSection('Email', record.patient?.email || 'Not provided')
    if (record.patient?.date_of_birth) {
      addSection('Date of Birth', new Date(record.patient.date_of_birth).toLocaleDateString())
    }
    yPosition += 10

    // Doctor Information
    doc.setFontSize(14)
    doc.setFont('helvetica', 'bold')
    doc.text('Doctor Information', margin, yPosition)
    yPosition += 15

    addSection('Doctor', record.doctor ? `Dr. ${record.doctor.first_name} ${record.doctor.last_name}` : 'N/A')
    if (record.doctor?.specialization) {
      addSection('Specialization', record.doctor.specialization)
    }
    yPosition += 10

    // Medical Details
    doc.setFontSize(14)
    doc.setFont('helvetica', 'bold')
    doc.text('Medical Details', margin, yPosition)
    yPosition += 15

    addSection('Record Type', record.record_type.replace('_', ' ').toUpperCase())
    addSection('Description', record.description)
    
    if (record.diagnosis) addSection('Diagnosis', record.diagnosis)
    if (record.treatment) addSection('Treatment', record.treatment)
    if (record.medications) addSection('Medications', record.medications)
    if (record.lab_results) addSection('Lab Results', record.lab_results)

    // Add footer
    const totalPages = doc.internal.pages.length - 1
    for (let i = 1; i <= totalPages; i++) {
      doc.setPage(i)
      doc.setFontSize(8)
      doc.setFont('helvetica', 'normal')
      doc.text(
        `Page ${i} of ${totalPages} | Hospital Management System`, 
        pageWidth / 2, 
        pageHeight - 10, 
        { align: 'center' }
      )
    }

    // Save the PDF
    const fileName = `medical-record-${record.id}-${new Date().toISOString().split('T')[0]}.pdf`
    doc.save(fileName)
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="w-12 h-12 border-b-2 border-indigo-600 rounded-full animate-spin"></div>
      </div>
    )
  }

  if (error || !record) {
    return (
      <div className="p-4 border border-red-200 rounded-md bg-red-50">
        <p className="text-red-800">Failed to load medical record details</p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center space-x-4">
        <button
          onClick={() => navigate('/medical-records')}
          className="p-2 transition-colors duration-200 bg-gray-100 rounded-lg hover:bg-gray-200"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            Medical Record #{record.id}
          </h1>
          <p className="text-gray-600">
            {record.patient ? `${record.patient.first_name} ${record.patient.last_name}` : 'Unknown Patient'}
          </p>
        </div>
      </div>

      {/* Medical Record Information Cards */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Patient Information */}
        <div className="p-6 bg-white rounded-lg shadow">
          <h2 className="flex items-center mb-4 text-lg font-semibold text-gray-900">
            <User className="w-5 h-5 mr-2 text-indigo-600" />
            Patient Information
          </h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Patient Name</label>
              <p className="text-gray-900">
                {record.patient ? `${record.patient.first_name} ${record.patient.last_name}` : 'Unknown Patient'}
              </p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Email</label>
              <p className="text-gray-900">{record.patient?.email || 'Not provided'}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Date of Birth</label>
              <p className="text-gray-900">
                {record.patient?.date_of_birth ? new Date(record.patient.date_of_birth).toLocaleDateString() : 'Not provided'}
              </p>
            </div>
          </div>
        </div>

        {/* Doctor & Visit Information */}
        <div className="p-6 bg-white rounded-lg shadow">
          <h2 className="flex items-center mb-4 text-lg font-semibold text-gray-900">
            <Stethoscope className="w-5 h-5 mr-2 text-indigo-600" />
            Doctor & Visit Information
          </h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Doctor</label>
              <p className="text-gray-900">
                {record.doctor ? `Dr. ${record.doctor.first_name} ${record.doctor.last_name}` : 'Unknown Doctor'}
              </p>
              {record.doctor && record.doctor.specialization && (
                <p className="text-sm text-gray-500">{record.doctor.specialization}</p>
              )}
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Visit Date</label>
              <p className="flex items-center text-gray-900">
                <Calendar className="w-4 h-4 mr-2 text-gray-500" />
                {record.visit_date ? new Date(record.visit_date).toLocaleDateString() : 'Not specified'}
              </p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Record Created</label>
              <p className="text-gray-900">
                {new Date(record.created_at).toLocaleDateString()}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Medical Details */}
      <div className="p-6 bg-white rounded-lg shadow">
        <h2 className="flex items-center mb-4 text-lg font-semibold text-gray-900">
          <FileText className="w-5 h-5 mr-2 text-indigo-600" />
          Medical Details
        </h2>
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
          <div>
            <label className="block mb-2 text-sm font-medium text-gray-700">Diagnosis</label>
            <div className="p-4 rounded-lg bg-gray-50">
              <p className="text-gray-900 whitespace-pre-wrap">{record.diagnosis || 'Not specified'}</p>
            </div>
          </div>
          <div>
            <label className="block mb-2 text-sm font-medium text-gray-700">Treatment</label>
            <div className="p-4 rounded-lg bg-gray-50">
              <p className="text-gray-900 whitespace-pre-wrap">{record.treatment || 'Not specified'}</p>
            </div>
          </div>
        </div>
        
        {record.notes && (
          <div className="mt-6">
            <label className="block mb-2 text-sm font-medium text-gray-700">Additional Notes</label>
            <div className="p-4 rounded-lg bg-gray-50">
              <p className="text-gray-900 whitespace-pre-wrap">{record.notes}</p>
            </div>
          </div>
        )}
      </div>

      {/* Action Buttons */}
      <div className="flex justify-end space-x-4">
        <button
          onClick={generateSingleRecordReport}
          className="flex items-center px-4 py-2 space-x-2 text-white transition-colors duration-200 bg-gray-600 rounded-lg hover:bg-gray-700"
        >
          <Download className="w-4 h-4" />
          <span>Download Report</span>
        </button>
        <button
          onClick={handleEditClick}
          className="px-4 py-2 text-white transition-colors duration-200 bg-indigo-600 rounded-lg hover:bg-indigo-700"
        >
          Edit Record
        </button>
      </div>

      {/* Edit Medical Record Modal */}
      <Modal
        isOpen={showEditModal}
        onClose={() => setShowEditModal(false)}
        title="Edit Medical Record"
      >
        <div className="max-h-[70vh] overflow-y-auto">
          <form onSubmit={handleEditRecord} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block mb-1 text-sm font-medium text-gray-700">
                  Patient
                </label>
                <select 
                  className="input"
                  value={editRecordForm.patient_id}
                  onChange={(e) => setEditRecordForm({...editRecordForm, patient_id: e.target.value})}
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
                  value={editRecordForm.doctor_id}
                  onChange={(e) => setEditRecordForm({...editRecordForm, doctor_id: e.target.value})}
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
                  Record Type
                </label>
                <select 
                  className="input"
                  value={editRecordForm.record_type}
                  onChange={(e) => setEditRecordForm({...editRecordForm, record_type: e.target.value})}
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
                <label className="block mb-1 text-sm font-medium text-gray-700">
                  Appointment ID (Optional)
                </label>
                <input
                  type="number"
                  className="input"
                  value={editRecordForm.appointment_id}
                  onChange={(e) => setEditRecordForm({...editRecordForm, appointment_id: e.target.value})}
                  placeholder="Appointment ID"
                />
              </div>
            </div>
            
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Title
              </label>
              <input
                type="text"
                required
                className="input"
                value={editRecordForm.title}
                onChange={(e) => setEditRecordForm({...editRecordForm, title: e.target.value})}
                placeholder="Record title"
              />
            </div>
            
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Description
              </label>
              <textarea
                className="input"
                rows={3}
                value={editRecordForm.description}
                onChange={(e) => setEditRecordForm({...editRecordForm, description: e.target.value})}
                placeholder="Detailed description..."
                required
              />
            </div>
            
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block mb-1 text-sm font-medium text-gray-700">
                  Diagnosis
                </label>
                <textarea
                  className="input"
                  rows={2}
                  value={editRecordForm.diagnosis}
                  onChange={(e) => setEditRecordForm({...editRecordForm, diagnosis: e.target.value})}
                  placeholder="Diagnosis details..."
                />
              </div>
              <div>
                <label className="block mb-1 text-sm font-medium text-gray-700">
                  Treatment
                </label>
                <textarea
                  className="input"
                  rows={2}
                  value={editRecordForm.treatment}
                  onChange={(e) => setEditRecordForm({...editRecordForm, treatment: e.target.value})}
                  placeholder="Treatment plan..."
                />
              </div>
            </div>
            
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block mb-1 text-sm font-medium text-gray-700">
                  Medications
                </label>
                <textarea
                  className="input"
                  rows={2}
                  value={editRecordForm.medications}
                  onChange={(e) => setEditRecordForm({...editRecordForm, medications: e.target.value})}
                  placeholder="Prescribed medications..."
                />
              </div>
              <div>
                <label className="block mb-1 text-sm font-medium text-gray-700">
                  Lab Results
                </label>
                <textarea
                  className="input"
                  rows={2}
                  value={editRecordForm.lab_results}
                  onChange={(e) => setEditRecordForm({...editRecordForm, lab_results: e.target.value})}
                  placeholder="Lab test results..."
                />
              </div>
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
                className="inline-flex items-center px-4 py-2 text-sm font-medium text-white transition-all duration-200 bg-indigo-600 border border-transparent rounded-lg shadow-sm hover:bg-indigo-700 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
                disabled={editRecordMutation.isLoading}
              >
                {editRecordMutation.isLoading && (
                  <svg className="w-4 h-4 mr-2 animate-spin" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="m4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                )}
                {editRecordMutation.isLoading ? 'Updating...' : 'Update Record'}
              </button>
            </div>
          </form>
        </div>
      </Modal>
    </div>
  )
}

export default MedicalRecordDetailPage
