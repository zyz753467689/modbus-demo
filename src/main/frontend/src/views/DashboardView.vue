<template>
  <div>
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="8">
        <el-card>
          <template #header>TCP 服务端</template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="状态">
              <el-tag :type="tcpServer.running ? 'success' : 'info'">
                {{ tcpServer.running ? '运行中' : '未启动' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item v-if="tcpServer.running" label="端口">{{ tcpServer.port }}</el-descriptions-item>
            <el-descriptions-item v-if="tcpServer.running" label="Unit ID">{{ tcpServer.unitId }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>TCP 客户端</template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="状态">
              <el-tag :type="tcpClient.connected ? 'success' : 'info'">
                {{ tcpClient.connected ? '已连接' : '未连接' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item v-if="tcpClient.connected" label="目标">{{ tcpClient.host }}:{{ tcpClient.port }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>轮询任务</template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="活跃任务">{{ polls.length }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-card v-if="simData">
      <template #header>模拟数据 (实时)</template>
      <el-row :gutter="20">
        <el-col :span="12">
          <h4>保持寄存器 (前10)</h4>
          <el-table :data="holdingRegsPreview" size="small" border stripe>
            <el-table-column prop="offset" label="地址" width="80" />
            <el-table-column prop="value" label="值" />
          </el-table>
        </el-col>
        <el-col :span="12">
          <h4>线圈状态 (前10)</h4>
          <el-table :data="coilsPreview" size="small" border stripe>
            <el-table-column prop="offset" label="地址" width="80" />
            <el-table-column prop="value" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.value ? 'success' : 'danger'" size="small">{{ row.value ? 'ON' : 'OFF' }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-col>
      </el-row>
    </el-card>

    <el-card v-else>
      <el-empty description="启动 TCP 服务端以查看模拟数据" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '../api'
import { useWebSocket } from '../composables/useWebSocket'

const tcpServer = ref({ running: false })
const tcpClient = ref({ connected: false })
const polls = ref([])
const simData = ref(null)

const { on } = useWebSocket()

on('simulator', (data) => {
  simData.value = data.data
})

const holdingRegsPreview = computed(() => {
  if (!simData.value?.holdingRegisters) return []
  return simData.value.holdingRegisters.slice(0, 10).map((v, i) => ({ offset: i, value: v }))
})

const coilsPreview = computed(() => {
  if (!simData.value?.coils) return []
  return simData.value.coils.slice(0, 10).map((v, i) => ({ offset: i, value: v }))
})

const refresh = async () => {
  try {
    const [s, c, p] = await Promise.all([
      api.getTcpServerStatus(),
      api.getTcpClientStatus(),
      api.listPolls(),
    ])
    tcpServer.value = s.data
    tcpClient.value = c.data
    polls.value = p.data
  } catch (e) {
    console.error('Dashboard refresh failed:', e)
  }
}

onMounted(refresh)
</script>
