export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T | null;
}

export type ChatRoomInfo = {
  roomId: number;
  roomName: string;
  lastId: number | null;
  lastMessage: string | null;
  lastDt: string | null;
};

export type ChatMessageInfo = {
  messageId: string;
  senderId?: string;
  message: string;
  sendDt: string;
  type: 'message' | 'system';
  position?: 'left' | 'right';
};