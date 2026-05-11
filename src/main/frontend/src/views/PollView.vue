<template>
  <div>
    <el-card style="margin-bottom: 20px">
      <template #header>创建轮询任务</template>
      <el-form :model="form" :inline="true" label-width="80px">
        <el-form-item label="连接类型">
          <el-select v-model="form.connectionType" style="width: 100px">
            <el-option label="TCP" value="TCP" />
            <el-option label="RTU" value="RTU" />
          </el-select>
        </el-form-item>
        <el-form-item label="Unit ID">
          <el-input-number v-model="form.unitId" :min="1" :max="247" />
        </el-form-item>
        <el-form-item label="功能码">
          <el-select v-model="form.function" style="width: 180px">
            <el-option label="01 - 读线圈" :value="1" />
            <el-option label="02 - 读离散输入" :value="2" />
            <el-option label="03 - 读保持寄存器" :value="3" />
            <el-option label="04 - 读输入寄存器" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="地址">
          <el-input-number v-model="form.offset" :min="0" :max="65535" />
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="form.quantity" :min="1" :max="125" />
        </el-form-item>
        <el-form-item label="间隔(ms)">
          <el-input-number v-model="form.intervalMs" :min="100" :max="60000" :step="100" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="startPoll" :loading="loading">启动轮询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-bottom: 20px">
      <template #header>活跃轮询任务</template>
      <el-table :data="polls" size="small" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="connectionType" label="类型" width="60" />
        <el-table-column prop="function" label="功能" width="150" />
        <el-table-column prop="offset" label="地址" width="60" />
        <el-table-column prop="quantity" label="数量" width="60" />
        <el-table-column prop="intervalMs" label="间隔(ms)" width="80" />
        <el-table-column prop="lastError" label="错误" />
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button type="danger" size="small" @click="stopPoll(row.id)">停止</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card>
      <template #header>实时数据</template>
      <div ref="chartContainer" style="height: 400px; width: 100%"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'
import { useWebSocket } from '../composables/useWebSocket'
import * as echarts from 'echarts'

const form = ref({
  connectionType: 'TCP',
  unitId: 1,
  function: 3,
  offset: 0,
  quantity: 10,
  intervalMs: 1000,
})
const polls = ref([])
const loading = ref(false)
const chartContainer = ref(null)
let chart = null

const timeSeriesData = ref({})

const { on } = useWebSocket()

on('poll', (data) => {
  const pollId = data.pollId
  const values = data.data?.values
  if (!values) return

  if (!timeSeriesData.value[pollId]) {
    timeSeriesData.value[pollId] = { times: [], values: [] }
  }

  const ts = timeSeriesData.value[pollId]
  const time = new Date(data.timestamp).toLocaleTimeString()
  ts.times.push(time)
  ts.values.push(values)

  // Keep last 50 readings
  if (ts.times.length > 50) {
    ts.times.shift()
    ts.values.shift()
  }

  updateChart()
})

const updateChart = () => {
  if (!chart) return

  const firstKey = Object.keys(timeSeriesData.value)[0]
  if (!firstKey) return

  const ts = timeSeriesData.value[firstKey]
  const values0 = ts.values.map(v => Array.isArray(v) ? v[0] : v)

  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ts.times },
    yAxis: { type: 'value' },
    series: [{ data: values0, type: 'line', smooth: true, name: 'Register 0' }],
  })
}

const startPoll = async () => {
  loading.value = true
  try {
    await api.startPoll(form.value)
    ElMessage.success('轮询任务已创建')
    await refreshPolls()
  } catch (e) {
    ElMessage.error('创建失败: ' + (e.response?.data?.error || e.message))
  } finally {
    loading.value = false
  }
}

const stopPoll = async (id) => {
  try {
    await api.stopPoll(id)
    ElMessage.success('轮询任务已停止')
    delete timeSeriesData.value[id]
    await refreshPolls()
  } catch (e) {
    ElMessage.error('停止失败: ' + e.message)
  }
}

const refreshPolls = async () => {
  try {
    const res = await api.listPolls()
    polls.value = res.data
  } catch (e) {
    console.error('Refresh polls failed:', e)
  }
}

onMounted(async () => {
  await refreshPolls()
  await nextTick()
  if (chartContainer.value) {
    chart = echarts.init(chartContainer.value)
  }
  window.addEventListener('resize', () => chart?.resize())
})

onUnmounted(() => {
  chart?.dispose()
})
</script>
