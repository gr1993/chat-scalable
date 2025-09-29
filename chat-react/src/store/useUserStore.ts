import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface UserState {
  isLoggedIn: boolean;
  id: string;
  login: (id: string) => void;
  logout: () => void;
}

export const useUserStore = create<UserState>()(
  persist(
    (set) => ({
      isLoggedIn: false,
      id: '',
      login: (id) => set({ isLoggedIn: true, id }),
      logout: () => set({ isLoggedIn: false, id: '' }),
    }),
    {
      name: 'user-storage',
      partialize: (state) => ({
        isLoggedIn: state.isLoggedIn,
        id: state.id,
      }),
    }
  )
);