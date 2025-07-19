import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { User } from '../types'
import api from '../lib/api'
import { UserIcon, MailIcon, Plus } from 'lucide-react'
import Modal from '../components/Modal'

const DoctorsPage = () => {
  const [showAddModal, setShowAddModal] = useState(false)
  const queryClient = useQueryClient()

  const [doctorForm, setDoctorForm] = useState({
    first_name: '',
    last_name: '',
    email: '',
    phone: '',
    specialization: '',
    license_number: ''
  })

  const { data: doctors, isLoading, error } = useQuery<User[]>(
    'doctors',
    async () => {
      const response = await api.get('/api/users?role=doctor')
      return response.data
    },
    {
      staleTime: 5 * 60 * 1000, // 5 minutes
    }
  )

  const addDoctorMutation = useMutation(
    (doctorData: typeof doctorForm) => {
      const payload = {
        ...doctorData,
        role: 'doctor',
        password: 'temp123' // Temporary password - should be changed on first login
      }
      return api.post('/api/users', payload)
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries('doctors')
        setShowAddModal(false)
        setDoctorForm({
          first_name: '',
          last_name: '',
          email: '',
          phone: '',
          specialization: '',
          license_number: ''
        })
      },
      onError: (error: any) => {
        console.error('Error adding doctor:', error)
        alert('Failed to add doctor. Please try again.')
      }
    }
  )

  const handleAddDoctor = async (e: React.FormEvent) => {
    e.preventDefault()
    addDoctorMutation.mutate(doctorForm)
  }

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-primary-600"></div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-md p-4">
        <p className="text-red-800">Error loading doctors. Please try again.</p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-900">Doctors</h1>
        <button 
          className="inline-flex items-center px-4 py-2 text-sm font-medium text-white transition-all duration-200 bg-emerald-600 border border-transparent rounded-lg shadow-sm hover:bg-emerald-700 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
          onClick={() => setShowAddModal(true)}
        >
          <Plus className="h-4 w-4 mr-2" />
          Add New Doctor
        </button>
      </div>

      <div className="bg-white shadow rounded-lg">
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-lg font-medium text-gray-900">Medical Staff</h2>
        </div>
        
        {!doctors || doctors.length === 0 ? (
          <div className="p-6 text-center text-gray-500">
            No doctors found.
          </div>
        ) : (
          <div className="divide-y divide-gray-200">
            {doctors.map((doctor) => (
              <div key={doctor.id} className="p-6">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-4">
                    <div className="flex-shrink-0">
                      <div className="h-12 w-12 rounded-full bg-primary-100 flex items-center justify-center">
                        <UserIcon className="h-6 w-6 text-primary-600" />
                      </div>
                    </div>
                    <div>
                      <h3 className="text-lg font-medium text-gray-900">
                        Dr. {doctor.first_name} {doctor.last_name}
                      </h3>
                      <div className="flex items-center space-x-4 mt-1">
                        <div className="flex items-center text-sm text-gray-500">
                          <MailIcon className="h-4 w-4 mr-1" />
                          {doctor.email}
                        </div>
                        <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                          doctor.is_active 
                            ? 'bg-green-100 text-green-800'
                            : 'bg-red-100 text-red-800'
                        }`}>
                          {doctor.is_active ? 'Active' : 'Inactive'}
                        </span>
                      </div>
                    </div>
                  </div>
                  
                  <div className="flex space-x-2">
                    <button className="bg-emerald-100 hover:bg-emerald-200 text-emerald-700 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors duration-200">
                      View Schedule
                    </button>
                    <button className="bg-green-100 hover:bg-green-200 text-green-700 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors duration-200">
                      Edit
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Add Doctor Modal */}
      <Modal
        isOpen={showAddModal}
        onClose={() => setShowAddModal(false)}
        title="Add New Doctor"
      >
        <form onSubmit={handleAddDoctor} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                First Name
              </label>
              <input
                type="text"
                required
                className="input"
                value={doctorForm.first_name}
                onChange={(e) => setDoctorForm({...doctorForm, first_name: e.target.value})}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Last Name
              </label>
              <input
                type="text"
                required
                className="input"
                value={doctorForm.last_name}
                onChange={(e) => setDoctorForm({...doctorForm, last_name: e.target.value})}
              />
            </div>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Email
            </label>
            <input
              type="email"
              required
              className="input"
              value={doctorForm.email}
              onChange={(e) => setDoctorForm({...doctorForm, email: e.target.value})}
            />
          </div>
          
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Phone
              </label>
              <input
                type="tel"
                className="input"
                value={doctorForm.phone}
                onChange={(e) => setDoctorForm({...doctorForm, phone: e.target.value})}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                License Number
              </label>
              <input
                type="text"
                className="input"
                value={doctorForm.license_number}
                onChange={(e) => setDoctorForm({...doctorForm, license_number: e.target.value})}
                placeholder="Medical license number"
              />
            </div>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Specialization
            </label>
            <select
              className="input"
              value={doctorForm.specialization}
              onChange={(e) => setDoctorForm({...doctorForm, specialization: e.target.value})}
              required
            >
              <option value="">Select Specialization</option>
              <option value="General Medicine">General Medicine</option>
              <option value="Cardiology">Cardiology</option>
              <option value="Neurology">Neurology</option>
              <option value="Orthopedics">Orthopedics</option>
              <option value="Pediatrics">Pediatrics</option>
              <option value="Surgery">Surgery</option>
              <option value="Dermatology">Dermatology</option>
              <option value="Psychiatry">Psychiatry</option>
              <option value="Radiology">Radiology</option>
              <option value="Emergency Medicine">Emergency Medicine</option>
            </select>
          </div>
          
          <div className="bg-blue-50 p-3 rounded-md">
            <p className="text-sm text-blue-800">
              <strong>Note:</strong> A temporary password "temp123" will be assigned. 
              The doctor should change it upon first login.
            </p>
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
              className="inline-flex items-center px-4 py-2 text-sm font-medium text-white transition-all duration-200 bg-emerald-600 border border-transparent rounded-lg shadow-sm hover:bg-emerald-700 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
              disabled={addDoctorMutation.isLoading}
            >
              {addDoctorMutation.isLoading && (
                <svg className="w-4 h-4 mr-2 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="m4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
              )}
              {addDoctorMutation.isLoading ? 'Adding...' : 'Add Doctor'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}

export default DoctorsPage
