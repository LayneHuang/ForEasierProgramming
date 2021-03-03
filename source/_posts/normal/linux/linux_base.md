---
title: Linux 常用命令
date: 2020-06-11 17:58:00
tags: tips
---
# 

### 1.日志抓取
```shell script
tail -f --line=50 filename| grep "key word"
```
### 2.查找当前目录下文件
```shell script
find . -name "*libc*"
```

### 3.查看当前cpu占用
```shell script
top -[dbnp]
```
-d: 秒数，显示页面刷新间隔  
-b: 以批次的方式执行 top  
-n: 与 -b 配合，表示执行多少次  
-p: 指定线程号
-H: thread 模式

### 4.查看磁盘IO状态
```shell script
iostat
```