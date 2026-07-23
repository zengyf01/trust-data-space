# ============================================================
# Datar 连接器 - 多阶段构建
# ============================================================

# syntax=docker/dockerfile:1.4
FROM maven:3.9-eclipse-temurin-17 AS builder

ARG MAVEN_MIRROR=https://maven.aliyun.com/repository/public

WORKDIR /app

RUN mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?>' > /root/.m2/settings.xml && \
    echo '<settings><mirrors><mirror><id>aliyun</id><mirrorOf>*</mirrorOf><url>'$MAVEN_MIRROR'</url></mirror></mirrors></settings>' >> /root/.m2/settings.xml

COPY pom.xml ./
COPY datar-common/pom.xml  datar-common/
COPY datar-dal/pom.xml     datar-dal/
COPY datar-service/pom.xml datar-service/
COPY datar-api/pom.xml     datar-api/

RUN mkdir -p datar-common/src datar-dal/src datar-service/src datar-api/src

COPY datar-common/src  datar-common/src
COPY datar-dal/src     datar-dal/src
COPY datar-service/src datar-service/src
COPY datar-api/src     datar-api/src

RUN --mount=type=cache,target=/root/.m2/repository \
    mvn -B clean package -DskipTests \
        -Dmaven.wagon.http.retryHandler.count=3

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY --from=builder /app/datar-api/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}"]
