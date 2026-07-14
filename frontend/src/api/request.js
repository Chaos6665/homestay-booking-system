import axios from 'axios';

const instance = axios.create({
  baseURL: '/',
  timeout: 10000
});

instance.interceptors.response.use(
  (response) => {
    return response.data;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default instance;
