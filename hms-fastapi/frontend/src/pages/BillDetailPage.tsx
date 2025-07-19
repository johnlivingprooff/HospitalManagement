import React, { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { ArrowLeft, FileText, DollarSign, Calendar, User, CreditCard, Download } from 'lucide-react'
import jsPDF from 'jspdf'
import api from '../lib/api'
import Modal from '../components/Modal'

interface Bill {
  id: number
  patient_id: number
  bill_number: string
  total_amount: number
  paid_amount: number
  status: string
  due_date: string
  description: string
  created_at: string
  patient?: {
    id: number
    first_name: string
    last_name: string
    email: string
  }
}

const BillDetailPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [showEditModal, setShowEditModal] = useState(false)
  
  const [editBillForm, setEditBillForm] = useState({
    patient_id: '',
    total_amount: '',
    status: '',
    description: '',
    due_date: ''
  })

  const { data: bill, isLoading, error } = useQuery<Bill>(
    ['bill', id],
    async () => {
      const response = await api.get(`/api/bills/${id}`)
      return response.data
    },
    {
      enabled: !!id
    }
  )

  // Fetch patients for edit form dropdown
  const { data: patients } = useQuery('patients', async () => {
    const response = await api.get('/api/patients')
    return response.data
  })

  const editBillMutation = useMutation(
    (billData: { id: number } & typeof editBillForm) => {
      const payload = {
        patient_id: parseInt(billData.patient_id),
        total_amount: Math.round(parseFloat(billData.total_amount) * 100), // Convert to cents
        status: billData.status,
        description: billData.description,
        due_date: billData.due_date
      }
      return api.put(`/api/bills/${billData.id}`, payload)
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['bill', id])
        queryClient.invalidateQueries('bills')
        setShowEditModal(false)
        setEditBillForm({
          patient_id: '',
          total_amount: '',
          status: '',
          description: '',
          due_date: ''
        })
      },
      onError: (error: any) => {
        console.error('Error updating bill:', error)
        alert('Failed to update bill. Please try again.')
      }
    }
  )

  const handleEditBill = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!bill) return
    editBillMutation.mutate({ ...editBillForm, id: bill.id })
  }

  const handleEditClick = () => {
    if (!bill) return
    setEditBillForm({
      patient_id: bill.patient_id.toString(),
      total_amount: (bill.total_amount / 100).toString(), // Convert from cents
      status: bill.status,
      description: bill.description,
      due_date: bill.due_date.split('T')[0] // Format date for input
    })
    setShowEditModal(true)
  }

  const generateSingleBillReport = () => {
    if (!bill) return

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
    doc.text('Bill Report', pageWidth / 2, 20, { align: 'center' })
    
    doc.setFontSize(12)
    doc.setFont('helvetica', 'normal')
    doc.text(`Generated on: ${new Date().toLocaleDateString()}`, pageWidth / 2, 35, { align: 'center' })
    
    yPosition = 50

    // Bill header
    doc.setFontSize(16)
    doc.setFont('helvetica', 'bold')
    doc.text(`Bill #${bill.id}`, margin, yPosition)
    yPosition += 20

    doc.setFontSize(12)
    doc.setFont('helvetica', 'normal')
    doc.text(`Issue Date: ${new Date(bill.created_at).toLocaleDateString()}`, margin, yPosition)
    yPosition += 15
    doc.text(`Due Date: ${new Date(bill.due_date).toLocaleDateString()}`, margin, yPosition)
    yPosition += 20

    // Patient Information
    doc.setFontSize(14)
    doc.setFont('helvetica', 'bold')
    doc.text('Patient Information', margin, yPosition)
    yPosition += 15

    addSection('Patient Name', bill.patient ? `${bill.patient.first_name} ${bill.patient.last_name}` : 'Unknown')
    addSection('Email', bill.patient?.email || 'Not provided')
    yPosition += 10

    // Bill Details
    doc.setFontSize(14)
    doc.setFont('helvetica', 'bold')
    doc.text('Bill Details', margin, yPosition)
    yPosition += 15

    addSection('Amount', `$${(bill.total_amount / 100).toFixed(2)}`)
    addSection('Status', bill.status.charAt(0).toUpperCase() + bill.status.slice(1))
    addSection('Description', bill.description)

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
    const fileName = `bill-${bill.id}-${new Date().toISOString().split('T')[0]}.pdf`
    doc.save(fileName)
  }

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'paid': return 'bg-green-100 text-green-800'
      case 'pending': return 'bg-yellow-100 text-yellow-800'
      case 'overdue': return 'bg-red-100 text-red-800'
      default: return 'bg-gray-100 text-gray-800'
    }
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="w-12 h-12 border-b-2 border-blue-600 rounded-full animate-spin"></div>
      </div>
    )
  }

  if (error || !bill) {
    return (
      <div className="p-4 border border-red-200 rounded-md bg-red-50">
        <p className="text-red-800">Failed to load bill details</p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center space-x-4">
        <button
          onClick={() => navigate('/bills')}
          className="p-2 transition-colors duration-200 bg-gray-100 rounded-lg hover:bg-gray-200"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            Bill #{bill.id}
          </h1>
          <p className="text-gray-600">Bill Details</p>
        </div>
      </div>

      {/* Bill Information Cards */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Bill Information */}
        <div className="p-6 bg-white rounded-lg shadow">
          <h2 className="flex items-center mb-4 text-lg font-semibold text-gray-900">
            <FileText className="w-5 h-5 mr-2 text-blue-600" />
            Bill Information
          </h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Bill ID</label>
              <p className="text-gray-900">#{bill.id}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Amount</label>
              <p className="flex items-center text-lg font-semibold text-gray-900">
                <DollarSign className="w-4 h-4 mr-1 text-green-600" />
                ${(bill.total_amount / 100).toFixed(2)}
              </p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Status</label>
              <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(bill.status)}`}>
                {bill.status.charAt(0).toUpperCase() + bill.status.slice(1)}
              </span>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Description</label>
              <p className="text-gray-900">{bill.description}</p>
            </div>
          </div>
        </div>

        {/* Patient & Dates */}
        <div className="p-6 bg-white rounded-lg shadow">
          <h2 className="flex items-center mb-4 text-lg font-semibold text-gray-900">
            <User className="w-5 h-5 mr-2 text-blue-600" />
            Patient & Dates
          </h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Patient</label>
              <p className="text-gray-900">
                {bill.patient ? `${bill.patient.first_name} ${bill.patient.last_name}` : 'Unknown Patient'}
              </p>
              <p className="text-sm text-gray-500">{bill.patient?.email || 'No email provided'}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Issue Date</label>
              <p className="flex items-center text-gray-900">
                <Calendar className="w-4 h-4 mr-2 text-gray-500" />
                {new Date(bill.created_at).toLocaleDateString()}
              </p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Due Date</label>
              <p className="flex items-center text-gray-900">
                <Calendar className="w-4 h-4 mr-2 text-gray-500" />
                {new Date(bill.due_date).toLocaleDateString()}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="flex justify-end space-x-4">
        {bill.status !== 'paid' && (
          <button
            onClick={() => {/* Implement payment logic */}}
            className="flex items-center px-4 py-2 space-x-2 text-white transition-colors duration-200 bg-green-600 rounded-lg hover:bg-green-700"
          >
            <CreditCard className="w-4 h-4" />
            <span>Process Payment</span>
          </button>
        )}
        <button
          onClick={generateSingleBillReport}
          className="flex items-center px-4 py-2 space-x-2 text-white transition-colors duration-200 bg-purple-600 rounded-lg hover:bg-purple-700"
        >
          <Download className="w-4 h-4" />
          <span>Download Bill</span>
        </button>
        <button
          onClick={handleEditClick}
          className="px-4 py-2 text-white transition-colors duration-200 bg-blue-600 rounded-lg hover:bg-blue-700"
        >
          Edit Bill
        </button>
      </div>

      {/* Edit Bill Modal */}
      <Modal
        isOpen={showEditModal}
        onClose={() => setShowEditModal(false)}
        title="Edit Bill"
      >
        <form onSubmit={handleEditBill} className="space-y-4 overflow-y-auto max-h-96">
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Patient
            </label>
            <select 
              className="input"
              value={editBillForm.patient_id}
              onChange={(e) => setEditBillForm({...editBillForm, patient_id: e.target.value})}
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
          
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Amount
              </label>
              <input
                type="number"
                step="0.01"
                required
                className="input"
                value={editBillForm.total_amount}
                onChange={(e) => setEditBillForm({...editBillForm, total_amount: e.target.value})}
              />
            </div>
            
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Status
              </label>
              <select
                className="input"
                value={editBillForm.status}
                onChange={(e) => setEditBillForm({...editBillForm, status: e.target.value})}
                required
              >
                <option value="pending">Pending</option>
                <option value="paid">Paid</option>
                <option value="partially_paid">Partially Paid</option>
                <option value="overdue">Overdue</option>
              </select>
            </div>
          </div>
          
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Due Date
            </label>
            <input
              type="date"
              className="input"
              value={editBillForm.due_date}
              onChange={(e) => setEditBillForm({...editBillForm, due_date: e.target.value})}
            />
          </div>
          
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Description
            </label>
            <textarea
              className="input"
              rows={3}
              value={editBillForm.description}
              onChange={(e) => setEditBillForm({...editBillForm, description: e.target.value})}
              placeholder="Description of services or items..."
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
              disabled={editBillMutation.isLoading}
            >
              {editBillMutation.isLoading ? 'Updating...' : 'Update Bill'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}

export default BillDetailPage
