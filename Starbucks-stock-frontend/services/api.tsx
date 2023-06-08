import axios from "axios"

const api = axios.create({
  // baseURL: `http://${process.env.NEXT_PUBLIC_APIHOST}:${process.env.NEXT_PUBLIC_APIPORT}`
  baseURL: `http://localhost:${process.env.NEXT_PUBLIC_APIPORT}`
})

export default api;