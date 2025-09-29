import axios from './axiosInstance';
import type { AxiosResponse } from 'axios';
import type { ApiResponse, ChatRoomInfo } from './types';

export const getRoomList = async (): Promise<ApiResponse<ChatRoomInfo[]>> => {
  const res: AxiosResponse<ApiResponse<ChatRoomInfo[]>> = await axios.get<ApiResponse<ChatRoomInfo[]>>(`/api/room`);
  return res.data;
};

export const createRoom = async (name: string): Promise<ApiResponse<null>> => {
  const form = new URLSearchParams();
  form.append('name', name);

  const res: AxiosResponse<ApiResponse<null>> = await axios.post<ApiResponse<null>>(
    `/api/room`, 
    form,
    {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
    }
  );
  return res.data;
};

export const enterRoom = async (sessionId: string, roomId: number, userId: string): Promise<ApiResponse<null>> => {
   const form = new URLSearchParams();
  form.append('userId', userId);

  const res: AxiosResponse<ApiResponse<null>> = await axios.post<ApiResponse<null>>(
    `/api/room/${roomId}/enter`, 
    form,
    {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'X-Session-Id': sessionId
      },
    }
  );
  return res.data;
};

export const exitRoom = async (roomId: number, userId: string): Promise<ApiResponse<null>> => {
   const form = new URLSearchParams();
  form.append('userId', userId);

  const res: AxiosResponse<ApiResponse<null>> = await axios.post<ApiResponse<null>>(
    `/api/room/${roomId}/exit`, 
    form,
    {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
    }
  );
  return res.data;
};