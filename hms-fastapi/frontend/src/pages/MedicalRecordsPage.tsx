import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { useNavigate } from 'react-router-dom'
import { FileTextIcon, CalendarIcon, UserIcon, Plus, Download } from 'lucide-react'
import jsPDF from 'jspdf'
import api from '../lib/api'
import Modal from '../components/Modal'
import SearchInput from '../components/SearchInput'
import { useClientSearch } from '../hooks/useOptimizedSearch'
import { MedicalRecord } from '../types'

const MedicalRecordsPage = () => {
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedType, setSelectedType] = useState('all')
  const [showAddModal, setShowAddModal] = useState(false)
  const [showEditModal, setShowEditModal] = useState(false)
  const [editingRecord, setEditingRecord] = useState<MedicalRecord | null>(null)
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const [recordForm, setRecordForm] = useState({
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

  const { data: records, isLoading, error } = useQuery<MedicalRecord[]>(
    'medical-records',
    async () => {
      const response = await api.get('/api/medical-records')
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

  const addRecordMutation = useMutation(
    (recordData: typeof recordForm) => {
      const payload = {
        ...recordData,
        patient_id: parseInt(recordData.patient_id),
        doctor_id: parseInt(recordData.doctor_id),
        appointment_id: recordData.appointment_id ? parseInt(recordData.appointment_id) : undefined
      }
      return api.post('/api/medical-records', payload)
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries('medical-records')
        setShowAddModal(false)
        setRecordForm({
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
        console.error('Error creating medical record:', error)
        alert('Failed to create medical record. Please try again.')
      }
    }
  )

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
        queryClient.invalidateQueries('medical-records')
        setShowEditModal(false)
        setEditingRecord(null)
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

  const handleAddRecord = async (e: React.FormEvent) => {
    e.preventDefault()
    addRecordMutation.mutate(recordForm)
  }

  const handleEditRecord = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!editingRecord) return
    editRecordMutation.mutate({ ...editRecordForm, id: editingRecord.id })
  }

  const handleEditClick = (record: MedicalRecord) => {
    setEditingRecord(record)
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

  const generateReport = () => {
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
        addText(`${label}:`, 10, true)
        // Split long content into multiple lines
        const splitContent = doc.splitTextToSize(content, pageWidth - margin * 2)
        splitContent.forEach((line: string) => {
          addText(`  ${line}`, 10, false)
        })
        yPosition += 5 // Add some space after section
      }
    }

    // Header
    doc.setFontSize(20)
    doc.setFont('helvetica', 'bold')
    doc.text('Medical Records Report', pageWidth / 2, 20, { align: 'center' })
    
    doc.setFontSize(12)
    doc.setFont('helvetica', 'normal')
    doc.text(`Generated on: ${new Date().toLocaleDateString()}`, pageWidth / 2, 35, { align: 'center' })
    doc.text(`Total Records: ${filteredRecords.length}`, pageWidth / 2, 45, { align: 'center' })
    
    yPosition = 60

    // Add each record
    filteredRecords.forEach((record, index) => {
      // Check if we need a new page for this record
      if (yPosition > pageHeight - 100) {
        doc.addPage()
        yPosition = 30
      }

      // Record header
      doc.setFontSize(14)
      doc.setFont('helvetica', 'bold')
      doc.text(`Record #${record.id} - ${record.title}`, margin, yPosition)
      yPosition += 20

      doc.setFontSize(10)
      doc.setFont('helvetica', 'normal')
      doc.text(`Date: ${new Date(record.created_at).toLocaleDateString()}`, margin, yPosition)
      yPosition += 15

      // Record details
      addSection('Patient', record.patient ? `${record.patient.first_name} ${record.patient.last_name}` : 'Unknown')
      addSection('Doctor', record.doctor ? `Dr. ${record.doctor.first_name} ${record.doctor.last_name}` : 'N/A')
      addSection('Type', record.record_type.replace('_', ' ').toUpperCase())
      addSection('Description', record.description)
      
      if (record.diagnosis) addSection('Diagnosis', record.diagnosis)
      if (record.treatment) addSection('Treatment', record.treatment)
      if (record.medications) addSection('Medications', record.medications)
      if (record.lab_results) addSection('Lab Results', record.lab_results)

      // Add separator line between records
      if (index < filteredRecords.length - 1) {
        yPosition += 10
        doc.setDrawColor(200, 200, 200)
        doc.line(margin, yPosition, pageWidth - margin, yPosition)
        yPosition += 15
      }
    })

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
    const fileName = `medical-records-report-${new Date().toISOString().split('T')[0]}.pdf`
    doc.save(fileName)
  }

  const getTypeColor = (type: string) => {
    switch (type.toLowerCase()) {
      case 'consultation': return 'bg-blue-100 text-blue-800'
      case 'lab_result': return 'bg-green-100 text-green-800'
      case 'prescription': return 'bg-purple-100 text-purple-800'
      case 'diagnosis': return 'bg-red-100 text-red-800'
      case 'surgery': return 'bg-orange-100 text-orange-800'
      default: return 'bg-gray-100 text-gray-800'
    }
  }

  const filteredRecords = useClientSearch(
    records,
    searchTerm,
    ['title', 'description', 'diagnosis', 'treatment', 'medications', 'lab_results', 'record_type'],
    [
      // Record type filter
      (record) => selectedType === 'all' || record.record_type === selectedType,
      // Manual search for nested patient and doctor fields
      (record) => {
        if (!searchTerm) return true
        const searchLower = searchTerm.toLowerCase()
        
        // Search in patient name
        const patientMatch = record.patient ? 
          `${record.patient.first_name} ${record.patient.last_name}`.toLowerCase().includes(searchLower) : false
        
        // Search in doctor name
        const doctorMatch = record.doctor ? 
          `${record.doctor.first_name} ${record.doctor.last_name}`.toLowerCase().includes(searchLower) : false
        
        return patientMatch || doctorMatch
      }
    ]
  )

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
        <p className="text-red-800">Error loading medical records. Please try again.</p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Medical Records</h1>
        <div className="flex items-center space-x-3">
          <button 
            onClick={generateReport}
            className="inline-flex items-center px-4 py-2 text-sm font-medium text-gray-700 transition-all duration-200 bg-white border border-gray-300 rounded-lg shadow-sm hover:bg-gray-50 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2"
          >
            <Download className="w-4 h-4 mr-2" />
            Report
          </button>
          <button 
            className="inline-flex items-center px-4 py-2 text-sm font-medium text-white transition-all duration-200 bg-indigo-600 border border-transparent rounded-lg shadow-sm hover:bg-indigo-700 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
            onClick={() => setShowAddModal(true)}
          >
            <Plus className="w-4 h-4 mr-2" />
            Add New Record
          </button>
        </div>
      </div>

      {/* Filters */}
      <div className="p-4 bg-white rounded-lg shadow">
        <div className="flex flex-col gap-4 md:flex-row">
          <div className="flex-1">
            <SearchInput
              value={searchTerm}
              onChange={setSearchTerm}
              placeholder="Search by patient name, record title, or diagnosis..."
              className="w-full"
            />
          </div>
          <div>
            <select
              className="input"
              value={selectedType}
              onChange={(e) => setSelectedType(e.target.value)}
            >
              <option value="all">All Types</option>
              <option value="consultation">Consultation</option>
              <option value="lab_result">Lab Result</option>
              <option value="prescription">Prescription</option>
              <option value="diagnosis">Diagnosis</option>
              <option value="surgery">Surgery</option>
            </select>
          </div>
        </div>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 gap-4 md:grid-cols-4">
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center">
            <FileTextIcon className="w-8 h-8 text-blue-600" />
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Total Records</p>
              <p className="text-2xl font-bold text-gray-900">{records?.length || 0}</p>
            </div>
          </div>
        </div>
        
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center">
            <UserIcon className="w-8 h-8 text-green-600" />
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Consultations</p>
              <p className="text-2xl font-bold text-gray-900">
                {records?.filter(r => r.record_type === 'consultation').length || 0}
              </p>
            </div>
          </div>
        </div>
        
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center">
            <FileTextIcon className="w-8 h-8 text-purple-600" />
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Lab Results</p>
              <p className="text-2xl font-bold text-gray-900">
                {records?.filter(r => r.record_type === 'lab_result').length || 0}
              </p>
            </div>
          </div>
        </div>
        
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center">
            <CalendarIcon className="w-8 h-8 text-orange-600" />
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">This Month</p>
              <p className="text-2xl font-bold text-gray-900">
                {records?.filter(r => new Date(r.created_at).getMonth() === new Date().getMonth()).length || 0}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Records Table */}
      <div className="overflow-hidden bg-white rounded-lg shadow">
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-lg font-medium text-gray-900">Medical Records ({filteredRecords.length})</h2>
        </div>
        
        {filteredRecords.length === 0 ? (
          <div className="p-6 text-center text-gray-500">
            No medical records found.
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
                    Record Title
                  </th>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                    Type
                  </th>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                    Doctor
                  </th>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                    Date
                  </th>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredRecords.map((record) => (
                  <tr key={record.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">
                        {record.patient ? `${record.patient.first_name} ${record.patient.last_name}` : 'Unknown'}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">{record.title}</div>
                      {record.description && (
                        <div className="max-w-xs text-sm text-gray-500 truncate">
                          {record.description}
                        </div>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getTypeColor(record.record_type)}`}>
                        {record.record_type.replace('_', ' ')}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-900 whitespace-nowrap">
                      {record.doctor ? `Dr. ${record.doctor.first_name} ${record.doctor.last_name}` : 'N/A'}
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-900 whitespace-nowrap">
                      {new Date(record.created_at).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 text-sm font-medium whitespace-nowrap">
                      <div className="flex space-x-2">
                        <button 
                          onClick={() => navigate(`/medical-records/${record.id}`)}
                          className="bg-indigo-100 hover:bg-indigo-200 text-indigo-700 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors duration-200"
                        >
                          View
                        </button>
                        <button 
                          onClick={() => handleEditClick(record)}
                          className="bg-green-100 hover:bg-green-200 text-green-700 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors duration-200"
                        >
                          Edit
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

      {/* Edit Medical Record Modal */}
      <Modal
        isOpen={showEditModal}
        onClose={() => setShowEditModal(false)}
        title="Edit Medical Record"
      >
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
      </Modal>

      {/* Add Medical Record Modal */}
      <Modal
        isOpen={showAddModal}
        onClose={() => setShowAddModal(false)}
        title="Add New Medical Record"
      >
        <form onSubmit={handleAddRecord} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Patient
              </label>
              <select 
                className="input"
                value={recordForm.patient_id}
                onChange={(e) => setRecordForm({...recordForm, patient_id: e.target.value})}
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
                value={recordForm.doctor_id}
                onChange={(e) => setRecordForm({...recordForm, doctor_id: e.target.value})}
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
                value={recordForm.record_type}
                onChange={(e) => setRecordForm({...recordForm, record_type: e.target.value})}
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
                value={recordForm.appointment_id}
                onChange={(e) => setRecordForm({...recordForm, appointment_id: e.target.value})}
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
              value={recordForm.title}
              onChange={(e) => setRecordForm({...recordForm, title: e.target.value})}
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
              value={recordForm.description}
              onChange={(e) => setRecordForm({...recordForm, description: e.target.value})}
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
                value={recordForm.diagnosis}
                onChange={(e) => setRecordForm({...recordForm, diagnosis: e.target.value})}
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
                value={recordForm.treatment}
                onChange={(e) => setRecordForm({...recordForm, treatment: e.target.value})}
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
                value={recordForm.medications}
                onChange={(e) => setRecordForm({...recordForm, medications: e.target.value})}
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
                value={recordForm.lab_results}
                onChange={(e) => setRecordForm({...recordForm, lab_results: e.target.value})}
                placeholder="Lab test results..."
              />
            </div>
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
              className="inline-flex items-center px-4 py-2 text-sm font-medium text-white transition-all duration-200 bg-indigo-600 border border-transparent rounded-lg shadow-sm hover:bg-indigo-700 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
              disabled={addRecordMutation.isLoading}
            >
              {addRecordMutation.isLoading && (
                <svg className="w-4 h-4 mr-2 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="m4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
              )}
              {addRecordMutation.isLoading ? 'Creating...' : 'Create Record'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}

export default MedicalRecordsPage
