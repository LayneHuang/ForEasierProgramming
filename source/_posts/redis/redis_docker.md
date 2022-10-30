---
title: Docker 搭建 Redis
date: 2021-12-06 16:40:00
categories: Redis
---

```shell script
docker pull redis
docker run --log-opt max-file=2 -p 6379:6379 --restart=always --name redis -v /h/db/redis/data:/data -d redis redis-server /etc/redis/redis.conf  --appendonly yes  --requirepass 000415
```

