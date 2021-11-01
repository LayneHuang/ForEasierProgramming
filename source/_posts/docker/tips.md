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