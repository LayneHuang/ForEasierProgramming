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

启动
```shell
docker run -it -d --name phoenix \
-p 2181:2181 -p 8765:8765 -p 15165:15165 \
-p 16000:16000 -p 16010:16010 -p 16020:16020 \
boostport/hbase-phoenix-all-in-one
```