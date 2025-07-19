import React, { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { ArrowLeft, TestTube2, User, Stethoscope, Calendar, FileText, Download } from 'lucide-react'
import jsPDF from 'jspdf'
import api from '../lib/api'
import Modal from '../components/Modal'

interface LabTest {
  id: number
  patient_id: number
  doctor_id: number
  test_name: string
  test_type: string
  status: string
  ordered_date: string
  completed_date?: string
  results?: string
  notes?: string
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

const LabTestDetailPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [showEditModal, setShowEditModal] = useState(false)
  
  const [editTestForm, setEditTestForm] = useState({
    patient_id: '',
    doctor_id: '',
    test_name: '',
    test_type: '',
    status: '',
    notes: '',
    results: ''
  })

  const { data: test, isLoading, error } = useQuery<LabTest>(
    ['lab-test', id],
    async () => {
      const response = await api.get(`/api/lab-tests/${id}`)
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

  const editTestMutation = useMutation(
    (testData: { id: number } & typeof editTestForm) => {
      const payload = {
        patient_id: parseInt(testData.patient_id),
        doctor_id: parseInt(testData.doctor_id),
        test_name: testData.test_name,
        test_type: testData.test_type,
        status: testData.status,
        notes: testData.notes,
        results: testData.results
      }
      return api.put(`/api/lab-tests/${testData.id}`, payload)
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['lab-test', id])
        queryClient.invalidateQueries('lab-tests')
        setShowEditModal(false)
        setEditTestForm({
          patient_id: '',
          doctor_id: '',
          test_name: '',
          test_type: '',
          status: '',
          notes: '',
          results: ''
        })
      },
      onError: (error: any) => {
        console.error('Error updating lab test:', error)
        alert('Failed to update lab test. Please try again.')
      }
    }
  )

  const handleEditTest = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!test) return
    editTestMutation.mutate({ ...editTestForm, id: test.id })
  }

  const handleEditClick = () => {
    if (!test) return
    setEditTestForm({
      patient_id: test.patient_id.toString(),
      doctor_id: test.doctor_id.toString(),
      test_name: test.test_name,
      test_type: test.test_type,
      status: test.status,
      notes: test.notes || '',
      results: test.results || ''
    })
    setShowEditModal(true)
  }

  const generateSingleTestReport = () => {
    if (!test) return

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
    doc.text('Lab Test Report', pageWidth / 2, 20, { align: 'center' })
    
    doc.setFontSize(12)
    doc.setFont('helvetica', 'normal')
    doc.text(`Generated on: ${new Date().toLocaleDateString()}`, pageWidth / 2, 35, { align: 'center' })
    
    yPosition = 50

    // Test header
    doc.setFontSize(16)
    doc.setFont('helvetica', 'bold')
    doc.text(`Test #${test.id} - ${test.test_name}`, margin, yPosition)
    yPosition += 20

    doc.setFontSize(12)
    doc.setFont('helvetica', 'normal')
    doc.text(`Ordered Date: ${new Date(test.ordered_date).toLocaleDateString()}`, margin, yPosition)
    if (test.completed_date) {
      yPosition += 15
      doc.text(`Completed Date: ${new Date(test.completed_date).toLocaleDateString()}`, margin, yPosition)
    }
    yPosition += 20

    // Patient Information
    doc.setFontSize(14)
    doc.setFont('helvetica', 'bold')
    doc.text('Patient Information', margin, yPosition)
    yPosition += 15

    addSection('Patient Name', test.patient ? `${test.patient.first_name} ${test.patient.last_name}` : 'Unknown')
    addSection('Email', test.patient?.email || 'Not provided')
    yPosition += 10

    // Doctor Information
    doc.setFontSize(14)
    doc.setFont('helvetica', 'bold')
    doc.text('Ordering Doctor Information', margin, yPosition)
    yPosition += 15

    addSection('Doctor', test.doctor ? `Dr. ${test.doctor.first_name} ${test.doctor.last_name}` : 'N/A')
    if (test.doctor?.specialization) {
      addSection('Specialization', test.doctor.specialization)
    }
    yPosition += 10

    // Test Details
    doc.setFontSize(14)
    doc.setFont('helvetica', 'bold')
    doc.text('Test Details', margin, yPosition)
    yPosition += 15

    addSection('Test Type', test.test_type)
    addSection('Status', test.status.replace('_', ' ').toUpperCase())
    
    if (test.results) addSection('Results', test.results)
    if (test.notes) addSection('Notes', test.notes)

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
    const fileName = `lab-test-${test.id}-${new Date().toISOString().split('T')[0]}.pdf`
    doc.save(fileName)
  }

  const updateStatusMutation = useMutation(
    (newStatus: string) => api.put(`/api/lab-tests/${id}`, { status: newStatus }),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['lab-test', id])
        queryClient.invalidateQueries('lab-tests')
      }
    }
  )

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'pending': return 'bg-yellow-100 text-yellow-800'
      case 'in_progress': return 'bg-blue-100 text-blue-800'
      case 'completed': return 'bg-green-100 text-green-800'
      case 'cancelled': return 'bg-red-100 text-red-800'
      default: return 'bg-gray-100 text-gray-800'
    }
  }

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-cyan-600"></div>
      </div>
    )
  }

  if (error || !test) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-md p-4">
        <p className="text-red-800">Failed to load lab test details</p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center space-x-4">
        <button
          onClick={() => navigate('/lab')}
          className="bg-gray-100 hover:bg-gray-200 p-2 rounded-lg transition-colors duration-200"
        >
          <ArrowLeft className="h-5 w-5" />
        </button>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            Lab Test #{test.id}
          </h1>
          <p className="text-gray-600">{test.test_name}</p>
        </div>
      </div>

      {/* Lab Test Information Cards */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Test Information */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
            <TestTube2 className="h-5 w-5 mr-2 text-cyan-600" />
            Test Information
          </h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Test Name</label>
              <p className="text-gray-900 font-semibold">{test.test_name}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Test Type</label>
              <p className="text-gray-900">{test.test_type}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Status</label>
              <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(test.status)}`}>
                {test.status.replace('_', ' ').charAt(0).toUpperCase() + test.status.replace('_', ' ').slice(1)}
              </span>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Ordered Date</label>
              <p className="text-gray-900 flex items-center">
                <Calendar className="h-4 w-4 mr-2 text-gray-500" />
                {new Date(test.ordered_date).toLocaleDateString()}
              </p>
            </div>
            {test.completed_date && (
              <div>
                <label className="block text-sm font-medium text-gray-700">Completed Date</label>
                <p className="text-gray-900 flex items-center">
                  <Calendar className="h-4 w-4 mr-2 text-gray-500" />
                  {new Date(test.completed_date).toLocaleDateString()}
                </p>
              </div>
            )}
          </div>
        </div>

        {/* Patient & Doctor Information */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
            <User className="h-5 w-5 mr-2 text-cyan-600" />
            Patient & Doctor
          </h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Patient</label>
              <p className="text-gray-900">
                {test.patient.first_name} {test.patient.last_name}
              </p>
              <p className="text-sm text-gray-500">{test.patient.email}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Ordering Doctor</label>
              <p className="text-gray-900 flex items-center">
                <Stethoscope className="h-4 w-4 mr-2 text-gray-500" />
                Dr. {test.doctor.first_name} {test.doctor.last_name}
              </p>
              {test.doctor.specialization && (
                <p className="text-sm text-gray-500">{test.doctor.specialization}</p>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Results Section */}
      {test.results && (
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
            <FileText className="h-5 w-5 mr-2 text-cyan-600" />
            Test Results
          </h2>
          <div className="bg-green-50 border border-green-200 rounded-lg p-4">
            <p className="text-gray-900 whitespace-pre-wrap">{test.results}</p>
          </div>
        </div>
      )}

      {/* Notes Section */}
      {test.notes && (
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Notes</h2>
          <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
            <p className="text-gray-900 whitespace-pre-wrap">{test.notes}</p>
          </div>
        </div>
      )}

      {/* Action Buttons */}
      <div className="flex justify-end space-x-4">
        {test.status === 'pending' && (
          <button
            onClick={() => updateStatusMutation.mutate('in_progress')}
            className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg transition-colors duration-200"
            disabled={updateStatusMutation.isLoading}
          >
            Start Test
          </button>
        )}
        {test.status === 'in_progress' && (
          <button
            onClick={() => updateStatusMutation.mutate('completed')}
            className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-lg transition-colors duration-200"
            disabled={updateStatusMutation.isLoading}
          >
            Complete Test
          </button>
        )}
        <button
          onClick={generateSingleTestReport}
          className="bg-purple-600 hover:bg-purple-700 text-white px-4 py-2 rounded-lg transition-colors duration-200 flex items-center space-x-2"
        >
          <Download className="h-4 w-4" />
          <span>Download Report</span>
        </button>
        <button
          onClick={handleEditClick}
          className="bg-cyan-600 hover:bg-cyan-700 text-white px-4 py-2 rounded-lg transition-colors duration-200"
        >
          Edit Test
        </button>
      </div>

      {/* Edit Test Modal */}
      <Modal
        isOpen={showEditModal}
        onClose={() => setShowEditModal(false)}
        title="Edit Lab Test"
      >
        <form onSubmit={handleEditTest} className="space-y-4 max-h-96 overflow-y-auto">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Patient
              </label>
              <select 
                className="input"
                value={editTestForm.patient_id}
                onChange={(e) => setEditTestForm({...editTestForm, patient_id: e.target.value})}
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
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Doctor
              </label>
              <select 
                className="input"
                value={editTestForm.doctor_id}
                onChange={(e) => setEditTestForm({...editTestForm, doctor_id: e.target.value})}
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
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Test Name
              </label>
              <input
                type="text"
                required
                className="input"
                value={editTestForm.test_name}
                onChange={(e) => setEditTestForm({...editTestForm, test_name: e.target.value})}
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Test Type
              </label>
              <input
                type="text"
                required
                className="input"
                value={editTestForm.test_type}
                onChange={(e) => setEditTestForm({...editTestForm, test_type: e.target.value})}
              />
            </div>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Status
            </label>
            <select
              className="input"
              value={editTestForm.status}
              onChange={(e) => setEditTestForm({...editTestForm, status: e.target.value})}
              required
            >
              <option value="pending">Pending</option>
              <option value="in_progress">In Progress</option>
              <option value="completed">Completed</option>
              <option value="cancelled">Cancelled</option>
            </select>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Results
            </label>
            <textarea
              className="input"
              rows={3}
              value={editTestForm.results}
              onChange={(e) => setEditTestForm({...editTestForm, results: e.target.value})}
              placeholder="Enter test results..."
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Notes
            </label>
            <textarea
              className="input"
              rows={3}
              value={editTestForm.notes}
              onChange={(e) => setEditTestForm({...editTestForm, notes: e.target.value})}
              placeholder="Enter additional notes..."
            />
          </div>
          
          <div className="flex justify-end space-x-3 pt-6 border-t border-gray-200">
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
              disabled={editTestMutation.isLoading}
            >
              {editTestMutation.isLoading ? 'Updating...' : 'Update Test'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}

export default LabTestDetailPage
