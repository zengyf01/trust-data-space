# ============================================================
# TDS 前端 - 多阶段构建
# ============================================================

FROM node:18-alpine AS builder

ARG NPM_REGISTRY=https://registry.npmmirror.com

WORKDIR /app

# 切换 npm 镜像源
RUN npm config set registry $NPM_REGISTRY

COPY package*.json ./
RUN npm install --no-audit --no-fund

COPY . .
RUN npm run build

# ---- 运行阶段 ----
FROM nginx:alpine

# 替换默认配置
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
