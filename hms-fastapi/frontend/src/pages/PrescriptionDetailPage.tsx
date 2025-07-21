import React, { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { ArrowLeft, Pill, User, Stethoscope, Calendar, Download, Edit } from 'lucide-react'
import jsPDF from 'jspdf'
import api from '../lib/api'
import Modal from '../components/Modal'
import { LoadingPrescriptionDetail } from '../components/loading/DetailLoadingStates'

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
  const [showEditModal, setShowEditModal] = useState(false)
  
  const [editPrescriptionForm, setEditPrescriptionForm] = useState({
    patient_id: '',
    doctor_id: '',
    medication_name: '',
    dosage: '',
    frequency: '',
    duration: '',
    quantity: '',
    instructions: '',
    status: ''
  })

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

  // Fetch patients and doctors for edit form dropdowns
  const { data: patients } = useQuery('patients', async () => {
    const response = await api.get('/api/patients')
    return response.data
  })

  const { data: doctors } = useQuery('doctors', async () => {
    const response = await api.get('/api/users?role=doctor')
    return response.data
  })

  const editPrescriptionMutation = useMutation(
    (prescriptionData: { id: number } & typeof editPrescriptionForm) => {
      const payload = {
        patient_id: parseInt(prescriptionData.patient_id),
        doctor_id: parseInt(prescriptionData.doctor_id),
        medication_name: prescriptionData.medication_name,
        dosage: prescriptionData.dosage,
        frequency: prescriptionData.frequency,
        duration: prescriptionData.duration,
        quantity: parseInt(prescriptionData.quantity),
        instructions: prescriptionData.instructions,
        status: prescriptionData.status
      }
      return api.put(`/api/prescriptions/${prescriptionData.id}`, payload)
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['prescription', id])
        queryClient.invalidateQueries('prescriptions')
        setShowEditModal(false)
        setEditPrescriptionForm({
          patient_id: '',
          doctor_id: '',
          medication_name: '',
          dosage: '',
          frequency: '',
          duration: '',
          quantity: '',
          instructions: '',
          status: ''
        })
      },
      onError: (error: any) => {
        console.error('Error updating prescription:', error)
        alert('Failed to update prescription. Please try again.')
      }
    }
  )

  const handleEditPrescription = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!prescription) return
    editPrescriptionMutation.mutate({ ...editPrescriptionForm, id: prescription.id })
  }

  const handleEditClick = () => {
    if (!prescription) return
    setEditPrescriptionForm({
      patient_id: prescription.patient_id.toString(),
      doctor_id: prescription.doctor_id.toString(),
      medication_name: prescription.medication_name,
      dosage: prescription.dosage,
      frequency: prescription.frequency,
      duration: prescription.duration,
      quantity: prescription.quantity.toString(),
      instructions: prescription.instructions || '',
      status: prescription.status
    })
    setShowEditModal(true)
  }

  const generateSinglePrescriptionReport = () => {
    if (!prescription) return

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
    doc.text('Prescription Report', pageWidth / 2, 20, { align: 'center' })
    
    doc.setFontSize(12)
    doc.setFont('helvetica', 'normal')
    doc.text(`Generated on: ${new Date().toLocaleDateString()}`, pageWidth / 2, 35, { align: 'center' })
    
    yPosition = 50

    // Prescription header
    doc.setFontSize(16)
    doc.setFont('helvetica', 'bold')
    doc.text(`Prescription #${prescription.id} - ${prescription.medication_name}`, margin, yPosition)
    yPosition += 20

    doc.setFontSize(12)
    doc.setFont('helvetica', 'normal')
    doc.text(`Prescribed Date: ${new Date(prescription.created_at).toLocaleDateString()}`, margin, yPosition)
    yPosition += 20

    // Patient Information
    doc.setFontSize(14)
    doc.setFont('helvetica', 'bold')
    doc.text('Patient Information', margin, yPosition)
    yPosition += 15

    addSection('Patient Name', prescription.patient ? `${prescription.patient.first_name} ${prescription.patient.last_name}` : 'Unknown')
    addSection('Email', prescription.patient?.email || 'Not provided')
    yPosition += 10

    // Doctor Information
    doc.setFontSize(14)
    doc.setFont('helvetica', 'bold')
    doc.text('Prescribing Doctor Information', margin, yPosition)
    yPosition += 15

    addSection('Doctor', prescription.doctor ? `Dr. ${prescription.doctor.first_name} ${prescription.doctor.last_name}` : 'N/A')
    if (prescription.doctor?.specialization) {
      addSection('Specialization', prescription.doctor.specialization)
    }
    yPosition += 10

    // Prescription Details
    doc.setFontSize(14)
    doc.setFont('helvetica', 'bold')
    doc.text('Prescription Details', margin, yPosition)
    yPosition += 15

    addSection('Medication', prescription.medication_name)
    addSection('Dosage', prescription.dosage)
    addSection('Frequency', prescription.frequency)
    addSection('Duration', prescription.duration)
    addSection('Quantity', `${prescription.quantity} units`)
    addSection('Status', prescription.status.charAt(0).toUpperCase() + prescription.status.slice(1))
    
    if (prescription.instructions) addSection('Instructions', prescription.instructions)

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
    const fileName = `prescription-${prescription.id}-${new Date().toISOString().split('T')[0]}.pdf`
    doc.save(fileName)
  }

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

  if (isLoading || error) {
    return <LoadingPrescriptionDetail />
  }

  if (error || !prescription) {
    return (
      <div className="p-4 border border-red-200 rounded-md bg-red-50">
        <p className="text-red-800">Failed to load prescription details</p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-4">
          <button
            onClick={() => navigate('/pharmacy')}
            className="p-2 transition-colors duration-200 bg-gray-100 rounded-lg hover:bg-gray-200"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <div>
            <h1 className="text-2xl font-bold text-gray-900">
              Prescription #{prescription.id}
            </h1>
            <p className="text-gray-600">{prescription.medication_name}</p>
          </div>
        </div>
        
        <div className="flex space-x-3">
          <button
            onClick={generateSinglePrescriptionReport}
            className="flex items-center px-4 py-2 space-x-2 text-white transition-colors duration-200 bg-purple-600 rounded-lg hover:bg-purple-700"
          >
            <Download className="w-4 h-4" />
            <span>Download Report</span>
          </button>
          <button
            onClick={handleEditClick}
            className="flex items-center px-4 py-2 space-x-2 text-white transition-colors duration-200 bg-indigo-600 rounded-lg hover:bg-indigo-700"
          >
            <Edit className="w-4 h-4" />
            <span>Edit Prescription</span>
          </button>
        </div>
      </div>

      {/* Prescription Information Cards */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Medication Information */}
        <div className="p-6 bg-white rounded-lg shadow">
          <h2 className="flex items-center mb-4 text-lg font-semibold text-gray-900">
            <Pill className="w-5 h-5 mr-2 text-purple-600" />
            Medication Information
          </h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Medication</label>
              <p className="text-lg font-semibold text-gray-900">{prescription.medication_name}</p>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">Dosage</label>
                <p className="text-gray-900">{prescription.dosage}</p>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">Frequency</label>
                <p className="text-gray-900">{prescription.frequency}</p>
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">Duration</label>
                <p className="text-gray-900">{prescription.duration}</p>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">Quantity</label>
                <p className="text-gray-900">{prescription.quantity} units</p>
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Status</label>
              <span className={`inline-flex px-3 py-1 text-sm font-semibold rounded-full ${getStatusColor(prescription.status)}`}>
                {prescription.status.charAt(0).toUpperCase() + prescription.status.slice(1)}
              </span>
            </div>
          </div>
        </div>

        {/* Patient & Doctor Information */}
        <div className="p-6 bg-white rounded-lg shadow">
          <h2 className="flex items-center mb-4 text-lg font-semibold text-gray-900">
            <User className="w-5 h-5 mr-2 text-purple-600" />
            Patient & Doctor
          </h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Patient</label>
              <p className="font-semibold text-gray-900">
                {prescription.patient.first_name} {prescription.patient.last_name}
              </p>
              <p className="text-sm text-gray-500">{prescription.patient.email}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Prescribing Doctor</label>
              <p className="flex items-center font-semibold text-gray-900">
                <Stethoscope className="w-4 h-4 mr-2 text-gray-500" />
                Dr. {prescription.doctor.first_name} {prescription.doctor.last_name}
              </p>
              {prescription.doctor.specialization && (
                <p className="ml-6 text-sm text-gray-500">{prescription.doctor.specialization}</p>
              )}
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Prescribed Date</label>
              <p className="flex items-center text-gray-900">
                <Calendar className="w-4 h-4 mr-2 text-gray-500" />
                {new Date(prescription.created_at).toLocaleDateString('en-US', { 
                  year: 'numeric', 
                  month: 'long', 
                  day: 'numeric' 
                })}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Instructions */}
      {prescription.instructions && (
        <div className="p-6 bg-white rounded-lg shadow">
          <h2 className="mb-4 text-lg font-semibold text-gray-900">Special Instructions</h2>
          <div className="p-4 border rounded-lg bg-amber-50 border-amber-200">
            <p className="leading-relaxed text-gray-900 whitespace-pre-wrap">{prescription.instructions}</p>
          </div>
        </div>
      )}

      {/* Status Action Buttons */}
      {prescription.status !== 'dispensed' && prescription.status !== 'cancelled' && (
        <div className="p-6 bg-white rounded-lg shadow">
          <h2 className="mb-4 text-lg font-semibold text-gray-900">Status Actions</h2>
          <div className="flex space-x-4">
            {prescription.status === 'pending' && (
              <button
                onClick={() => updateStatusMutation.mutate('ready')}
                className="px-6 py-2 text-white transition-colors duration-200 bg-blue-600 rounded-lg hover:bg-blue-700"
                disabled={updateStatusMutation.isLoading}
              >
                Mark as Ready
              </button>
            )}
            {prescription.status === 'ready' && (
              <button
                onClick={() => updateStatusMutation.mutate('dispensed')}
                className="px-6 py-2 text-white transition-colors duration-200 bg-green-600 rounded-lg hover:bg-green-700"
                disabled={updateStatusMutation.isLoading}
              >
                Mark as Dispensed
              </button>
            )}
            <button
              onClick={() => updateStatusMutation.mutate('cancelled')}
              className="px-6 py-2 text-white transition-colors duration-200 bg-red-600 rounded-lg hover:bg-red-700"
              disabled={updateStatusMutation.isLoading}
            >
              Cancel Prescription
            </button>
          </div>
        </div>
      )}

      {/* Edit Prescription Modal */}
      <Modal
        isOpen={showEditModal}
        onClose={() => setShowEditModal(false)}
        title="Edit Prescription"
      >
        <form onSubmit={handleEditPrescription} className="space-y-4 overflow-y-auto max-h-96">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Patient
              </label>
              <select 
                className="input"
                value={editPrescriptionForm.patient_id}
                onChange={(e) => setEditPrescriptionForm({...editPrescriptionForm, patient_id: e.target.value})}
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
                value={editPrescriptionForm.doctor_id}
                onChange={(e) => setEditPrescriptionForm({...editPrescriptionForm, doctor_id: e.target.value})}
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
                Medication Name
              </label>
              <input
                type="text"
                required
                className="input"
                value={editPrescriptionForm.medication_name}
                onChange={(e) => setEditPrescriptionForm({...editPrescriptionForm, medication_name: e.target.value})}
                placeholder="e.g., Amoxicillin"
              />
            </div>
            
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Dosage
              </label>
              <input
                type="text"
                required
                className="input"
                value={editPrescriptionForm.dosage}
                onChange={(e) => setEditPrescriptionForm({...editPrescriptionForm, dosage: e.target.value})}
                placeholder="e.g., 500mg"
              />
            </div>
          </div>
          
          <div className="grid grid-cols-3 gap-4">
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Frequency
              </label>
              <input
                type="text"
                required
                className="input"
                value={editPrescriptionForm.frequency}
                onChange={(e) => setEditPrescriptionForm({...editPrescriptionForm, frequency: e.target.value})}
                placeholder="e.g., 3 times daily"
              />
            </div>
            
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Duration
              </label>
              <input
                type="text"
                required
                className="input"
                value={editPrescriptionForm.duration}
                onChange={(e) => setEditPrescriptionForm({...editPrescriptionForm, duration: e.target.value})}
                placeholder="e.g., 7 days"
              />
            </div>
            
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Quantity
              </label>
              <input
                type="number"
                required
                min="1"
                className="input"
                value={editPrescriptionForm.quantity}
                onChange={(e) => setEditPrescriptionForm({...editPrescriptionForm, quantity: e.target.value})}
                placeholder="Number of units"
              />
            </div>
          </div>
          
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Status
            </label>
            <select
              className="input"
              value={editPrescriptionForm.status}
              onChange={(e) => setEditPrescriptionForm({...editPrescriptionForm, status: e.target.value})}
              required
            >
              <option value="pending">Pending</option>
              <option value="ready">Ready</option>
              <option value="dispensed">Dispensed</option>
              <option value="cancelled">Cancelled</option>
            </select>
          </div>
          
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Special Instructions
            </label>
            <textarea
              className="input"
              rows={3}
              value={editPrescriptionForm.instructions}
              onChange={(e) => setEditPrescriptionForm({...editPrescriptionForm, instructions: e.target.value})}
              placeholder="Special instructions for the patient (e.g., take with food, avoid alcohol...)"
            />
          </div>
          
          <div className="flex justify-end pt-6 space-x-3 border-t border-gray-200">
            <button
              type="button"
              onClick={() => setShowEditModal(false)}
              className="btn-secondary"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn-primary"
              disabled={editPrescriptionMutation.isLoading}
            >
              {editPrescriptionMutation.isLoading ? 'Updating...' : 'Update Prescription'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}

export default PrescriptionDetailPage
