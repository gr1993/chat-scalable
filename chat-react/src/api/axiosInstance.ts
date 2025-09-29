import axios from 'axios';

const instance = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '', // Vite 환경 변수
  withCredentials: false, // 쿠키 인증 등 필요할 경우
  timeout: 5000,
});

export default instance;