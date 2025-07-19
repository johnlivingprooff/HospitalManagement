// import React from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery } from 'react-query'
import { ArrowLeft, User, Phone, Mail, Calendar, MapPin } from 'lucide-react'
import api from '../lib/api'

interface Patient {
  id: number
  first_name: string
  last_name: string
  email: string
  phone: string
  date_of_birth: string
  gender: string
  address: string
  is_active: boolean
  created_at: string
}

const PatientDetailPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()

  const { data: patient, isLoading, error } = useQuery<Patient>(
    ['patient', id],
    async () => {
      const response = await api.get(`/api/patients/${id}`)
      return response.data
    },
    {
      enabled: !!id
    }
  )

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="w-12 h-12 border-b-2 border-green-600 rounded-full animate-spin"></div>
      </div>
    )
  }

  if (error || !patient) {
    return (
      <div className="p-4 border border-red-200 rounded-md bg-red-50">
        <p className="text-red-800">Failed to load patient details</p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center space-x-4">
        <button
          onClick={() => navigate('/patients')}
          className="p-2 transition-colors duration-200 bg-gray-100 rounded-lg hover:bg-gray-200"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            {patient.first_name} {patient.last_name}
          </h1>
          <p className="text-gray-600">Patient Details</p>
        </div>
      </div>

      {/* Patient Information Cards */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Personal Information */}
        <div className="p-6 bg-white rounded-lg shadow">
          <h2 className="flex items-center mb-4 text-lg font-semibold text-gray-900">
            <User className="w-5 h-5 mr-2 text-green-600" />
            Personal Information
          </h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Full Name</label>
              <p className="text-gray-900">{patient.first_name} {patient.last_name}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Date of Birth</label>
              <p className="flex items-center text-gray-900">
                <Calendar className="w-4 h-4 mr-2 text-gray-500" />
                {new Date(patient.date_of_birth).toLocaleDateString()}
              </p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Gender</label>
              <p className="text-gray-900 capitalize">{patient.gender}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Status</label>
              <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                patient.is_active 
                  ? 'bg-green-100 text-green-800' 
                  : 'bg-red-100 text-red-800'
              }`}>
                {patient.is_active ? 'Active' : 'Inactive'}
              </span>
            </div>
          </div>
        </div>

        {/* Contact Information */}
        <div className="p-6 bg-white rounded-lg shadow">
          <h2 className="flex items-center mb-4 text-lg font-semibold text-gray-900">
            <Phone className="w-5 h-5 mr-2 text-green-600" />
            Contact Information
          </h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Email</label>
              <p className="flex items-center text-gray-900">
                <Mail className="w-4 h-4 mr-2 text-gray-500" />
                {patient.email}
              </p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Phone</label>
              <p className="flex items-center text-gray-900">
                <Phone className="w-4 h-4 mr-2 text-gray-500" />
                {patient.phone || 'Not provided'}
              </p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Address</label>
              <p className="flex items-center text-gray-900">
                <MapPin className="w-4 h-4 mr-2 text-gray-500" />
                {patient.address || 'Not provided'}
              </p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Registered</label>
              <p className="text-gray-900">
                {new Date(patient.created_at).toLocaleDateString()}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="flex justify-end space-x-4">
        <button
          onClick={() => navigate(`/patients/edit/${patient.id}`)}
          className="px-4 py-2 text-white transition-colors duration-200 bg-green-600 rounded-lg hover:bg-green-700"
        >
          Edit Patient
        </button>
      </div>
    </div>
  )
}

export default PatientDetailPage
