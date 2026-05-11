import { ref, onMounted, onUnmounted } from 'vue'

export function useWebSocket(path = '/ws/data') {
  const ws = ref(null)
  const connected = ref(false)
  const lastMessage = ref(null)
  const listeners = new Map()

  const connect = () => {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const url = `${protocol}//${window.location.host}${path}`
    ws.value = new WebSocket(url)

    ws.value.onopen = () => { connected.value = true }
    ws.value.onclose = () => { connected.value = false }
    ws.value.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        lastMessage.value = data
        const type = data.type
        if (listeners.has(type)) {
          listeners.get(type).forEach(cb => cb(data))
        }
        if (listeners.has('*')) {
          listeners.get('*').forEach(cb => cb(data))
        }
      } catch (e) {
        console.error('WebSocket parse error:', e)
      }
    }
  }

  const on = (type, callback) => {
    if (!listeners.has(type)) {
      listeners.set(type, [])
    }
    listeners.get(type).push(callback)
  }

  const disconnect = () => {
    if (ws.value) {
      ws.value.close()
      ws.value = null
    }
  }

  onMounted(connect)
  onUnmounted(disconnect)

  return { connected, lastMessage, on, disconnect, connect }
}
