<template>
  <div>
    <el-card style="margin-bottom: 20px">
      <template #header>TCP 客户端连接</template>
      <el-form :inline="true">
        <el-form-item label="主机">
          <el-input v-model="host" style="width: 150px" />
        </el-form-item>
        <el-form-item label="端口">
          <el-input-number v-model="port" :min="1" :max="65535" />
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
                <el-option label="15 - 写多个线圈" :value="15" />
                <el-option label="16 - 写多个寄存器" :value="16" />
              </el-select>
            </el-form-item>
            <el-form-item label="起始地址">
              <el-input-number v-model="writeForm.offset" :min="0" :max="65535" />
            </el-form-item>
            <el-form-item label="值">
              <el-input v-model="writeForm.valuesStr" placeholder="逗号分隔，如: 100,200" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="doWrite" :loading="writeLoading">写入</el-button>
            </el-form-item>
          </el-form>
          <el-result v-if="writeResult" icon="success" :title="'写入成功'" :sub-title="JSON.stringify(writeResult)" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const host = ref('localhost')
const port = ref(5020)
const status = ref({ connected: false })
const loading = ref(false)

const readForm = ref({ unitId: 1, function: 3, offset: 0, quantity: 10 })
const writeForm = ref({ unitId: 1, function: 6, offset: 0, valuesStr: '0' })
const readResult = ref(null)
const writeResult = ref(null)
const readLoading = ref(false)
const writeLoading = ref(false)

const readResultValues = computed(() => {
  if (!readResult.value?.values) return []
  return readResult.value.values.map((v, i) => ({ index: i, value: v }))
})

const connect = async () => {
  loading.value = true
  try {
    await api.connectTcpClient({ host: host.value, port: port.value })
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
    await api.disconnectTcpClient()
    ElMessage.success('已断开')
    await refresh()
  } finally {
    loading.value = false
  }
}

const doRead = async () => {
  readLoading.value = true
  try {
    const res = await api.tcpRead(readForm.value)
    readResult.value = res.data
  } catch (e) {
    ElMessage.error('读取失败: ' + (e.response?.data?.error || e.message))
    readResult.value = null
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
    const res = await api.tcpWrite({
      unitId: writeForm.value.unitId,
      function: writeForm.value.function,
      offset: writeForm.value.offset,
      values,
    })
    writeResult.value = res.data
    ElMessage.success('写入成功')
  } catch (e) {
    ElMessage.error('写入失败: ' + (e.response?.data?.error || e.message))
    writeResult.value = null
  } finally {
    writeLoading.value = false
  }
}

const refresh = async () => {
  try {
    const res = await api.getTcpClientStatus()
    status.value = res.data
  } catch (e) {
    console.error('Refresh failed:', e)
  }
}

onMounted(refresh)
</script>
