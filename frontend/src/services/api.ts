import axios from "axios";

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://localhost:8080/api",
});

api.interceptors.request.use((config) => {

  const savedUser = localStorage.getItem("user");

  if (savedUser) {

    const user = JSON.parse(savedUser);

    if (user.token) {
      config.headers.Authorization =
        `Bearer ${user.token}`;
    }
  }

  return config;
});
