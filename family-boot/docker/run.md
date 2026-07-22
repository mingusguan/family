# family-boot Docker 部署

## 部署应用

在 `family-boot/docker` 目录执行：

```bash
docker compose --env-file ../.env up -d --build
```

## 接入 Nginx

Nginx 配置通过容器名 `family-boot:8000` 转发，因此首次部署需要让 Nginx 加入应用网络：

```bash
docker network connect family-network mingus-nginx
```

如果提示容器已经加入网络，可以忽略。之后检查并刷新 Nginx：

```bash
docker exec mingus-nginx nginx -t && docker exec mingus-nginx nginx -s reload
```

## 验证

```bash
docker ps --filter name=family-boot
docker logs --tail 200 family-boot
curl -i http://127.0.0.1:8000/
curl -i http://family.mingusone.com/api/
```

## 更新应用

重新上传代码或 JAR 后执行：

```bash
docker compose --env-file ../.env up -d --build --force-recreate family-boot
```