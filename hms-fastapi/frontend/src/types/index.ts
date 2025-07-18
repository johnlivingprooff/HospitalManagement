export interface User {
  id: number
  email: string
  first_name: string
  last_name: string
  role: string
  is_active: boolean
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
