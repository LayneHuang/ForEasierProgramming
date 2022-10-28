---
title: hdfs接入
date: 2022-10-27 21:00:00
categories: hdfs
---

# 相关资料

{% link 'SpringBoot集成HDFS' https://www.jianshu.com/p/0a2d89397cbd [title] %}  
{% link 'Hadoop下载' https://hadoop.apache.org/releases.html [title] %}  
{% link 'Hadoop下载' https://mirrors.tuna.tsinghua.edu.cn/apache/hadoop/common/ [title] %}
<!-- more -->

### hadoop winutils

https://www.jianshu.com/p/6efd353c4b25  
github.com/steveloughran/winutils

### 启动命令

在 hadoop 的 sbin 目录下

```shell
./start-dfs.sh
```

windows 环境还需要自己安装 ssh, 还是挺麻烦的
{% link 'windows开启' https://blog.csdn.net/qq_44758798/article/details/125742911 [title] %}

### docker部署hadoop

{% link 'sequenceiq/hadoop-docker搭建' https://www.jianshu.com/p/9c9f1dc22c3b [title] %}


```shell
docker pull sequenceiq/hadoop-docker:2.7.0
```

