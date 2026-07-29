FROM python:3.10-slim

WORKDIR /app

# 安装依赖
RUN pip install --no-cache-dir flask requests

# 复制agent脚本
COPY agent.py /app/

# 开放端口
EXPOSE 8081

# 启动命令
CMD ["python", "agent.py"]

# 环境变量
ENV AGENT_PORT=8081
