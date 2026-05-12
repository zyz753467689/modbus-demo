<template>
  <div>
    <el-card style="margin-bottom: 20px">
      <template #header>RTU 服务端控制</template>
      <el-form :inline="true">
        <el-form-item label="串口">
          <el-select v-model="serialPort" placeholder="选择串口" style="width: 200px">
            <el-option v-for="p in serialPorts" :key="p.name" :label="p.description" :value="p.name" />
            <el-option v-if="virtualPort0" :label="virtualPort0 + ' (虚拟-服务端)'" :value="virtualPort0" />
            <el-option v-else-if="!virtualRunning" label="(需先创建虚拟串口)" value="" disabled />
          </el-select>
        </el-form-item>
        <el-form-item label="波特率">
          <el-select v-model="baudRate" style="width: 100px">
            <el-option v-for="b in [9600, 19200, 38400, 57600, 115200]" :key="b" :label="String(b)" :value="b" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据位">
          <el-select v-model="dataBits" style="width: 80px">
            <el-option :value="8" label="8" />
            <el-option :value="7" label="7" />
            <el-option :value="6" label="6" />
            <el-option :value="5" label="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="停止位">
          <el-select v-model="stopBits" style="width: 80px">
            <el-option :value="1" label="1" />
            <el-option :value="2" label="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="校验">
          <el-select v-model="parity" style="width: 80px">
            <el-option :value="0" label="无" />
            <el-option :value="1" label="奇" />
            <el-option :value="2" label="偶" />
          </el-select>
        </el-form-item>
        <el-form-item label="Unit ID">
          <el-input-number v-model="unitId" :min="1" :max="247" />
        </el-form-item>
        <el-form-item>
          <el-button v-if="!status.running" type="primary" @click="start" :loading="loading">启动</el-button>
          <el-button v-else type="danger" @click="stop" :loading="loading">停止</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-bottom: 20px">
      <template #header>虚拟串口</template>
      <el-form :inline="true">
        <el-form-item>
          <el-button type="warning" @click="startVirtual" :loading="virtualLoading">创建虚拟串口对</el-button>
          <el-button @click="stopVirtual">停止虚拟串口</el-button>
        </el-form-item>
        <el-form-item>
          <el-tag :type="virtualRunning ? 'success' : 'info'">{{ virtualRunning ? '虚拟串口运行中' : '未启动' }}</el-tag>
        </el-form-item>
      </el-form>
      <el-alert v-if="virtualRunning" type="info" :closable="false" show-icon>
        虚拟串口已创建: {{ virtualPort0 || '/tmp/vmodbus0' }} (服务端) <-> {{ virtualPort1 || '/tmp/vmodbus1' }} (客户端)
      </el-alert>
    </el-card>

    <el-card v-if="status.running">
      <template #header>数据区</template>
      <el-tabs>
        <el-tab-pane label="保持寄存器">
          <el-table :data="holdingRegs" size="small" border stripe max-height="400">
            <el-table-column prop="offset" label="地址" width="80" />
            <el-table-column prop="value" label="值" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="线圈">
          <el-table :data="coils" size="small" border stripe max-height="400">
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

const serialPort = ref('/tmp/vmodbus0')
const baudRate = ref(9600)
const dataBits = ref(8)
const stopBits = ref(1)
const parity = ref(0)
const unitId = ref(1)
const status = ref({ running: false })
const serverData = ref(null)
const loading = ref(false)
const serialPorts = ref([])
const virtualRunning = ref(false)
const virtualLoading = ref(false)
const virtualPort0 = ref(null)
const virtualPort1 = ref(null)

const holdingRegs = computed(() => {
  if (!serverData.value?.holdingRegisters) return []
  return serverData.value.holdingRegisters.map((v, i) => ({ offset: i, value: v }))
})

const coils = computed(() => {
  if (!serverData.value?.coils) return []
  return serverData.value.coils.map((v, i) => ({ offset: i, value: v }))
})

const start = async () => {
  loading.value = true
  try {
    await api.startRtuServer({
      serialPort: serialPort.value,
      baudRate: baudRate.value,
      dataBits: dataBits.value,
      stopBits: stopBits.value,
      parity: parity.value,
      unitId: unitId.value,
    })
    ElMessage.success('RTU 服务端已启动')
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
    await api.stopRtuServer()
    ElMessage.success('RTU 服务端已停止')
    serverData.value = null
    await refresh()
  } finally {
    loading.value = false
  }
}

const startVirtual = async () => {
  virtualLoading.value = true
  try {
    const res = await api.startVirtualPorts()
    virtualRunning.value = true
    virtualPort0.value = res.data.port0
    virtualPort1.value = res.data.port1
    serialPort.value = res.data.port0
    ElMessage.success('虚拟串口已创建')
  } catch (e) {
    ElMessage.error('创建失败: ' + (e.response?.data?.error || e.message))
  } finally {
    virtualLoading.value = false
  }
}

const stopVirtual = async () => {
  await api.stopVirtualPorts()
  virtualRunning.value = false
  virtualPort0.value = null
  virtualPort1.value = null
}

const refresh = async () => {
  try {
    const [sRes, pRes, vRes] = await Promise.all([
      api.getRtuServerStatus(),
      api.listSerialPorts(),
      api.getVirtualPortStatus(),
    ])
    status.value = sRes.data
    serialPorts.value = pRes.data
    virtualRunning.value = vRes.data.running
    if (vRes.data.running && vRes.data.port0) {
      virtualPort0.value = vRes.data.port0
    }

    if (sRes.data.running) {
      serialPort.value = sRes.data.serialPort
      baudRate.value = sRes.data.baudRate
      unitId.value = sRes.data.unitId
      if (sRes.data.dataBits) dataBits.value = sRes.data.dataBits
      if (sRes.data.stopBits) stopBits.value = sRes.data.stopBits
      if (sRes.data.parity !== undefined) parity.value = sRes.data.parity
      const dataRes = await api.getRtuServerData()
      serverData.value = dataRes.data
    }
  } catch (e) {
    console.error('Refresh failed:', e)
  }
}

onMounted(refresh)
</script>
