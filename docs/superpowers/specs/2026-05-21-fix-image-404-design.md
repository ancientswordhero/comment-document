# 修复封面图片 404 — 设计文档

## 概述

修复 IDEA 中运行时封面图片加载 404 的问题。根因是上传路径和资源映射路径都依赖 `user.dir`（JVM 工作目录），IDEA 和命令行的工作目录不同导致路径不一致。

## 根因

- `FileService` 用 `Paths.get(uploadDir).toAbsolutePath()` 上传文件 → 实际存到 `library-server/uploads/covers/`
- `WebConfig` 用相同方式解析资源映射 → IDEA 工作目录为项目根时映射到 `shuhai/uploads/`（空目录）
- 两处不一致导致 404

## 修复方案

将 `file.upload-dir` 从相对路径改为基于 `user.home` 的绝对路径，彻底脱离 `user.dir` 依赖。

### 改动清单

1. `application.yml` — `file.upload-dir` 改为 `${user.home}/.library/uploads/covers`
2. `application-dev.yml` — 同上
3. 迁移已有文件至新路径

### 不改动

- `FileService.java` — 无需修改，`Paths.get()` 对绝对路径正确处理
- `WebConfig.java` — `getParent()` 逻辑在新路径下仍然正确，无需修改
