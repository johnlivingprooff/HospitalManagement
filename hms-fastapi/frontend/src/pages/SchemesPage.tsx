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
        queryClient.invalidateQueries('schemes');
      },
      onError: () => setToast({ message: 'Failed to update scheme', type: 'error' }),
    }
  );

  const archiveMutation = useMutation(
    ({ id, active }: { id: number; active: boolean }) => api.put(`/api/schemes/${id}`, { active }),
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
  const active = (schemes || []).filter((s: any) => s.active).length;
  const inactive = (schemes || []).filter((s: any) => !s.active).length;
  const types = Array.from(new Set((schemes || []).map((s: any) => s.type))).length;

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
                        {scheme.active ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex space-x-2">
                        {canEdit ? (
                          editingId === scheme.id ? (
                            <>
                              <input
                                className="border px-1 py-0.5 rounded"
                                value={editForm.name}
                                onChange={e => setEditForm(f => ({ ...f, name: e.target.value }))}
                              />
                              <input
                                className="border px-1 py-0.5 rounded"
                                value={editForm.type}
                                onChange={e => setEditForm(f => ({ ...f, type: e.target.value }))}
                              />
                              <input
                                className="border px-1 py-0.5 rounded"
                                value={editForm.description}
                                onChange={e => setEditForm(f => ({ ...f, description: e.target.value }))}
                              />
                              <button
                                className="flex items-center justify-center p-2 text-green-700 transition-colors duration-200 bg-green-100 rounded-lg hover:bg-green-200"
                                title="Save"
                                onClick={() => updateMutation.mutate({ id: scheme.id, data: editForm })}
                                type="button"
                              >
                                {/* Save icon (checkmark) */}
                                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" /></svg>
                              </button>
                              <button
                                className="flex items-center justify-center p-2 text-gray-700 transition-colors duration-200 bg-gray-100 rounded-lg hover:bg-gray-200"
                                title="Cancel"
                                onClick={() => setEditingId(null)}
                                type="button"
                              >
                                {/* Cancel icon (X) */}
                                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" /></svg>
                              </button>
                            </>
                          ) : (
                            <>
                              <button
                                className="flex items-center justify-center p-2 text-yellow-700 transition-colors duration-200 bg-yellow-100 rounded-lg hover:bg-yellow-200"
                                title="Edit Scheme"
                                onClick={() => {
                                  setEditingId(scheme.id);
                                  setEditForm({ name: scheme.name, type: scheme.type, description: scheme.description });
                                }}
                                type="button"
                              >
                                {/* Edit icon */}
                                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.232 5.232l3.536 3.536M9 13l6-6m2 2l-6 6m-2 2h2v2h2v-2h2v-2h-2v-2h-2v2H9v2z" /></svg>
                              </button>
                              <button
                                className={`flex items-center justify-center p-2 transition-colors duration-200 rounded-lg ${scheme.active ? 'text-red-700 bg-red-100 hover:bg-red-200' : 'text-green-700 bg-green-100 hover:bg-green-200'}`}
                                title={scheme.active ? 'Archive Scheme' : 'Activate Scheme'}
                                onClick={() => archiveMutation.mutate({ id: scheme.id, active: !scheme.active })}
                                type="button"
                              >
                                {scheme.active ? (
                                  // Archive icon (UserX)
                                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" /></svg>
                                ) : (
                                  // Activate icon (UserCheck)
                                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" /></svg>
                                )}
                              </button>
                            </>
                          )
                        ) : (
                          <span className="text-gray-400">View Only</span>
                        )}
                      </div>
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
