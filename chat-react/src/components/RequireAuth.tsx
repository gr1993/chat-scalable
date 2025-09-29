import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useUserStore } from '@/store/useUserStore';

interface Props {
  children: React.ReactNode;
  redirectTo?: string;
}

const RequireAuth: React.FC<Props> = ({ children, redirectTo = '/login' }) => {
  const { isLoggedIn } = useUserStore();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isLoggedIn) {
      navigate(redirectTo);
    }
  }, [isLoggedIn, navigate, redirectTo]);

  if (!isLoggedIn) return null;

  return <>{children}</>;
};

export default RequireAuth;