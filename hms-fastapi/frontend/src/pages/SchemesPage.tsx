import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import api from '../lib/api';
import { Skeleton } from '../components/ui/Skeleton';
import Toast from '../components/Toast';
import { useRole } from '../contexts/RoleContext';

const SchemesPage: React.FC = () => {
  const { getUserRole } = useRole();
  const queryClient = useQueryClient();
  const [toast, setToast] = useState<{ message: string; type: 'error' | 'success' } | null>(null);
  const [form, setForm] = useState({ name: '', type: '', description: '' });
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editForm, setEditForm] = useState({ name: '', type: '', description: '' });
  const [showModal, setShowModal] = useState(false);
  const [isEditMode, setIsEditMode] = useState(false);

  const { data: schemes, isLoading } = useQuery('schemes', () => api.get('/api/schemes').then(res => res.data));

  const createMutation = useMutation(
    (data: typeof form) => api.post('/api/schemes', data),
    {
      onSuccess: () => {
        setToast({ message: 'Scheme added', type: 'success' });
        setForm({ name: '', type: '', description: '' });
        queryClient.invalidateQueries('schemes');
      },
      onError: () => setToast({ message: 'Failed to add scheme', type: 'error' }),
    }
  );

  const updateMutation = useMutation(
    ({ id, data }: { id: number; data: typeof editForm }) => api.put(`/api/schemes/${id}`, data),
    {
      onSuccess: () => {
        setToast({ message: 'Scheme updated', type: 'success' });
        setEditingId(null);
        setShowModal(false);
        queryClient.invalidateQueries('schemes');
      },
      onError: () => setToast({ message: 'Failed to update scheme', type: 'error' }),
    }
  );

  const archiveMutation = useMutation(
    ({ id, is_active }: { id: number; is_active: boolean }) => api.put(`/api/schemes/${id}`, { is_active }),
    {
      onSuccess: () => {
        setToast({ message: 'Scheme status updated', type: 'success' });
        queryClient.invalidateQueries('schemes');
      },
      onError: () => setToast({ message: 'Failed to update status', type: 'error' }),
    }
  );

  const canEdit = ['admin', 'doctor', 'nurse'].includes(getUserRole());

  if (isLoading) {
    return (
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-bold text-gray-900">Schemes</h1>
          {canEdit && (
            <button
              onClick={() => {}}
              className="bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2.5 px-6 rounded-lg shadow-lg hover:shadow-xl transition-all duration-200 transform hover:scale-105 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
              disabled
            >
              New Scheme
            </button>
          )}
        </div>
        <div className="grid grid-cols-1 gap-4 md:grid-cols-4">
          {Array.from({ length: 4 }).map((_, i) => (
            <Skeleton key={i} variant="card" height={90} />
          ))}
        </div>
        <div className="bg-white rounded-lg shadow">
          <div className="flex flex-col gap-4 p-4 md:flex-row">
            <Skeleton variant="input" width={220} height={38} />
            <Skeleton variant="input" width={180} height={38} />
          </div>
        </div>
        <div className="overflow-hidden bg-white rounded-lg shadow">
          <Skeleton variant="text" width={200} height={28} className="mx-6 my-4" />
          <div className="px-6 pb-6">
            {Array.from({ length: 5 }).map((_, i) => (
              <div key={i} className="flex items-center gap-4 mb-4">
                <Skeleton variant="text" width={120} height={24} />
                <Skeleton variant="text" width={80} height={24} />
                <Skeleton variant="text" width={200} height={24} />
                <Skeleton variant="badge" width={60} height={24} />
                <Skeleton variant="button" width={90} height={36} />
              </div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  // Summary card helpers
  const total = (schemes || []).length;
  const active = (schemes || []).filter((s: any) => s.is_active).length;
  const inactive = (schemes || []).filter((s: any) => !s.is_active).length;
  const types = Array.from(new Set((schemes || []).map((s: any) => s.type))).length;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Schemes</h1>
        {canEdit && (
          <button
            onClick={() => {
              setIsEditMode(false);
              setForm({ name: '', type: '', description: '' });
              setShowModal(true);
            }}
            className="bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2.5 px-6 rounded-lg shadow-lg hover:shadow-xl transition-all duration-200 transform hover:scale-105 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
          >
            New Scheme
          </button>
        )}
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 gap-4 md:grid-cols-4">
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center">
            <span className="flex items-center justify-center w-8 h-8 text-lg font-bold text-blue-600 bg-blue-100 rounded-full">{total}</span>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Total</p>
              <p className="text-2xl font-bold text-gray-900">Schemes</p>
            </div>
          </div>
        </div>
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center">
            <span className="flex items-center justify-center w-8 h-8 text-lg font-bold text-green-600 bg-green-100 rounded-full">{active}</span>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Active</p>
              <p className="text-2xl font-bold text-gray-900">Schemes</p>
            </div>
          </div>
        </div>
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center">
            <span className="flex items-center justify-center w-8 h-8 text-lg font-bold text-gray-600 bg-gray-100 rounded-full">{inactive}</span>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Inactive</p>
              <p className="text-2xl font-bold text-gray-900">Schemes</p>
            </div>
          </div>
        </div>
        <div className="p-6 bg-white rounded-lg shadow">
          <div className="flex items-center">
            <span className="flex items-center justify-center w-8 h-8 text-lg font-bold text-purple-600 bg-purple-100 rounded-full">{types}</span>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Types</p>
              <p className="text-2xl font-bold text-gray-900">of Scheme</p>
            </div>
          </div>
        </div>
      </div>

      {/* Filters (future: add search/filter here) */}
      {/* <div className="flex flex-col gap-4 p-4 mb-6 bg-white rounded-lg shadow md:flex-row">
        <input className="w-full input md:w-1/2" placeholder="Search schemes..." />
        <select className="w-full input md:w-1/4">
          <option>All Status</option>
          <option>Active</option>
          <option>Inactive</option>
        </select>
      </div> */}

      {/* Table */}
      <div className="overflow-hidden bg-white rounded-lg shadow">
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-lg font-medium text-gray-900">Schemes ({schemes.length})</h2>
        </div>
        {schemes.length === 0 ? (
          <div className="p-6 text-center text-gray-500">No schemes found.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">Name</th>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">Type</th>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">Description</th>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">Status</th>
                  <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">Actions</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {schemes.map((scheme: any) => (
                  <tr key={scheme.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <Link to={`/schemes/${scheme.id}`} className="font-medium text-blue-600 hover:underline">{scheme.name}</Link>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">{scheme.type}</td>
                    <td className="px-6 py-4 whitespace-nowrap">{scheme.description}</td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex items-center px-2 py-1 text-xs font-semibold rounded-full ${scheme.active ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'}`}>
                        {scheme.is_active ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex space-x-2">
                        {canEdit ? (
                          <>
                            <button
                              className="flex items-center justify-center p-2 text-yellow-700 transition-colors duration-200 bg-yellow-100 rounded-lg hover:bg-yellow-200"
                              title="Edit Scheme"
                              onClick={() => {
                                setEditingId(scheme.id);
                                setEditForm({ name: scheme.name, type: scheme.type, description: scheme.description });
                                setIsEditMode(true);
                                setShowModal(true);
                              }}
                              type="button"
                            >
                              {/* Edit icon */}
                              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.232 5.232l3.536 3.536M9 13l6-6m2 2l-6 6m-2 2h2v2h2v-2h2v-2h-2v-2h-2v2H9v2z" /></svg>
                            </button>
                            <button
                              className={`flex items-center justify-center p-2 transition-colors duration-200 rounded-lg ${scheme.active ? 'text-red-700 bg-red-100 hover:bg-red-200' : 'text-green-700 bg-green-100 hover:bg-green-200'}`}
                              title={scheme.is_active ? 'Archive Scheme' : 'Activate Scheme'}
                              onClick={() => archiveMutation.mutate({ id: scheme.id, is_active: !scheme.is_active })}
                              type="button"
                            >
                              {scheme.is_active ? (
                                // Archive icon (Trash)
                                <svg className="w-4 h-4 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6M1 7h22M8 7V5a2 2 0 012-2h4a2 2 0 012 2v2" />
                                </svg>
                              ) : (
                                // Activate icon (UserCheck)
                                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" /></svg>
                              )}
                            </button>
                          </>
                        ) : (
                          <span className="text-gray-400">View Only</span>
                        )}
                      </div>
      {/* Add/Edit Scheme Modal */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-30">
          <div className="relative w-full max-w-md p-6 bg-white rounded-lg shadow-lg">
            <button
              className="absolute text-gray-400 top-2 right-2 hover:text-gray-600"
              onClick={() => {
                setShowModal(false);
                setEditingId(null);
                setIsEditMode(false);
              }}
              title="Close"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" /></svg>
            </button>
            <h2 className="mb-4 text-xl font-semibold text-gray-900">{isEditMode ? 'Edit Scheme' : 'Add New Scheme'}</h2>
            <form
              onSubmit={e => {
                e.preventDefault();
                if (isEditMode && editingId) {
                  updateMutation.mutate({ id: editingId, data: editForm });
                } else if (!isEditMode) {
                  createMutation.mutate(form);
                  setShowModal(false);
                }
              }}
              className="space-y-4"
            >
              <div>
                <label className="block mb-1 text-sm font-medium text-gray-700">Name</label>
                <input
                  className="w-full input"
                  value={isEditMode ? editForm.name : form.name}
                  onChange={e => isEditMode
                    ? setEditForm(f => ({ ...f, name: e.target.value }))
                    : setForm(f => ({ ...f, name: e.target.value }))}
                  required
                />
              </div>
              <div>
                <label className="block mb-1 text-sm font-medium text-gray-700">Type</label>
                <input
                  className="w-full input"
                  value={isEditMode ? editForm.type : form.type}
                  onChange={e => isEditMode
                    ? setEditForm(f => ({ ...f, type: e.target.value }))
                    : setForm(f => ({ ...f, type: e.target.value }))}
                  required
                />
              </div>
              <div>
                <label className="block mb-1 text-sm font-medium text-gray-700">Description</label>
                <textarea
                  className="w-full input"
                  value={isEditMode ? editForm.description : form.description}
                  onChange={e => isEditMode
                    ? setEditForm(f => ({ ...f, description: e.target.value }))
                    : setForm(f => ({ ...f, description: e.target.value }))}
                  rows={3}
                />
              </div>
              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200"
                  onClick={() => {
                    setShowModal(false);
                    setEditingId(null);
                    setIsEditMode(false);
                  }}
                >Cancel</button>
                <button
                  type="submit"
                  className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700"
                  disabled={isEditMode ? updateMutation.isLoading : createMutation.isLoading}
                >{isEditMode ? (updateMutation.isLoading ? 'Saving...' : 'Save') : (createMutation.isLoading ? 'Adding...' : 'Add')}</button>
              </div>
            </form>
          </div>
        </div>
      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
      {toast && <Toast message={toast.message} type={toast.type} onClose={() => setToast(null)} />}
    </div>
  );
};

export default SchemesPage;
