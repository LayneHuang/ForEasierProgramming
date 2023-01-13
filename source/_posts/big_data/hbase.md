---
title: hbase接入
date: 2022-12-29 21:00:00
categories: Hadoop
---

```shell
docker pull harisekhon/hbase
```

### phoenix 版本

{% link '部署参考博客' https://blog.csdn.net/lambert00001/article/details/127761406 [title] %}

```shell
docker pull boostport/hbase-phoenix-all-in-one
```

无hadoop网络时先添加子网络

```shell
docker network create --driver=bridge --subnet=172.19.0.0/16 hnet
```

启动

```shell
#!/bin/bash
docker rm -f phoenix
docker run -it -d --name phoenix \
-e TZ=Asia/Shanghai \
--net=hnet \
--ip=172.19.1.5 \
--hostname=phoenix \
-p 2181:2181 \
-p 8765:8765 \
-p 15165:15165 \
-p 16000:16000 \
-p 16010:16010 \
-p 16020:16020 \
-v /home/hbase/data:/tmp \
boostport/hbase-phoenix-all-in-one:2.0-5.0
```
