<template>
  <el-container style="height: 100vh">
    <el-aside width="220px" style="background: #304156">
      <div style="color: #fff; padding: 20px; font-size: 18px; font-weight: bold; text-align: center">
        Modbus Demo
      </div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/">
          <el-icon><Monitor /></el-icon>
          <span>总览</span>
        </el-menu-item>
        <el-sub-menu index="tcp">
          <template #title>
            <el-icon><Connection /></el-icon>
            <span>Modbus TCP</span>
          </template>
          <el-menu-item index="/tcp/server">TCP 服务端</el-menu-item>
          <el-menu-item index="/tcp/client">TCP 客户端</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="rtu">
          <template #title>
            <el-icon><Cpu /></el-icon>
            <span>Modbus RTU</span>
          </template>
          <el-menu-item index="/rtu/server">RTU 服务端</el-menu-item>
          <el-menu-item index="/rtu/client">RTU 客户端</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/poll">
          <el-icon><DataLine /></el-icon>
          <span>轮询监控</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-main style="background: #f0f2f5; padding: 20px; overflow-y: auto">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { Monitor, Connection, Cpu, DataLine } from '@element-plus/icons-vue'
</script>

<style>
body { margin: 0; padding: 0; }
</style>
