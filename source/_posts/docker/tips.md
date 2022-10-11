---
title: Docker tips
date: 2021-06-25 16:40:00
categories: Docker
---

### Docker常用命令

删除所有镜像
```shell
docker rmi $(docker images -q)
```

进入容器
```shell
docker exec -it redis /bin/bash
docker exec -it redis sh
```

复制文件
```shell
docker cp -a emqx:/opt/emqx/etc/plugins/emqx_web_hook.conf emqx_web_hook.conf
```