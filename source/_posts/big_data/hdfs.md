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
{% link '菜鸟教程搭建教学' https://www.runoob.com/w3cnote/hdfs-setup.html [title] %}
{% link '阿里hadoop镜像' https://blog.csdn.net/m0_67390969/article/details/126553657 [title] %}
{% link '集群配置' https://blog.csdn.net/qq_48961214/article/details/124495773 [title] %}
{% link '集群配置' https://blog.csdn.net/m0_51111980/article/details/125782120 [title] %}

### JAVA API

问题解决 could only be replicated to 0 nodes instead of minReplication (=1)  
{% link 'blog' https://blog.csdn.net/qq_41837900/article/details/124850389 [title] %}

hdfs其配置文件在 (/usr/local/hadoop-2.7.0/etc/hadoop) core-site.xml 和 hdfs-site.xml  
参数 `-p 9000:9000`, 需要开放hdfs的连接

{% link '端口使用详情' https://blog.csdn.net/jeffiny/article/details/78728965 [title] %}

### hadoop 集群处理

name node 与 2个 data node

```shell
# docker 拉取 hadoop 镜像
docker pull sequenceiq/hadoop-docker:2.7.0

docker run -d --name hadoop_master -e TZ=Asia/Shanghai -p 8088:8088 -p 9000:9000 -p 9870:9870 -p 50010:50010 -p 50020:50020 -p 50070:50070 -p 50075:50075 5c3cc170c6bc
docker run -d --name hadoop_slave0 -e TZ=Asia/Shanghai 5c3cc170c6bc
docker run -d --name hadoop_slave1 -e TZ=Asia/Shanghai 5c3cc170c6bc
```

配置环境变量

```shell
vi ~/.bashrc
```

```text
export HADOOP_HOME=/usr/local/hadoop
export PATH=$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin
```

环境变量生效

```shell
source ~/.bashrc
```

hdfs 常用命令

```shell
# 展示目录
hadoop fs -ls [path]
# 递归展示目录
hadoop fs -ls -R [path]
# 删除文件
hadoop fs -rm [-R] [path]
# 将本地文件 copy 到 hdfs 上
hadoop fs -put local_file [path]
# 复制 hdfs 文件到本地
hadoop fs -get hdfs_file [path]
# 查看HDFS上某文件的内容
hadoop fs -cat [path]
```

通过修改 /etc/hosts 文件实现网络互通

core-site.xml

```xml

<configuration>
    <property>
        <name>fs.defaultFS</name>
        <value>hdfs://6944ba211968:9000</value>
    </property>
    <property>
        <name>hadoop.tmp.dir</name>
        <value>file:///home/hadoop/tmp</value>
    </property>
</configuration>
```

hdfs-site.xml

```xml

<configuration>
    <property>
        <name>dfs.namenode.name.dir</name>
        <value>file:///home/hadoop/hdfs/name</value>
    </property>
    <property>
        <name>dfs.replication</name>
        <value>2</value>
    </property>
</configuration>
```

格式化 hdfs

```shell
hdfs namenode -format
```

启动 hdfs

```shell
start-dfs.sh
```