import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface ChatState {
  currentRoom: {
    id: number;
    name: string;
  } | null;
  setCurrentRoom: (room: ChatState['currentRoom']) => void;
}

export const useChatStore = create<ChatState>()(
  persist(
    (set) => ({
      currentRoom: null,
      setCurrentRoom: (room) => set({ currentRoom: room }),
    }),
    {
      name: 'chat-storage',
      partialize: (state) => ({
        currentRoom: state.currentRoom,
      }),
    }
  )
);