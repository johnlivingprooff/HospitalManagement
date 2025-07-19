import React from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery } from 'react-query'
import { ArrowLeft, FileText, DollarSign, Calendar, User, CreditCard } from 'lucide-react'
import api from '../services/api'

interface Bill {
  id: number
  patient_id: number
  amount: number
  status: string
  description: string
  created_at: string
  due_date: string
  patient: {
    first_name: string
    last_name: string
    email: string
  }
}

const BillDetailPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()

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
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    )
  }

  if (error || !bill) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-md p-4">
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
          className="bg-gray-100 hover:bg-gray-200 p-2 rounded-lg transition-colors duration-200"
        >
          <ArrowLeft className="h-5 w-5" />
        </button>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            Bill #{bill.id}
          </h1>
          <p className="text-gray-600">Bill Details</p>
        </div>
      </div>

      {/* Bill Information Cards */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Bill Information */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
            <FileText className="h-5 w-5 mr-2 text-blue-600" />
            Bill Information
          </h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Bill ID</label>
              <p className="text-gray-900">#{bill.id}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Amount</label>
              <p className="text-gray-900 flex items-center text-lg font-semibold">
                <DollarSign className="h-4 w-4 mr-1 text-green-600" />
                ${bill.amount.toFixed(2)}
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
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
            <User className="h-5 w-5 mr-2 text-blue-600" />
            Patient & Dates
          </h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Patient</label>
              <p className="text-gray-900">
                {bill.patient.first_name} {bill.patient.last_name}
              </p>
              <p className="text-sm text-gray-500">{bill.patient.email}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Issue Date</label>
              <p className="text-gray-900 flex items-center">
                <Calendar className="h-4 w-4 mr-2 text-gray-500" />
                {new Date(bill.created_at).toLocaleDateString()}
              </p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Due Date</label>
              <p className="text-gray-900 flex items-center">
                <Calendar className="h-4 w-4 mr-2 text-gray-500" />
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
            className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-lg transition-colors duration-200 flex items-center space-x-2"
          >
            <CreditCard className="h-4 w-4" />
            <span>Process Payment</span>
          </button>
        )}
        <button
          onClick={() => window.print()}
          className="bg-gray-600 hover:bg-gray-700 text-white px-4 py-2 rounded-lg transition-colors duration-200"
        >
          Print Bill
        </button>
      </div>
    </div>
  )
}

export default BillDetailPage
