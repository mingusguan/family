# syntax=docker/dockerfile:1.7

FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml ./
COPY src ./src

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy

ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-Xms256m -Xmx768m -XX:+UseG1GC -Duser.timezone=Asia/Shanghai" \
    SPRING_PROFILES_ACTIVE=prod

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl tzdata \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system --gid 10001 family \
    && useradd --system --uid 10001 --gid family --home-dir /app family \
    && mkdir -p /logs/family-boot /app/uploadPath \
    && chown -R family:family /app /logs/family-boot

COPY --from=build --chown=10001:10001 \
    /workspace/target/family-boot.jar /app/family-boot.jar

USER 10001:10001

EXPOSE 8000

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/family-boot.jar"]
