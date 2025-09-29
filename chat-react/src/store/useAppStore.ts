import { create } from 'zustand'
import { persist } from 'zustand/middleware';

interface AppState {
  isBackButton: boolean;
  headerTitle: string;
  setHeaderInfo: (isBackButton: boolean, title: string) => void;
  wsSessionId: string;
  setWsSessionId: (wsSessionId: string) => void;
}

export const useAppStore = create<AppState>()(
  persist(
    (set) => ({
      isBackButton: false,
      headerTitle: '기본 헤더',
      setHeaderInfo: (isBackButton, title) => set({ isBackButton, headerTitle: title }),
      wsSessionId: '',
      setWsSessionId: (wsSessionId) => set({ wsSessionId })
    }),
    {
      name: 'app-storage',
      partialize: (state) => ({
        isBackButton: state.isBackButton,
        headerTitle: state.headerTitle,
      }),
    }
  )
);