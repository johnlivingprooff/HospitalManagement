import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from 'react-query'
import { AuthProvider } from './contexts/AuthContext'
import { ProtectedRoute } from './components/ProtectedRoute'
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import PatientsPage from './pages/PatientsPage'
import PatientDetailPage from './pages/PatientDetailPage'
import AppointmentsPage from './pages/AppointmentsPage'
import AppointmentDetailPage from './pages/AppointmentDetailPage'
import DoctorsPage from './pages/DoctorsPage'
import BillsPage from './pages/BillsPage'
import BillDetailPage from './pages/BillDetailPage'
import MedicalRecordsPage from './pages/MedicalRecordsPage'
import MedicalRecordDetailPage from './pages/MedicalRecordDetailPage'
import SettingsPage from './pages/SettingsPage'
import PharmacyPage from './pages/PharmacyPage'
import PrescriptionDetailPage from './pages/PrescriptionDetailPage'
import LabPage from './pages/LabPage'
import LabTestDetailPage from './pages/LabTestDetailPage'
import Layout from './components/Layout'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
})

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <Router>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route
              path="/"
              element={
                <ProtectedRoute>
                  <Layout />
                </ProtectedRoute>
              }
            >
              <Route index element={<DashboardPage />} />
              <Route path="patients" element={<PatientsPage />} />
              <Route path="patients/:id" element={<PatientDetailPage />} />
              <Route path="appointments" element={<AppointmentsPage />} />
              <Route path="appointments/:id" element={<AppointmentDetailPage />} />
              <Route path="doctors" element={<DoctorsPage />} />
              <Route path="bills" element={<BillsPage />} />
              <Route path="bills/:id" element={<BillDetailPage />} />
              <Route path="medical-records" element={<MedicalRecordsPage />} />
              <Route path="medical-records/:id" element={<MedicalRecordDetailPage />} />
              <Route path="pharmacy" element={<PharmacyPage />} />
              <Route path="pharmacy/:id" element={<PrescriptionDetailPage />} />
              <Route path="lab" element={<LabPage />} />
              <Route path="lab/:id" element={<LabTestDetailPage />} />
              <Route path="settings" element={<SettingsPage />} />
            </Route>
          </Routes>
        </Router>
      </AuthProvider>
    </QueryClientProvider>
  )
}

export default App
