@echo off
echo ====================================
echo 大学生就业帮扶系统启动脚本
echo ====================================

echo 请选择要启动的环境:
echo 1. 开发环境(本地API)
echo 2. 生产环境
echo 3. 测试环境

choice /c 123 /m "请选择环境(1-3):"

if errorlevel 3 goto test
if errorlevel 2 goto prod
if errorlevel 1 goto dev

:dev
echo 正在启动开发环境...
set VITE_API_BASE_URL=http://localhost:8080
set VITE_APP_TITLE=大学生就业帮扶系统(开发环境)
set VITE_ENABLE_DEBUG=true
set VITE_ENABLE_MOCK=false
set VITE_FILE_UPLOAD_LIMIT=10485760
npm run dev
goto end

:prod
echo 正在启动生产环境...
set VITE_API_BASE_URL=https://api.job-help.com
set VITE_APP_TITLE=大学生就业帮扶系统
set VITE_ENABLE_DEBUG=false
set VITE_ENABLE_MOCK=false
set VITE_FILE_UPLOAD_LIMIT=5242880
npm run build
npm run preview
goto end

:test
echo 正在启动测试环境...
set VITE_API_BASE_URL=http://test-api.job-help.com
set VITE_APP_TITLE=大学生就业帮扶系统(测试环境)
set VITE_ENABLE_DEBUG=true
set VITE_ENABLE_MOCK=true
set VITE_FILE_UPLOAD_LIMIT=10485760
npm run dev
goto end

:end
echo 应用已启动 