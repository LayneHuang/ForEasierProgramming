---
title: hbase接入
date: 2022-12-29 21:00:00
categories: [ Hadoop ]
---

```shell
docker pull harisekhon/hbase
```

### 带上phoenix的docker版本

```shell
docker pull boostport/hbase-phoenix-all-in-one
```

无hadoop网络时先添加子网络

```shell
docker network create --driver=bridge --subnet=172.19.0.0/16 hnet
```

phoenix查询hbase

```shell
export HBASE_CONF_DIR=/opt/hbase/conf/
/opt/phoenix-server/bin/sqlline.py localhost
```

test sql

```mysql
select investment_code, current_price, percent from investment_record_min order by record_id desc limit 27;
```

命令行

```shell
# 退出
!quit
# 所有表
!tables
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

### 获取配置放到spring boot程序resources中

```shell
docker cp phoenix:/opt/hbase/conf/hbase-site.xml /home/hbase/hbase-site.xml
```

### 参考

{% link '平台搭建参考' https://blog.csdn.net/lambert00001/article/details/127761406 [title] %}
{% link '解决Client isNamespaceMappingEnabled' https://blog.csdn.net/u010022158/article/details/107490980 [title] %}
{% link 'Phoenix基本语法对比' https://blog.csdn.net/pianpianxihuanni9/article/details/105241152 [title] %}

### 自增序列

{% link '自增序列使用' https://blog.csdn.net/a2267378/article/details/101372736 %}