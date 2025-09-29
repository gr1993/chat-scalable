import axios from './axiosInstance';
import type { AxiosResponse } from 'axios';
import type { ApiResponse } from './types';

export const entryUser = async (id: string): Promise<ApiResponse<null>> => {
  const res: AxiosResponse<ApiResponse<null>> = await axios.post<ApiResponse<null>>(`/api/user/entry/${id}`);
  return res.data;
};