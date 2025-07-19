export interface User {
  id: number
  email: string
  first_name: string
  last_name: string
  role: string
  is_active: boolean
  phone?: string
  specialization?: string
  license_number?: string
  created_at: string
  updated_at: string
}

export interface Patient {
  id: number
  first_name: string
  last_name: string
  email?: string
  phone?: string
  date_of_birth?: string
  gender?: string
  address?: string
  emergency_contact?: string
  emergency_phone?: string
  medical_history?: string
  allergies?: string
  current_medications?: string
  insurance_info?: string
  is_active: boolean
  created_by_id?: number
  created_at: string
  updated_at: string
}

export interface Appointment {
  id: number
  patient_id: number
  doctor_id: number
  appointment_date: string
  duration_minutes: number
  status: string
  appointment_type: string
  notes?: string
  symptoms?: string
  diagnosis?: string
  treatment_plan?: string
  created_at: string
  updated_at: string
}

export interface AppointmentWithRelations extends Appointment {
  patient?: Patient
  doctor?: User
}

export interface LoginCredentials {
  email: string
  password: string
}

export interface AuthResponse {
  access_token: string
  token_type: string
  user: User
}

export interface PatientCreate {
  first_name: string
  last_name: string
  email?: string
  phone?: string
  date_of_birth?: string
  gender?: string
  address?: string
  emergency_contact?: string
  emergency_phone?: string
  medical_history?: string
  allergies?: string
  current_medications?: string
  insurance_info?: string
}

export interface AppointmentCreate {
  patient_id: number
  doctor_id: number
  appointment_date: string
  duration_minutes?: number
  appointment_type: string
  notes?: string
  symptoms?: string
}

export interface MedicalRecord {
  id: number
  patient_id: number
  doctor_id: number
  appointment_id?: number
  record_type: string
  title: string
  description: string
  diagnosis?: string
  treatment?: string
  medications?: string
  lab_results?: string
  notes?: string
  visit_date?: string
  created_at: string
  updated_at?: string
  patient?: {
    first_name: string
    last_name: string
    email?: string
    date_of_birth?: string
  }
  doctor?: {
    first_name: string
    last_name: string
    specialization?: string
  }
}
