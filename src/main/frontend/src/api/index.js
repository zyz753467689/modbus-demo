import axios from 'axios'

const api = axios.create({ baseURL: '/api' })

export default {
  // TCP Server
  startTcpServer: (data = {}) => api.post('/server/tcp/start', data),
  stopTcpServer: () => api.post('/server/tcp/stop'),
  getTcpServerStatus: () => api.get('/server/tcp/status'),
  getTcpServerData: () => api.get('/server/tcp/data'),
  setTcpServerData: (type, offset, value) => api.put(`/server/tcp/data/${type}/${offset}`, { value }),

  // RTU Server
  startRtuServer: (data) => api.post('/server/rtu/start', data),
  stopRtuServer: () => api.post('/server/rtu/stop'),
  getRtuServerStatus: () => api.get('/server/rtu/status'),
  getRtuServerData: () => api.get('/server/rtu/data'),

  // TCP Client
  connectTcpClient: (data) => api.post('/client/tcp/connect', data),
  disconnectTcpClient: () => api.post('/client/tcp/disconnect'),
  getTcpClientStatus: () => api.get('/client/tcp/status'),
  tcpRead: (data) => api.post('/client/tcp/read', data),
  tcpWrite: (data) => api.post('/client/tcp/write', data),

  // RTU Client
  connectRtuClient: (data) => api.post('/client/rtu/connect', data),
  disconnectRtuClient: () => api.post('/client/rtu/disconnect'),
  getRtuClientStatus: () => api.get('/client/rtu/status'),
  rtuRead: (data) => api.post('/client/rtu/read', data),
  rtuWrite: (data) => api.post('/client/rtu/write', data),

  // Poll
  startPoll: (data) => api.post('/poll/start', data),
  stopPoll: (id) => api.post('/poll/stop', { id }),
  listPolls: () => api.get('/poll/list'),

  // Serial
  listSerialPorts: () => api.get('/serial/ports'),
  startVirtualPorts: () => api.post('/serial/virtual/start'),
  stopVirtualPorts: () => api.post('/serial/virtual/stop'),
  getVirtualPortStatus: () => api.get('/serial/virtual/status'),
}
