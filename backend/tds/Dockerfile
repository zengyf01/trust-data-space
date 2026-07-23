# ============================================================
# TDS 服务平台 - 多阶段构建
# 启用 BuildKit: docker buildx build 或 DOCKER_BUILDKIT=1
# ============================================================

# ---- 构建阶段 ----
# syntax=docker/dockerfile:1.4
FROM maven:3.9-eclipse-temurin-17 AS builder

ARG MAVEN_MIRROR=https://maven.aliyun.com/repository/public

WORKDIR /app

# 配置 Maven 镜像（创建 settings.xml 持久化）
RUN mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?>' > /root/.m2/settings.xml && \
    echo '<settings>' >> /root/.m2/settings.xml && \
    echo '  <mirrors><mirror><id>aliyun</id><mirrorOf>*</mirrorOf><url>'$MAVEN_MIRROR'</url></mirror></mirrors>' >> /root/.m2/settings.xml && \
    echo '</settings>' >> /root/.m2/settings.xml

# 优先复制 pom 利用 Docker 层缓存
COPY pom.xml ./
COPY tds-common/pom.xml  tds-common/
COPY tds-dal/pom.xml     tds-dal/
COPY tds-service/pom.xml tds-service/
COPY tds-api/pom.xml     tds-api/

# 占位 src 目录让 mvn 能解析模块
RUN mkdir -p tds-common/src tds-dal/src tds-service/src tds-api/src

# 复制源码
COPY tds-common/src  tds-common/src
COPY tds-dal/src     tds-dal/src
COPY tds-service/src tds-service/src
COPY tds-api/src     tds-api/src

# 使用 BuildKit 缓存挂载共享 Maven 仓库（跨构建复用）
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn -B clean package -DskipTests \
        -Dmaven.wagon.http.retryHandler.count=3 \
        -Dmaven.wagon.httpconnectionManager.ttlSeconds=120

# ---- 运行阶段 ----
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# 时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY --from=builder /app/tds-api/target/*.jar app.jar

EXPOSE 8080

# 使用 JAVA_OPTS 便于在 docker-compose 中调整
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}"]
