import React from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery } from 'react-query'
import { ArrowLeft, FileText, User, Stethoscope, Calendar, Printer } from 'lucide-react'
import api from '../lib/api'

interface MedicalRecord {
  id: number
  patient_id: number
  doctor_id: number
  diagnosis: string
  treatment: string
  notes?: string
  visit_date: string
  created_at: string
  patient: {
    first_name: string
    last_name: string
    email: string
    date_of_birth: string
  }
  doctor: {
    first_name: string
    last_name: string
    specialization?: string
  }
}

const MedicalRecordDetailPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()

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

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
      </div>
    )
  }

  if (error || !record) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-md p-4">
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
          className="bg-gray-100 hover:bg-gray-200 p-2 rounded-lg transition-colors duration-200"
        >
          <ArrowLeft className="h-5 w-5" />
        </button>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            Medical Record #{record.id}
          </h1>
          <p className="text-gray-600">
            {record.patient.first_name} {record.patient.last_name}
          </p>
        </div>
      </div>

      {/* Medical Record Information Cards */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Patient Information */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
            <User className="h-5 w-5 mr-2 text-indigo-600" />
            Patient Information
          </h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Patient Name</label>
              <p className="text-gray-900">
                {record.patient.first_name} {record.patient.last_name}
              </p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Email</label>
              <p className="text-gray-900">{record.patient.email}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Date of Birth</label>
              <p className="text-gray-900">
                {new Date(record.patient.date_of_birth).toLocaleDateString()}
              </p>
            </div>
          </div>
        </div>

        {/* Doctor & Visit Information */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
            <Stethoscope className="h-5 w-5 mr-2 text-indigo-600" />
            Doctor & Visit Information
          </h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Doctor</label>
              <p className="text-gray-900">
                Dr. {record.doctor.first_name} {record.doctor.last_name}
              </p>
              {record.doctor.specialization && (
                <p className="text-sm text-gray-500">{record.doctor.specialization}</p>
              )}
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Visit Date</label>
              <p className="text-gray-900 flex items-center">
                <Calendar className="h-4 w-4 mr-2 text-gray-500" />
                {new Date(record.visit_date).toLocaleDateString()}
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
      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
          <FileText className="h-5 w-5 mr-2 text-indigo-600" />
          Medical Details
        </h2>
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Diagnosis</label>
            <div className="bg-gray-50 p-4 rounded-lg">
              <p className="text-gray-900 whitespace-pre-wrap">{record.diagnosis}</p>
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Treatment</label>
            <div className="bg-gray-50 p-4 rounded-lg">
              <p className="text-gray-900 whitespace-pre-wrap">{record.treatment}</p>
            </div>
          </div>
        </div>
        
        {record.notes && (
          <div className="mt-6">
            <label className="block text-sm font-medium text-gray-700 mb-2">Additional Notes</label>
            <div className="bg-gray-50 p-4 rounded-lg">
              <p className="text-gray-900 whitespace-pre-wrap">{record.notes}</p>
            </div>
          </div>
        )}
      </div>

      {/* Action Buttons */}
      <div className="flex justify-end space-x-4">
        <button
          onClick={() => window.print()}
          className="bg-gray-600 hover:bg-gray-700 text-white px-4 py-2 rounded-lg transition-colors duration-200 flex items-center space-x-2"
        >
          <Printer className="h-4 w-4" />
          <span>Print Record</span>
        </button>
        <button
          onClick={() => navigate(`/medical-records/edit/${record.id}`)}
          className="bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg transition-colors duration-200"
        >
          Edit Record
        </button>
      </div>
    </div>
  )
}

export default MedicalRecordDetailPage
