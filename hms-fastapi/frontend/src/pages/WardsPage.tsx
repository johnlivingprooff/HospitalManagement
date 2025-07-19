import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { useNavigate } from 'react-router-dom'
import { 
  Building2, 
  Bed, 
  Users, 
  Plus, 
  Edit, 
  Trash2, 
  UserPlus,
  UserMinus,
  Eye,
  CheckCircle
} from 'lucide-react'
import api from '../lib/api'
import Modal from '../components/Modal'
import SearchInput from '../components/SearchInput'
import ProtectedPage from '../components/ProtectedPage'
import { useClientSearch } from '../hooks/useOptimizedSearch'

interface Ward {
  id: number
  name: string
  type: string
  capacity: number
  current_occupancy: number
  floor: number
  description?: string
  is_active: boolean
  created_at: string
  patients?: WardPatient[]
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
  }
  doctor?: {
    id: number
    first_name: string
    last_name: string
  }
}

const WardsPage = () => {
  const [searchTerm, setSearchTerm] = useState('')
  const [wardTypeFilter, setWardTypeFilter] = useState('all')
  const [statusFilter, setStatusFilter] = useState('all')
  const [showAddWardModal, setShowAddWardModal] = useState(false)
  const [showEditWardModal, setShowEditWardModal] = useState(false)
  const [showDeleteWardModal, setShowDeleteWardModal] = useState(false)
  const [showAdmitPatientModal, setShowAdmitPatientModal] = useState(false)
  const [showDischargeModal, setShowDischargeModal] = useState(false)
  const [selectedWard, setSelectedWard] = useState<Ward | null>(null)
  const [selectedWardPatient, setSelectedWardPatient] = useState<WardPatient | null>(null)
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const [wardForm, setWardForm] = useState({
    name: '',
    type: 'general',
    capacity: '',
    floor: '',
    description: ''
  })

  const [editWardForm, setEditWardForm] = useState({
    name: '',
    type: 'general',
    capacity: '',
    floor: '',
    description: '',
    is_active: true
  })

  const [admitForm, setAdmitForm] = useState({
    patient_id: '',
    doctor_id: '',
    bed_number: '',
    notes: ''
  })

  const [dischargeForm, setDischargeForm] = useState({
    discharge_date: new Date().toISOString().split('T')[0],
    notes: ''
  })

  // Fetch wards with patient information
  const { data: wards, isLoading, error } = useQuery<Ward[]>(
    'wards',
    async () => {
      const response = await api.get('/api/wards')
      return response.data
    },
    {
      staleTime: 2 * 60 * 1000,
    }
  )

  // Fetch patients for admission dropdown
  const { data: patients } = useQuery('available-patients', async () => {
    const response = await api.get('/api/patients?status=available')
    return response.data
  })

  // Fetch doctors for admission
  const { data: doctors } = useQuery('doctors', async () => {
    const response = await api.get('/api/users?role=doctor')
    return response.data
  })

  // Ward mutations
  const addWardMutation = useMutation(
    (wardData: typeof wardForm) => {
      const payload = {
        ...wardData,
        capacity: parseInt(wardData.capacity),
        floor: parseInt(wardData.floor)
      }
      return api.post('/api/wards', payload)
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries('wards')
        setShowAddWardModal(false)
        setWardForm({
          name: '',
          type: 'general',
          capacity: '',
          floor: '',
          description: ''
        })
      },
      onError: (error: any) => {
        console.error('Error creating ward:', error)
        alert('Failed to create ward. Please try again.')
      }
    }
  )

  const editWardMutation = useMutation(
    (wardData: { id: number } & typeof editWardForm) => {
      const { id, ...payload } = wardData
      const formattedPayload = {
        ...payload,
        capacity: parseInt(payload.capacity.toString()),
        floor: parseInt(payload.floor.toString())
      }
      return api.put(`/api/wards/${id}`, formattedPayload)
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries('wards')
        setShowEditWardModal(false)
        setSelectedWard(null)
      }
    }
  )

  const deleteWardMutation = useMutation(
    (wardId: number) => api.delete(`/api/wards/${wardId}`),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('wards')
        setShowDeleteWardModal(false)
        setSelectedWard(null)
      }
    }
  )

  // Patient admission/discharge mutations
  const admitPatientMutation = useMutation(
    (admissionData: { wardId: number } & typeof admitForm) => {
      const { wardId, ...payload } = admissionData
      return api.post(`/api/wards/${wardId}/patients`, {
        ...payload,
        patient_id: parseInt(payload.patient_id),
        doctor_id: parseInt(payload.doctor_id)
      })
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries('wards')
        queryClient.invalidateQueries('available-patients')
        setShowAdmitPatientModal(false)
        setSelectedWard(null)
        setAdmitForm({
          patient_id: '',
          doctor_id: '',
          bed_number: '',
          notes: ''
        })
      }
    }
  )

  const dischargePatientMutation = useMutation(
    (dischargeData: { wardPatientId: number } & typeof dischargeForm) => {
      const { wardPatientId, ...payload } = dischargeData
      return api.put(`/api/ward-patients/${wardPatientId}/discharge`, payload)
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries('wards')
        queryClient.invalidateQueries('available-patients')
        setShowDischargeModal(false)
        setSelectedWardPatient(null)
        setDischargeForm({
          discharge_date: new Date().toISOString().split('T')[0],
          notes: ''
        })
      }
    }
  )

  // Event handlers
  const handleAddWard = async (e: React.FormEvent) => {
    e.preventDefault()
    addWardMutation.mutate(wardForm)
  }

  const handleEditWard = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedWard) return
    editWardMutation.mutate({ ...editWardForm, id: selectedWard.id })
  }

  const handleEditClick = (ward: Ward) => {
    setSelectedWard(ward)
    setEditWardForm({
      name: ward.name,
      type: ward.type,
      capacity: ward.capacity.toString(),
      floor: ward.floor.toString(),
      description: ward.description || '',
      is_active: ward.is_active
    })
    setShowEditWardModal(true)
  }

  const handleDeleteClick = (ward: Ward) => {
    setSelectedWard(ward)
    setShowDeleteWardModal(true)
  }

  const handleAdmitClick = (ward: Ward) => {
    setSelectedWard(ward)
    setShowAdmitPatientModal(true)
  }

  const handleDischargeClick = (wardPatient: WardPatient) => {
    setSelectedWardPatient(wardPatient)
    setShowDischargeModal(true)
  }

  const handleAdmitPatient = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedWard) return
    admitPatientMutation.mutate({ wardId: selectedWard.id, ...admitForm })
  }

  const handleDischargePatient = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedWardPatient) return
    dischargePatientMutation.mutate({ wardPatientId: selectedWardPatient.id, ...dischargeForm })
  }

  // Utility functions
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

  const getOccupancyColor = (current: number, capacity: number) => {
    const percentage = (current / capacity) * 100
    if (percentage >= 90) return 'text-red-600'
    if (percentage >= 75) return 'text-orange-600'
    return 'text-green-600'
  }

//   const getPatientStatusColor = (status: string) => {
//     switch (status.toLowerCase()) {
//       case 'admitted': return 'bg-blue-100 text-blue-800'
//       case 'stable': return 'bg-green-100 text-green-800'
//       case 'critical': return 'bg-red-100 text-red-800'
//       case 'recovering': return 'bg-yellow-100 text-yellow-800'
//       default: return 'bg-gray-100 text-gray-800'
//     }
//   }

  // Apply client-side filtering
  const filteredWards = useClientSearch(
    wards,
    searchTerm,
    ['name', 'type', 'description'],
    [
      (ward) => wardTypeFilter === 'all' || ward.type === wardTypeFilter,
      (ward) => statusFilter === 'all' || 
        (statusFilter === 'available' && ward.current_occupancy < ward.capacity) ||
        (statusFilter === 'full' && ward.current_occupancy >= ward.capacity) ||
        (statusFilter === 'active' && ward.is_active) ||
        (statusFilter === 'inactive' && !ward.is_active)
    ]
  ) || []

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="w-12 h-12 border-b-2 border-blue-600 rounded-full animate-spin"></div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="p-4 border border-red-200 rounded-md bg-red-50">
        <p className="text-red-800">Error loading wards. Please try again.</p>
      </div>
    )
  }

  return (
    <ProtectedPage resource="wards" action="read">
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Ward Management</h1>
            <p className="text-gray-600">Manage hospital wards and patient admissions</p>
          </div>
          <button 
            onClick={() => setShowAddWardModal(true)}
            className="inline-flex items-center px-4 py-2 text-sm font-medium text-white transition-all duration-200 bg-blue-600 border border-transparent rounded-lg shadow-sm hover:bg-blue-700 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
          >
            <Plus className="w-4 h-4 mr-2" />
            Add New Ward
          </button>
        </div>

        {/* Search and Filters */}
        <div className="p-4 bg-white rounded-lg shadow">
          <div className="flex flex-col gap-4 md:flex-row">
            <div className="flex-1">
              <SearchInput
                value={searchTerm}
                onChange={setSearchTerm}
                placeholder="Search wards by name, type, or description..."
                className="w-full"
              />
            </div>
            <div className="flex gap-4">
              <select
                className="input"
                value={wardTypeFilter}
                onChange={(e) => setWardTypeFilter(e.target.value)}
              >
                <option value="all">All Types</option>
                <option value="general">General</option>
                <option value="icu">ICU</option>
                <option value="emergency">Emergency</option>
                <option value="surgery">Surgery</option>
                <option value="maternity">Maternity</option>
                <option value="pediatric">Pediatric</option>
              </select>
              <select
                className="input"
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
              >
                <option value="all">All Status</option>
                <option value="active">Active</option>
                <option value="inactive">Inactive</option>
                <option value="available">Available Beds</option>
                <option value="full">Full Capacity</option>
              </select>
            </div>
          </div>
        </div>

        {/* Summary Cards */}
        <div className="grid grid-cols-1 gap-4 md:grid-cols-4">
          <div className="p-6 bg-white rounded-lg shadow">
            <div className="flex items-center">
              <Building2 className="w-8 h-8 text-blue-600" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Total Wards</p>
                <p className="text-2xl font-bold text-gray-900">
                  {wards?.length || 0}
                </p>
              </div>
            </div>
          </div>
          
          <div className="p-6 bg-white rounded-lg shadow">
            <div className="flex items-center">
              <Bed className="w-8 h-8 text-green-600" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Total Beds</p>
                <p className="text-2xl font-bold text-gray-900">
                  {wards?.reduce((sum, ward) => sum + ward.capacity, 0) || 0}
                </p>
              </div>
            </div>
          </div>
          
          <div className="p-6 bg-white rounded-lg shadow">
            <div className="flex items-center">
              <Users className="w-8 h-8 text-orange-600" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Occupied Beds</p>
                <p className="text-2xl font-bold text-gray-900">
                  {wards?.reduce((sum, ward) => sum + ward.current_occupancy, 0) || 0}
                </p>
              </div>
            </div>
          </div>
          
          <div className="p-6 bg-white rounded-lg shadow">
            <div className="flex items-center">
              <CheckCircle className="w-8 h-8 text-purple-600" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Available Beds</p>
                <p className="text-2xl font-bold text-gray-900">
                  {wards?.reduce((sum, ward) => sum + (ward.capacity - ward.current_occupancy), 0) || 0}
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Wards Grid */}
        <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
          {filteredWards.map((ward) => (
            <div key={ward.id} className="transition-shadow duration-200 bg-white rounded-lg shadow hover:shadow-md">
              <div className="p-6">
                <div className="flex items-center justify-between mb-4">
                  <div className="flex items-center">
                    <Building2 className="w-6 h-6 mr-2 text-blue-600" />
                    <h3 className="text-lg font-semibold text-gray-900">{ward.name}</h3>
                  </div>
                  <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getWardTypeColor(ward.type)}`}>
                    {ward.type.toUpperCase()}
                  </span>
                </div>
                
                <div className="mb-4 space-y-3">
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-600">Floor:</span>
                    <span className="text-sm font-medium text-gray-900">Level {ward.floor}</span>
                  </div>
                  
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-600">Capacity:</span>
                    <span className={`text-sm font-medium ${getOccupancyColor(ward.current_occupancy, ward.capacity)}`}>
                      {ward.current_occupancy}/{ward.capacity} beds
                    </span>
                  </div>
                  
                  <div className="w-full h-2 bg-gray-200 rounded-full">
                    <div 
                      className={`h-2 rounded-full transition-all duration-300 ${
                        (ward.current_occupancy / ward.capacity) >= 0.9 ? 'bg-red-500' :
                        (ward.current_occupancy / ward.capacity) >= 0.75 ? 'bg-orange-500' : 'bg-green-500'
                      }`}
                      style={{ width: `${Math.min((ward.current_occupancy / ward.capacity) * 100, 100)}%` }}
                    ></div>
                  </div>
                  
                  {ward.description && (
                    <p className="text-sm text-gray-600 line-clamp-2">{ward.description}</p>
                  )}
                </div>

                {/* Ward Actions */}
                <div className="flex items-center justify-between pt-4 border-t border-gray-200">
                  <div className="flex space-x-2">
                    <button 
                      onClick={() => handleEditClick(ward)}
                      className="bg-blue-100 hover:bg-blue-200 text-blue-700 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors duration-200"
                    >
                      <Edit className="w-4 h-4" />
                    </button>
                    <button 
                      onClick={() => navigate(`/wards/${ward.id}`)}
                      className="bg-green-100 hover:bg-green-200 text-green-700 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors duration-200"
                    >
                      <Eye className="w-4 h-4" />
                    </button>
                    <button 
                      onClick={() => handleDeleteClick(ward)}
                      className="bg-red-100 hover:bg-red-200 text-red-700 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors duration-200"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                  <button 
                    onClick={() => handleAdmitClick(ward)}
                    disabled={ward.current_occupancy >= ward.capacity}
                    className="bg-purple-100 hover:bg-purple-200 text-purple-700 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors duration-200 disabled:opacity-50 disabled:cursor-not-allowed flex items-center"
                  >
                    <UserPlus className="w-4 h-4 mr-1" />
                    Admit
                  </button>
                </div>

                {/* Current Patients Preview */}
                {ward.patients && ward.patients.length > 0 && (
                  <div className="pt-4 mt-4 border-t border-gray-200">
                    <h4 className="mb-2 text-sm font-medium text-gray-900">Current Patients</h4>
                    <div className="space-y-2 overflow-y-auto max-h-32">
                      {ward.patients.slice(0, 3).map((wp) => (
                        <div key={wp.id} className="flex items-center justify-between text-xs">
                          <span className="text-gray-600">
                            {wp.patient.first_name} {wp.patient.last_name}
                          </span>
                          <div className="flex items-center space-x-2">
                            <span className="text-gray-500">Bed {wp.bed_number}</span>
                            <button 
                              onClick={() => handleDischargeClick(wp)}
                              className="px-2 py-1 text-xs text-orange-700 transition-colors duration-200 bg-orange-100 rounded hover:bg-orange-200"
                            >
                              <UserMinus className="w-3 h-3" />
                            </button>
                          </div>
                        </div>
                      ))}
                      {ward.patients.length > 3 && (
                        <div className="text-xs text-center text-gray-500">
                          +{ward.patients.length - 3} more patients
                        </div>
                      )}
                    </div>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>

        {filteredWards.length === 0 && (
          <div className="py-12 text-center">
            <Building2 className="w-12 h-12 mx-auto mb-4 text-gray-400" />
            <p className="text-gray-500">No wards found matching your criteria.</p>
          </div>
        )}

        {/* Add Ward Modal */}
        <Modal
          isOpen={showAddWardModal}
          onClose={() => setShowAddWardModal(false)}
          title="Add New Ward"
        >
          <form onSubmit={handleAddWard} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block mb-1 text-sm font-medium text-gray-700">
                  Ward Name
                </label>
                <input
                  type="text"
                  required
                  className="input"
                  value={wardForm.name}
                  onChange={(e) => setWardForm({...wardForm, name: e.target.value})}
                  placeholder="e.g., Ward A1"
                />
              </div>
              <div>
                <label className="block mb-1 text-sm font-medium text-gray-700">
                  Ward Type
                </label>
                <select 
                  className="input"
                  value={wardForm.type}
                  onChange={(e) => setWardForm({...wardForm, type: e.target.value})}
                  required
                >
                  <option value="general">General</option>
                  <option value="icu">ICU</option>
                  <option value="emergency">Emergency</option>
                  <option value="surgery">Surgery</option>
                  <option value="maternity">Maternity</option>
                  <option value="pediatric">Pediatric</option>
                </select>
              </div>
            </div>
            
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block mb-1 text-sm font-medium text-gray-700">
                  Capacity (beds)
                </label>
                <input
                  type="number"
                  required
                  min="1"
                  className="input"
                  value={wardForm.capacity}
                  onChange={(e) => setWardForm({...wardForm, capacity: e.target.value})}
                  placeholder="20"
                />
              </div>
              <div>
                <label className="block mb-1 text-sm font-medium text-gray-700">
                  Floor
                </label>
                <input
                  type="number"
                  required
                  min="1"
                  className="input"
                  value={wardForm.floor}
                  onChange={(e) => setWardForm({...wardForm, floor: e.target.value})}
                  placeholder="1"
                />
              </div>
            </div>
            
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Description
              </label>
              <textarea
                className="input"
                rows={3}
                value={wardForm.description}
                onChange={(e) => setWardForm({...wardForm, description: e.target.value})}
                placeholder="Ward description and special notes..."
              />
            </div>
            
            <div className="flex justify-end pt-6 space-x-3 border-t border-gray-200">
              <button
                type="button"
                onClick={() => setShowAddWardModal(false)}
                className="btn-secondary"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="btn-primary"
                disabled={addWardMutation.isLoading}
              >
                {addWardMutation.isLoading ? 'Creating...' : 'Create Ward'}
              </button>
            </div>
          </form>
        </Modal>

        {/* Edit Ward Modal */}
        <Modal
          isOpen={showEditWardModal}
          onClose={() => setShowEditWardModal(false)}
          title="Edit Ward"
        >
          <form onSubmit={handleEditWard} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block mb-1 text-sm font-medium text-gray-700">
                  Ward Name
                </label>
                <input
                  type="text"
                  required
                  className="input"
                  value={editWardForm.name}
                  onChange={(e) => setEditWardForm({...editWardForm, name: e.target.value})}
                />
              </div>
              <div>
                <label className="block mb-1 text-sm font-medium text-gray-700">
                  Ward Type
                </label>
                <select 
                  className="input"
                  value={editWardForm.type}
                  onChange={(e) => setEditWardForm({...editWardForm, type: e.target.value})}
                  required
                >
                  <option value="general">General</option>
                  <option value="icu">ICU</option>
                  <option value="emergency">Emergency</option>
                  <option value="surgery">Surgery</option>
                  <option value="maternity">Maternity</option>
                  <option value="pediatric">Pediatric</option>
                </select>
              </div>
            </div>
            
            <div className="grid grid-cols-3 gap-4">
              <div>
                <label className="block mb-1 text-sm font-medium text-gray-700">
                  Capacity
                </label>
                <input
                  type="number"
                  required
                  min="1"
                  className="input"
                  value={editWardForm.capacity}
                  onChange={(e) => setEditWardForm({...editWardForm, capacity: e.target.value})}
                />
              </div>
              <div>
                <label className="block mb-1 text-sm font-medium text-gray-700">
                  Floor
                </label>
                <input
                  type="number"
                  required
                  min="1"
                  className="input"
                  value={editWardForm.floor}
                  onChange={(e) => setEditWardForm({...editWardForm, floor: e.target.value})}
                />
              </div>
              <div>
                <label className="block mb-1 text-sm font-medium text-gray-700">
                  Status
                </label>
                <select 
                  className="input"
                  value={editWardForm.is_active.toString()}
                  onChange={(e) => setEditWardForm({...editWardForm, is_active: e.target.value === 'true'})}
                >
                  <option value="true">Active</option>
                  <option value="false">Inactive</option>
                </select>
              </div>
            </div>
            
            <div>
              <label className="block mb-1 text-sm font-medium text-gray-700">
                Description
              </label>
              <textarea
                className="input"
                rows={3}
                value={editWardForm.description}
                onChange={(e) => setEditWardForm({...editWardForm, description: e.target.value})}
              />
            </div>
            
            <div className="flex justify-end pt-6 space-x-3 border-t border-gray-200">
              <button
                type="button"
                onClick={() => setShowEditWardModal(false)}
                className="btn-secondary"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="btn-primary"
                disabled={editWardMutation.isLoading}
              >
                {editWardMutation.isLoading ? 'Updating...' : 'Update Ward'}
              </button>
            </div>
          </form>
        </Modal>

        {/* Delete Ward Modal */}
        <Modal
          isOpen={showDeleteWardModal}
          onClose={() => setShowDeleteWardModal(false)}
          title="Delete Ward"
        >
          <div className="space-y-4">
            <div className="p-4 border border-red-200 rounded-lg bg-red-50">
              <p className="text-red-800">
                Are you sure you want to delete <strong>{selectedWard?.name}</strong>?
                This action cannot be undone and will affect any associated patient records.
              </p>
            </div>
            
            <div className="flex justify-end space-x-3">
              <button
                type="button"
                onClick={() => setShowDeleteWardModal(false)}
                className="btn-secondary"
              >
                Cancel
              </button>
              <button
                onClick={() => selectedWard && deleteWardMutation.mutate(selectedWard.id)}
                className="px-4 py-2 text-white transition-colors duration-200 bg-red-600 rounded-lg hover:bg-red-700"
                disabled={deleteWardMutation.isLoading}
              >
                {deleteWardMutation.isLoading ? 'Deleting...' : 'Delete Ward'}
              </button>
            </div>
          </div>
        </Modal>

        {/* Admit Patient Modal */}
        <Modal
          isOpen={showAdmitPatientModal}
          onClose={() => setShowAdmitPatientModal(false)}
          title={`Admit Patient to ${selectedWard?.name}`}
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
                  {patients?.map((patient: any) => (
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
                onClick={() => setShowAdmitPatientModal(false)}
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
          title={`Discharge Patient`}
        >
          <form onSubmit={handleDischargePatient} className="space-y-4">
            {selectedWardPatient && (
              <div className="p-4 border border-blue-200 rounded-lg bg-blue-50">
                <h4 className="font-medium text-blue-900">
                  {selectedWardPatient.patient.first_name} {selectedWardPatient.patient.last_name}
                </h4>
                <p className="text-sm text-blue-700">
                  Bed: {selectedWardPatient.bed_number} | 
                  Admitted: {new Date(selectedWardPatient.admission_date).toLocaleDateString()}
                </p>
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
                Discharge Notes
              </label>
              <textarea
                className="input"
                rows={3}
                value={dischargeForm.notes}
                onChange={(e) => setDischargeForm({...dischargeForm, notes: e.target.value})}
                placeholder="Discharge summary, follow-up instructions..."
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
                className="px-4 py-2 text-white transition-colors duration-200 bg-orange-600 rounded-lg hover:bg-orange-700"
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

export default WardsPage
