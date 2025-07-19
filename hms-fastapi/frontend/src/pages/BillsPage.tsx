import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { useNavigate } from 'react-router-dom'
import { CreditCardIcon, DollarSignIcon, FileTextIcon, Plus } from 'lucide-react'
import api from '../lib/api'
import Modal from '../components/Modal'
import SearchInput from '../components/SearchInput'
import { useClientSearch } from '../hooks/useOptimizedSearch'

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
  }
}

const BillsPage = () => {
  const [showAddModal, setShowAddModal] = useState(false)
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState('all')
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const [billForm, setBillForm] = useState({
    patient_id: '',
    bill_number: '',
    total_amount: '',
    description: '',
    due_date: '',
    status: 'pending'
  })

  const { data: bills, isLoading, error } = useQuery<Bill[]>(
    'bills',
    async () => {
      const response = await api.get('/api/bills')
      return response.data
    },
    {
      staleTime: 5 * 60 * 1000, // 5 minutes
    }
  )

  // Fetch patients for dropdown
  const { data: patients } = useQuery('patients', async () => {
    const response = await api.get('/api/patients')
    return response.data
  })

  const addBillMutation = useMutation(
    (billData: typeof billForm) => {
      const payload = {
        ...billData,
        patient_id: parseInt(billData.patient_id),
        total_amount: Math.round(parseFloat(billData.total_amount) * 100), // Convert to cents
        paid_amount: 0 // Default to 0 for new bills
      }
      return api.post('/api/bills', payload)
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries('bills')
        setShowAddModal(false)
        setBillForm({
          patient_id: '',
          bill_number: '',
          total_amount: '',
          description: '',
          due_date: '',
          status: 'pending'
        })
      },
      onError: (error: any) => {
        console.error('Error creating bill:', error)
        alert('Failed to create bill. Please try again.')
      }
    }
  )

  const handleAddBill = async (e: React.FormEvent) => {
    e.preventDefault()
    addBillMutation.mutate(billForm)
  }

  const formatCurrency = (cents: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(cents / 100)
  }

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'paid': return 'bg-green-100 text-green-800'
      case 'pending': return 'bg-yellow-100 text-yellow-800'
      case 'partially_paid': return 'bg-blue-100 text-blue-800'
      case 'overdue': return 'bg-red-100 text-red-800'
      default: return 'bg-gray-100 text-gray-800'
    }
  }

  // Apply client-side filtering and search
  const filteredBills = useClientSearch(
    bills,
    searchTerm,
    ['bill_number', 'description'],
    [
      // Status filter
      (bill) => statusFilter === 'all' || bill.status === statusFilter,
      // Manual search for nested patient fields
      (bill) => {
        if (!searchTerm) return true
        const searchLower = searchTerm.toLowerCase()
        
        // Search in patient name
        const patientMatch = bill.patient ? 
          `${bill.patient.first_name} ${bill.patient.last_name}`.toLowerCase().includes(searchLower) : false
        
        return patientMatch
      }
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
        <p className="text-red-800">Error loading bills. Please try again.</p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Bills & Payments</h1>
        <button 
          className="inline-flex items-center px-4 py-2 text-sm font-medium text-white transition-all duration-200 bg-blue-600 border border-transparent rounded-lg shadow-sm hover:bg-blue-700 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
          onClick={() => setShowAddModal(true)}
        >
          <Plus className="w-4 h-4 mr-2" />
          Create New Bill
        </button>
      </div>

      {/* Search and Filters */}
      <div className="p-4 bg-white rounded-lg shadow">
        <div className="flex flex-col gap-4 md:flex-row">
          <div className="flex-1">
            <SearchInput
              value={searchTerm}
              onChange={setSearchTerm}
              placeholder="Search by bill number, patient name, or description..."
              className="w-full"
            />
          </div>
          <div>
            <select
              className="input"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="all">All Statuses</option>
              <option value="pending">Pending</option>
              <option value="partially_paid">Partially Paid</option>
              <option value="paid">Paid</option>
              <option value="overdue">Overdue</option>
            </select>
          </div>
        </div>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 gap-4 md:grid-cols-4">
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center">
            <DollarSignIcon className="w-8 h-8 text-green-600" />
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Total Revenue</p>
              <p className="text-2xl font-bold text-gray-900">
                {formatCurrency(bills?.reduce((sum, bill) => sum + bill.paid_amount, 0) || 0)}
              </p>
            </div>
          </div>
        </div>
        
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center">
            <CreditCardIcon className="w-8 h-8 text-blue-600" />
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Outstanding</p>
              <p className="text-2xl font-bold text-gray-900">
                {formatCurrency(bills?.reduce((sum, bill) => sum + (bill.total_amount - bill.paid_amount), 0) || 0)}
              </p>
            </div>
          </div>
        </div>
        
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center">
            <FileTextIcon className="w-8 h-8 text-yellow-600" />
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Pending Bills</p>
              <p className="text-2xl font-bold text-gray-900">
                {bills?.filter(bill => bill.status === 'pending').length || 0}
              </p>
            </div>
          </div>
        </div>
        
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center">
            <DollarSignIcon className="w-8 h-8 text-red-600" />
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Overdue</p>
              <p className="text-2xl font-bold text-gray-900">
                {bills?.filter(bill => bill.status === 'overdue').length || 0}
              </p>
            </div>
          </div>
        </div>
      </div>

      <div className="overflow-hidden bg-white rounded-lg shadow">
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-lg font-medium text-gray-900">Recent Bills</h2>
        </div>
        
        {!bills || bills.length === 0 ? (
          <div className="p-6 text-center text-gray-500">
            No bills found.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                    Bill #
                  </th>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                    Patient
                  </th>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                    Amount
                  </th>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                    Paid
                  </th>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                    Status
                  </th>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                    Due Date
                  </th>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredBills.map((bill) => (
                  <tr key={bill.id}>
                    <td className="px-6 py-4 text-sm font-medium text-gray-900 whitespace-nowrap">
                      {bill.bill_number}
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-900 whitespace-nowrap">
                      {bill.patient ? `${bill.patient.first_name} ${bill.patient.last_name}` : 'N/A'}
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-900 whitespace-nowrap">
                      {formatCurrency(bill.total_amount)}
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-900 whitespace-nowrap">
                      {formatCurrency(bill.paid_amount)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(bill.status)}`}>
                        {bill.status.replace('_', ' ')}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-900 whitespace-nowrap">
                      {bill.due_date ? new Date(bill.due_date).toLocaleDateString() : 'N/A'}
                    </td>
                    <td className="px-6 py-4 text-sm font-medium whitespace-nowrap">
                      <div className="flex space-x-2">
                        <button 
                          onClick={() => navigate(`/bills/${bill.id}`)}
                          className="bg-blue-100 hover:bg-blue-200 text-blue-700 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors duration-200"
                        >
                          View
                        </button>
                        <button className="bg-green-100 hover:bg-green-200 text-green-700 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors duration-200">
                          Pay
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

      {/* Create Bill Modal */}
      <Modal
        isOpen={showAddModal}
        onClose={() => setShowAddModal(false)}
        title="Create New Bill"
      >
        <form onSubmit={handleAddBill} className="space-y-4">
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Patient
            </label>
            <select 
              className="input"
              value={billForm.patient_id}
              onChange={(e) => setBillForm({...billForm, patient_id: e.target.value})}
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
                Bill Number
              </label>
              <input
                type="text"
                required
                className="input"
                value={billForm.bill_number}
                onChange={(e) => setBillForm({...billForm, bill_number: e.target.value})}
                placeholder="e.g., BILL-2025-001"
              />
            </div>
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Total Amount ($)
              </label>
              <input
                type="number"
                step="0.01"
                required
                className="input"
                value={billForm.total_amount}
                onChange={(e) => setBillForm({...billForm, total_amount: e.target.value})}
                placeholder="0.00"
              />
            </div>
          </div>
          
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Due Date
            </label>
            <input
              type="date"
              className="input"
              value={billForm.due_date}
              onChange={(e) => setBillForm({...billForm, due_date: e.target.value})}
            />
          </div>
          
          <div>
            <label className="block mb-1 text-sm font-medium text-gray-700">
              Description
            </label>
            <textarea
              className="input"
              rows={3}
              value={billForm.description}
              onChange={(e) => setBillForm({...billForm, description: e.target.value})}
              placeholder="Description of services or items..."
            />
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
              className="inline-flex items-center px-4 py-2 text-sm font-medium text-white transition-all duration-200 bg-blue-600 border border-transparent rounded-lg shadow-sm hover:bg-blue-700 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
              disabled={addBillMutation.isLoading}
            >
              {addBillMutation.isLoading && (
                <svg className="w-4 h-4 mr-2 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="m4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
              )}
              {addBillMutation.isLoading ? 'Creating...' : 'Create Bill'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}

export default BillsPage
