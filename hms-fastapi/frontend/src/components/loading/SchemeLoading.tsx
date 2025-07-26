import React from 'react';
import { Skeleton } from '../ui/Skeleton';

const SchemeLoading: React.FC = () => (
  <div className="flex flex-col items-center justify-center min-h-[60vh] w-full">
    <div className="w-full max-w-4xl">
      <Skeleton variant="text" width={180} height={32} className="mb-8 mx-auto" />
      <div className="mb-6">
        <Skeleton variant="input" width={220} height={38} className="mb-2" />
        <Skeleton variant="input" width={220} height={38} className="mb-2" />
        <Skeleton variant="input" width={320} height={38} />
      </div>
      <div>
        {Array.from({ length: 5 }).map((_, i) => (
          <div key={i} className="flex gap-4 mb-4 items-center">
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

export default SchemeLoading;
