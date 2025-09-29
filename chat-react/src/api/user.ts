import axios from './axiosInstance';
import type { AxiosResponse } from 'axios';
import type { ApiResponse } from './types';

export const enterUser = async (id: string): Promise<ApiResponse<null>> => {
  const res: AxiosResponse<ApiResponse<null>> = await axios.post<ApiResponse<null>>(`/api/user/enter/${id}`);
  return res.data;
};