# PowerShell脚本：合并com.employmentsupport.eaps和com.bs.eaps包

# 项目根目录
$PROJECT_ROOT = "D:\bs\project"
$SOURCE_DIR = "$PROJECT_ROOT\eaps\src\main\java\com\employmentsupport\eaps"
$TARGET_DIR = "$PROJECT_ROOT\eaps\src\main\java\com\bs\eaps"
$BACKUP_DIR = "$PROJECT_ROOT\eaps-backup-$(Get-Date -Format 'yyyyMMdd_HHmmss')"

# 创建备份目录
Write-Host "正在创建备份..." -ForegroundColor Green
New-Item -ItemType Directory -Path $BACKUP_DIR -Force | Out-Null
Copy-Item -Path "$PROJECT_ROOT\eaps" -Destination $BACKUP_DIR -Recurse

# 按模块合并代码
$MODULES = @(
    "entity",
    "mapper",
    "service", 
    "controller", 
    "dto", 
    "common", 
    "config", 
    "security"
)

foreach ($module in $MODULES) {
    $moduleSourceDir = "$SOURCE_DIR\$module"
    $moduleTargetDir = "$TARGET_DIR\$module"
    
    # 检查源目录是否存在
    if (Test-Path $moduleSourceDir) {
        Write-Host "处理模块: $module" -ForegroundColor Cyan
        
        # 确保目标目录存在
        if (-not (Test-Path $moduleTargetDir)) {
            New-Item -ItemType Directory -Path $moduleTargetDir -Force | Out-Null
        }
        
        # 获取源目录中的文件
        $files = Get-ChildItem -Path $moduleSourceDir -File -Recurse
        
        foreach ($file in $files) {
            $targetFilePath = $file.FullName -replace [regex]::Escape($SOURCE_DIR), $TARGET_DIR
            $targetFile = Get-Item -Path $targetFilePath -ErrorAction SilentlyContinue
            
            # 如果目标文件不存在，直接复制
            if ($null -eq $targetFile) {
                # 确保目标文件的目录存在
                $targetFileDir = Split-Path -Path $targetFilePath -Parent
                if (-not (Test-Path $targetFileDir)) {
                    New-Item -ItemType Directory -Path $targetFileDir -Force | Out-Null
                }
                
                # 复制文件并更新包名
                Write-Host "复制文件: $($file.Name) -> $targetFilePath" -ForegroundColor Yellow
                $content = Get-Content -Path $file.FullName -Raw
                $updatedContent = $content -replace "package com\.employmentsupport\.eaps", "package com.bs.eaps"
                $updatedContent = $updatedContent -replace "import com\.employmentsupport\.eaps", "import com.bs.eaps"
                Set-Content -Path $targetFilePath -Value $updatedContent
            } else {
                # 文件存在，标记为需要手动合并
                Write-Host "文件已存在，需要手动合并: $($file.Name)" -ForegroundColor Red
                # 创建一个备份文件，供手动合并
                $mergeFilePath = "$targetFilePath.employmentsupport.java"
                $content = Get-Content -Path $file.FullName -Raw
                $updatedContent = $content -replace "package com\.employmentsupport\.eaps", "package com.bs.eaps"
                $updatedContent = $updatedContent -replace "import com\.employmentsupport\.eaps", "import com.bs.eaps"
                Set-Content -Path $mergeFilePath -Value $updatedContent
            }
        }
    }
}

# 处理主应用类
$mainAppSourcePath = "$SOURCE_DIR\EapsApplication.java"
$mainAppTargetPath = "$TARGET_DIR\EapsApplication.java"

if (Test-Path $mainAppSourcePath) {
    Write-Host "处理主应用类: EapsApplication.java" -ForegroundColor Green
    $mainAppSource = Get-Content -Path $mainAppSourcePath -Raw
    $mainAppMergePath = "$mainAppTargetPath.employmentsupport.java"
    $updatedContent = $mainAppSource -replace "package com\.employmentsupport\.eaps", "package com.bs.eaps"
    $updatedContent = $updatedContent -replace "import com\.employmentsupport\.eaps", "import com.bs.eaps"
    $updatedContent = $updatedContent -replace "@MapperScan\(""com\.employmentsupport\.eaps\.mapper""\)", '@MapperScan("com.bs.eaps.mapper")'
    Set-Content -Path $mainAppMergePath -Value $updatedContent
}

Write-Host "代码合并初步完成!" -ForegroundColor Green
Write-Host "已创建备份: $BACKUP_DIR" -ForegroundColor Green
Write-Host "请手动处理需要合并的文件（带有.employmentsupport.java扩展名的文件）" -ForegroundColor Yellow 