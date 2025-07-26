import React from 'react';
import { useParams, Link } from 'react-router-dom';
import { useQuery } from 'react-query';
import api from '../lib/api';
import SchemeLoading from '../components/loading/SchemeLoading';
import Toast from '../components/Toast';

const SchemeDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const {
    data: scheme,
    isLoading,
    error,
  } = useQuery(['scheme', id], () => api.get(`/api/schemes/${id}`).then(res => res.data), {
    enabled: !!id,
  });

  if (isLoading) return <SchemeLoading />;
  if (error) return <Toast message="Failed to load scheme details" type="error" onClose={() => {}} />;
  if (!scheme) return <div className="p-8">Scheme not found.</div>;

  return (
    <div className="max-w-2xl mx-auto p-6 bg-white rounded shadow">
      <h2 className="text-2xl font-bold mb-4">Scheme Details</h2>
      <div className="mb-2"><span className="font-semibold">Name:</span> {scheme.name}</div>
      <div className="mb-2"><span className="font-semibold">Type:</span> {scheme.type}</div>
      <div className="mb-2"><span className="font-semibold">Description:</span> {scheme.description}</div>
      <div className="mb-2"><span className="font-semibold">Status:</span> {scheme.active ? 'Active' : 'Inactive'}</div>
      <div className="mt-6">
        <Link to="/schemes" className="text-blue-600 hover:underline">Back to Schemes</Link>
      </div>
    </div>
  );
};

export default SchemeDetailPage;
