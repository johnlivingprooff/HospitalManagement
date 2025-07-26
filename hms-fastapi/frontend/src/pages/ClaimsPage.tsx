import React, { useState, Suspense, useRef } from 'react'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import api, { updateClaim } from '../lib/api'
import Modal from '../components/Modal'
import Toast from '../components/Toast'
const LoadingClaimsPage = React.lazy(() => import('../components/loading/ClaimsLoadingStates').then(m => ({ default: m.LoadingClaimsPage })))

interface Claim {
  id: number
  patient_id: number
  scheme: string
  amount: number
  status: string
  submitted_at: string
  processed_at?: string
  outcome?: string
  description?: string
}

const ClaimsPage = () => {
  const queryClient = useQueryClient()
  const [showModal, setShowModal] = useState(false)
  const [form, setForm] = useState({ patient_id: '', scheme: '', amount: '', description: '' })
  const [patientSearch, setPatientSearch] = useState('')
  const [showPatientDropdown, setShowPatientDropdown] = useState(false)
  const patientDropdownRef = useRef<HTMLDivElement>(null)
  const [schemeSearch, setSchemeSearch] = useState('')
  const [showSchemeDropdown, setShowSchemeDropdown] = useState(false)
  const schemeDropdownRef = useRef<HTMLDivElement>(null)
  // Fetch patients for select
  // Fetch all patients on modal open
  const { data: patients, isLoading: loadingPatients } = useQuery(
    'patients',
    () => api.get('/api/patients', { params: { limit: 1000 } }).then(res => res.data),
    { enabled: showModal }
  )

  // Fetch schemes for select
  const { data: schemes, isLoading: loadingSchemes } = useQuery('schemes', () =>
    api.get('/api/schemes').then(res => res.data),
    { enabled: showModal }
  )
  const [toast, setToast] = useState<{ message: string; type?: 'success'|'error' }>({ message: '' })

  // Fetch claims with patient info
  const { data: claims, isLoading, error } = useQuery<Claim[]>(
    'claims',
    () => api.get('/api/claims').then(res => res.data)
  )

  // Map patientId to patient for fast lookup
  const patientMap = React.useMemo(() => {
    const map: Record<string, any> = {}
    patients?.forEach((p: any) => { map[p.id] = p })
    return map
  }, [patients])

  const createClaim = useMutation(
    (data: any) => api.post('/api/claims', data),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('claims')
        setShowModal(false)
        setForm({ patient_id: '', scheme: '', amount: '', description: '' })
        setToast({ message: 'Claim submitted successfully', type: 'success' })
      },
      onError: () => setToast({ message: 'Failed to submit claim', type: 'error' })
    }
  )

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.patient_id || !form.scheme || !form.amount) {
      setToast({ message: 'All fields are required', type: 'error' })
      return
    }
    createClaim.mutate({
      patient_id: Number(form.patient_id),
      scheme: form.scheme,
      amount: Number(form.amount),
      description: form.description
    })
  }

  if (isLoading) {
    return (
      <Suspense fallback={<div>Loading...</div>}>
        <LoadingClaimsPage />
      </Suspense>
    )
  }

  if (error) {
    return (
      <div className="p-4 border border-red-200 rounded-md bg-red-50">
        <p className="text-red-800">Error loading claims. Please try again.</p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between gap-4 px-2 py-4 sm:px-0">
        <h1 className="text-2xl font-bold">Claims Management</h1>
        <button
          className="inline-flex items-center gap-2 px-5 py-2 font-semibold text-white transition bg-blue-600 rounded-md shadow hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-400 focus:ring-offset-2"
          onClick={() => setShowModal(true)}
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" /></svg>
          Submit New Claim
        </button>
      </div>
      <div className="p-0 overflow-x-auto bg-white rounded-lg shadow sm:p-4">
        <table className="min-w-full text-sm divide-y divide-gray-200">
          <thead>
            <tr className="bg-gray-50">
              <th className="px-4 py-3 font-semibold text-left text-gray-700">ID</th>
              <th className="px-4 py-3 font-semibold text-left text-gray-700">Patient ID</th>
              <th className="px-4 py-3 font-semibold text-left text-gray-700">Scheme</th>
              <th className="px-4 py-3 font-semibold text-left text-gray-700">Amount</th>
              <th className="px-4 py-3 font-semibold text-left text-gray-700">Status</th>
              <th className="px-4 py-3 font-semibold text-left text-gray-700">Submitted</th>
              <th className="px-4 py-3 font-semibold text-left text-gray-700">Outcome</th>
            </tr>
          </thead>
          <tbody>
            {claims?.map(claim => (
              <tr key={claim.id} className="transition even:bg-gray-50 hover:bg-blue-50">
                <td className="px-4 py-2 whitespace-nowrap">{claim.id}</td>
                <td className="px-4 py-2 whitespace-nowrap">
                  {patientMap[claim.patient_id]?.first_name
                    ? `${patientMap[claim.patient_id].first_name} ${patientMap[claim.patient_id].last_name}`
                    : claim.patient_id}
                </td>
                <td className="px-4 py-2 whitespace-nowrap">{claim.scheme}</td>
                <td className="px-4 py-2 whitespace-nowrap">{claim.amount}</td>
                <td className="px-4 py-2 whitespace-nowrap">
                  {claim.status}
                  {['pending', 'processing'].includes(claim.status) && (
                    <select
                      className="ml-2 input"
                      value={claim.status}
                      onChange={async e => {
                        try {
                          await updateClaim(claim.id, { status: e.target.value })
                          queryClient.invalidateQueries('claims')
                          setToast({ message: 'Status updated', type: 'success' })
                        } catch {
                          setToast({ message: 'Failed to update status', type: 'error' })
                        }
                      }}
                    >
                      <option value="pending">Pending</option>
                      <option value="processing">Processing</option>
                      <option value="approved">Approved</option>
                      <option value="rejected">Rejected</option>
                      <option value="paid">Paid</option>
                    </select>
                  )}
                </td>
                <td className="px-4 py-2 whitespace-nowrap">{new Date(claim.submitted_at).toLocaleString()}</td>
                <td className="px-4 py-2 whitespace-nowrap">{claim.outcome || '-'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <Modal isOpen={showModal} onClose={() => setShowModal(false)} title="Submit Claim">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label>Patient</label>
            <div className="relative" ref={patientDropdownRef}>
              <div
                className="flex items-center w-full cursor-pointer input"
                tabIndex={0}
                onClick={() => setShowPatientDropdown(true)}
              >
                {form.patient_id && patients?.find((p: any) => String(p.id) === form.patient_id)
                  ? `${patients.find((p: any) => String(p.id) === form.patient_id).first_name} ${patients.find((p: any) => String(p.id) === form.patient_id).last_name}`
                  : 'Select patient'}
              </div>
              {showPatientDropdown && (
                <div
                  className="absolute z-10 w-full mt-1 overflow-y-auto bg-white border rounded shadow max-h-60"
                  onMouseDown={e => e.preventDefault()}
                >
                  <input
                    className="w-full px-3 py-2 border-b outline-none"
                    placeholder="Search patient..."
                    value={patientSearch}
                    onChange={e => setPatientSearch(e.target.value)}
                    autoFocus
                    onBlur={() => setTimeout(() => setShowPatientDropdown(false), 150)}
                  />
                  {loadingPatients ? (
                    <div className="p-2 text-sm text-center text-gray-400">Loading...</div>
                  ) : (
                    patients?.filter((p: any) =>
                      !patientSearch ||
                      `${p.first_name} ${p.last_name}`.toLowerCase().includes(patientSearch.toLowerCase()) ||
                      (p.email || '').toLowerCase().includes(patientSearch.toLowerCase())
                    ).map((p: any) => (
                      <div
                        key={p.id}
                        className={`px-3 py-2 hover:bg-blue-50 cursor-pointer ${form.patient_id === String(p.id) ? 'bg-blue-100 font-semibold' : ''}`}
                        onMouseDown={e => {
                          e.preventDefault();
                          setForm(f => ({ ...f, patient_id: String(p.id) }))
                          setShowPatientDropdown(false)
                          setPatientSearch('')
                        }}
                      >
                        {p.first_name} {p.last_name} <span className="text-xs text-gray-500">({p.email || p.id})</span>
                      </div>
                    ))
                  )}
                </div>
              )}
            </div>
          </div>
          <div>
            <label>Scheme</label>
            <div className="relative" ref={schemeDropdownRef}>
              <div
                className="flex items-center w-full cursor-pointer input"
                tabIndex={0}
                onClick={() => setShowSchemeDropdown(true)}
              >
                {form.scheme || 'Select scheme'}
              </div>
              {showSchemeDropdown && (
                <div
                  className="absolute z-10 w-full mt-1 overflow-y-auto bg-white border rounded shadow max-h-60"
                  onMouseDown={e => e.preventDefault()}
                >
                  <input
                    className="w-full px-3 py-2 border-b outline-none"
                    placeholder="Search scheme..."
                    value={schemeSearch}
                    onChange={e => setSchemeSearch(e.target.value)}
                    autoFocus
                    onBlur={() => setTimeout(() => setShowSchemeDropdown(false), 150)}
                  />
                  {loadingSchemes ? (
                    <div className="p-2 text-sm text-center text-gray-400">Loading...</div>
                  ) : (
                    schemes?.filter((s: string) =>
                      !schemeSearch || s.toLowerCase().includes(schemeSearch.toLowerCase())
                    ).map((s: string) => (
                      <div
                        key={s}
                        className={`px-3 py-2 hover:bg-blue-50 cursor-pointer ${form.scheme === s ? 'bg-blue-100 font-semibold' : ''}`}
                        onMouseDown={e => {
                          e.preventDefault();
                          setForm(f => ({ ...f, scheme: s }))
                          setShowSchemeDropdown(false)
                          setSchemeSearch('')
                        }}
                      >
                        {s}
                      </div>
                    ))
                  )}
                </div>
              )}
            </div>
          </div>
          <div>
            <label>Amount</label>
            <input type="number" className="input" value={form.amount} onChange={e => setForm(f => ({ ...f, amount: e.target.value }))} required />
          </div>
          <div>
            <label>Description</label>
            <textarea className="input" value={form.description} onChange={e => setForm(f => ({ ...f, description: e.target.value }))} />
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <button type="button" className="px-5 py-2 btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
            <button type="submit" className="px-5 py-2 btn-primary" disabled={createClaim.isLoading}>
              {createClaim.isLoading ? 'Submitting...' : 'Submit'}
            </button>
          </div>
        </form>
      </Modal>
      {toast.message && (
        <Toast message={toast.message} type={toast.type} onClose={() => setToast({ message: '' })} />
      )}
    </div>
  )
}

export default ClaimsPage
