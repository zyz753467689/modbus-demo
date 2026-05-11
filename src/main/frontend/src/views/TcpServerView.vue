<template>
  <div>
    <el-card style="margin-bottom: 20px">
      <template #header>TCP 服务端控制</template>
      <el-form :inline="true">
        <el-form-item label="端口">
          <el-input-number v-model="port" :min="1024" :max="65535" />
        </el-form-item>
        <el-form-item label="Unit ID">
          <el-input-number v-model="unitId" :min="1" :max="247" />
        </el-form-item>
        <el-form-item>
          <el-button v-if="!status.running" type="primary" @click="start" :loading="loading">启动</el-button>
          <el-button v-else type="danger" @click="stop" :loading="loading">停止</el-button>
        </el-form-item>
      </el-form>
      <el-descriptions :column="3" border size="small" v-if="status.running">
        <el-descriptions-item label="状态"><el-tag type="success">运行中</el-tag></el-descriptions-item>
        <el-descriptions-item label="端口">{{ status.port }}</el-descriptions-item>
        <el-descriptions-item label="Unit ID">{{ status.unitId }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card v-if="status.running">
      <template #header>数据区</template>
      <el-tabs>
        <el-tab-pane label="保持寄存器">
          <el-table :data="holdingRegs" size="small" border stripe max-height="400">
            <el-table-column prop="offset" label="地址" width="80" />
            <el-table-column prop="value" label="值">
              <template #default="{ row }">
                <el-input-number v-model="row.value" :min="0" :max="65535" :controls="false" size="small"
                  @change="(val) => updateData('holdingregister', row.offset, val)" />
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="线圈">
          <el-table :data="coils" size="small" border stripe max-height="400">
            <el-table-column prop="offset" label="地址" width="80" />
            <el-table-column prop="value" label="状态" width="100">
              <template #default="{ row }">
                <el-switch v-model="row.value" @change="(val) => updateData('coil', row.offset, val)" />
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="输入寄存器">
          <el-table :data="inputRegs" size="small" border stripe max-height="400">
            <el-table-column prop="offset" label="地址" width="80" />
            <el-table-column prop="value" label="值" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="离散输入">
          <el-table :data="discreteInputs" size="small" border stripe max-height="400">
            <el-table-column prop="offset" label="地址" width="80" />
            <el-table-column prop="value" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.value ? 'success' : 'danger'" size="small">{{ row.value ? 'ON' : 'OFF' }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'
import { useWebSocket } from '../composables/useWebSocket'

const port = ref(5020)
const unitId = ref(1)
const status = ref({ running: false })
const serverData = ref(null)
const loading = ref(false)

const { on } = useWebSocket()

on('simulator', (data) => {
  if (status.value.running) {
    serverData.value = data.data
  }
})

const holdingRegs = computed(() => {
  if (!serverData.value?.holdingRegisters) return []
  return serverData.value.holdingRegisters.map((v, i) => ({ offset: i, value: v }))
})

const coils = computed(() => {
  if (!serverData.value?.coils) return []
  return serverData.value.coils.map((v, i) => ({ offset: i, value: v }))
})

const inputRegs = computed(() => {
  if (!serverData.value?.inputRegisters) return []
  return serverData.value.inputRegisters.map((v, i) => ({ offset: i, value: v }))
})

const discreteInputs = computed(() => {
  if (!serverData.value?.discreteInputs) return []
  return serverData.value.discreteInputs.map((v, i) => ({ offset: i, value: v }))
})

const start = async () => {
  loading.value = true
  try {
    await api.startTcpServer({ port: port.value, unitId: unitId.value })
    ElMessage.success('TCP 服务端已启动')
    await refresh()
  } catch (e) {
    ElMessage.error('启动失败: ' + (e.response?.data?.error || e.message))
  } finally {
    loading.value = false
  }
}

const stop = async () => {
  loading.value = true
  try {
    await api.stopTcpServer()
    ElMessage.success('TCP 服务端已停止')
    serverData.value = null
    await refresh()
  } catch (e) {
    ElMessage.error('停止失败: ' + e.message)
  } finally {
    loading.value = false
  }
}

const updateData = async (type, offset, value) => {
  try {
    await api.setTcpServerData(type, offset, value)
  } catch (e) {
    ElMessage.error('更新失败: ' + e.message)
  }
}

const refresh = async () => {
  try {
    const res = await api.getTcpServerStatus()
    status.value = res.data
    if (res.data.running) {
      const dataRes = await api.getTcpServerData()
      serverData.value = dataRes.data
    }
  } catch (e) {
    console.error('Refresh failed:', e)
  }
}

onMounted(refresh)
</script>
