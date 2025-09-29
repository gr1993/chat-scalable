import type { ApiResponse } from './types';

export const handleApiResponse = async <T>(
  apiCall: Promise<ApiResponse<T>>,
  successCallback: (data: T | null) => void
) => {
  try {
    const res = await apiCall;

    if (res.success) {
      successCallback(res.data);
    } else {
      alert(res.message);
    }
  } catch (err) {
    console.error(err);
    alert('서버 오류가 발생했습니다.');
  }
};