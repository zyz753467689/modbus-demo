<template>
  <div>
    <el-card style="margin-bottom: 20px">
      <template #header>RTU 客户端连接</template>
      <el-form :inline="true">
        <el-form-item label="串口">
          <el-select v-model="serialPort" placeholder="选择串口" style="width: 200px">
            <el-option v-for="p in serialPorts" :key="p.name" :label="p.description" :value="p.name" />
            <el-option v-if="virtualPort1" :label="virtualPort1 + ' (虚拟-客户端)'" :value="virtualPort1" />
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
        <el-form-item>
          <el-button v-if="!status.connected" type="primary" @click="connect" :loading="loading">连接</el-button>
          <el-button v-else type="danger" @click="disconnect" :loading="loading">断开</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="20" v-if="status.connected">
      <el-col :span="12">
        <el-card>
          <template #header>读取数据</template>
          <el-form :model="readForm" label-width="80px">
            <el-form-item label="Unit ID">
              <el-input-number v-model="readForm.unitId" :min="1" :max="247" />
            </el-form-item>
            <el-form-item label="功能码">
              <el-select v-model="readForm.function">
                <el-option label="01 - 读线圈" :value="1" />
                <el-option label="02 - 读离散输入" :value="2" />
                <el-option label="03 - 读保持寄存器" :value="3" />
                <el-option label="04 - 读输入寄存器" :value="4" />
              </el-select>
            </el-form-item>
            <el-form-item label="起始地址">
              <el-input-number v-model="readForm.offset" :min="0" :max="65535" />
            </el-form-item>
            <el-form-item label="数量">
              <el-input-number v-model="readForm.quantity" :min="1" :max="2000" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="doRead" :loading="readLoading">读取</el-button>
            </el-form-item>
          </el-form>
          <el-table v-if="readResult" :data="readResultValues" size="small" border stripe max-height="300">
            <el-table-column prop="index" label="序号" width="80" />
            <el-table-column prop="value" label="值" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>写入数据</template>
          <el-form :model="writeForm" label-width="80px">
            <el-form-item label="Unit ID">
              <el-input-number v-model="writeForm.unitId" :min="1" :max="247" />
            </el-form-item>
            <el-form-item label="功能码">
              <el-select v-model="writeForm.function">
                <el-option label="05 - 写单个线圈" :value="5" />
                <el-option label="06 - 写单个寄存器" :value="6" />
              </el-select>
            </el-form-item>
            <el-form-item label="起始地址">
              <el-input-number v-model="writeForm.offset" :min="0" :max="65535" />
            </el-form-item>
            <el-form-item label="值">
              <el-input v-model="writeForm.valuesStr" placeholder="值，如: 100" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="doWrite" :loading="writeLoading">写入</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const serialPort = ref('/tmp/vmodbus1')
const baudRate = ref(9600)
const dataBits = ref(8)
const stopBits = ref(1)
const parity = ref(0)
const status = ref({ connected: false })
const loading = ref(false)
const serialPorts = ref([])
const virtualRunning = ref(false)
const virtualPort1 = ref(null)

const readForm = ref({ unitId: 1, function: 3, offset: 0, quantity: 10 })
const writeForm = ref({ unitId: 1, function: 6, offset: 0, valuesStr: '0' })
const readResult = ref(null)
const readLoading = ref(false)
const writeLoading = ref(false)

const readResultValues = computed(() => {
  if (!readResult.value?.values) return []
  return readResult.value.values.map((v, i) => ({ index: i, value: v }))
})

const connect = async () => {
  loading.value = true
  try {
    await api.connectRtuClient({
      serialPort: serialPort.value,
      baudRate: baudRate.value,
      dataBits: dataBits.value,
      stopBits: stopBits.value,
      parity: parity.value,
    })
    ElMessage.success('已连接')
    await refresh()
  } catch (e) {
    ElMessage.error('连接失败: ' + (e.response?.data?.error || e.message))
  } finally {
    loading.value = false
  }
}

const disconnect = async () => {
  loading.value = true
  try {
    await api.disconnectRtuClient()
    ElMessage.success('已断开')
    await refresh()
  } finally {
    loading.value = false
  }
}

const doRead = async () => {
  readLoading.value = true
  try {
    const res = await api.rtuRead(readForm.value)
    readResult.value = res.data
  } catch (e) {
    ElMessage.error('读取失败: ' + (e.response?.data?.error || e.message))
  } finally {
    readLoading.value = false
  }
}

const doWrite = async () => {
  writeLoading.value = true
  try {
    const values = writeForm.value.valuesStr.split(',').map(v => {
      const trimmed = v.trim()
      const num = Number(trimmed)
      return isNaN(num) ? trimmed === 'true' : num
    })
    await api.rtuWrite({
      unitId: writeForm.value.unitId,
      function: writeForm.value.function,
      offset: writeForm.value.offset,
      values,
    })
    ElMessage.success('写入成功')
  } catch (e) {
    ElMessage.error('写入失败: ' + (e.response?.data?.error || e.message))
  } finally {
    writeLoading.value = false
  }
}

const refresh = async () => {
  try {
    const [cRes, pRes, vRes] = await Promise.all([api.getRtuClientStatus(), api.listSerialPorts(), api.getVirtualPortStatus()])
    status.value = cRes.data
    serialPorts.value = pRes.data
    virtualRunning.value = vRes.data.running
    if (vRes.data.running && vRes.data.port1) {
      virtualPort1.value = vRes.data.port1
    }
    if (cRes.data.connected) {
      serialPort.value = cRes.data.serialPort
      baudRate.value = cRes.data.baudRate
      if (cRes.data.dataBits) dataBits.value = cRes.data.dataBits
      if (cRes.data.stopBits) stopBits.value = cRes.data.stopBits
      if (cRes.data.parity !== undefined) parity.value = cRes.data.parity
    }
  } catch (e) {
    console.error('Refresh failed:', e)
  }
}

onMounted(refresh)
</script>
