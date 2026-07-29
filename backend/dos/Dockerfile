# ============================================================
# DOS 交付平台 - 多阶段构建
# ============================================================

# syntax=docker/dockerfile:1.4
FROM maven:3.9-eclipse-temurin-17 AS builder

ARG MAVEN_MIRROR=https://maven.aliyun.com/repository/public

WORKDIR /app

RUN mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?>' > /root/.m2/settings.xml && \
    echo '<settings><mirrors><mirror><id>aliyun</id><mirrorOf>*</mirrorOf><url>'$MAVEN_MIRROR'</url></mirror></mirrors></settings>' >> /root/.m2/settings.xml

COPY pom.xml ./
COPY dos-common/pom.xml  dos-common/
COPY dos-dal/pom.xml     dos-dal/
COPY dos-service/pom.xml dos-service/
COPY dos-api/pom.xml     dos-api/

RUN mkdir -p dos-common/src dos-dal/src dos-service/src dos-api/src

COPY dos-common/src  dos-common/src
COPY dos-dal/src     dos-dal/src
COPY dos-service/src dos-service/src
COPY dos-api/src     dos-api/src

RUN --mount=type=cache,target=/root/.m2/repository \
    mvn -B clean package -DskipTests \
        -Dmaven.wagon.http.retryHandler.count=3

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY --from=builder /app/dos-api/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}"]
