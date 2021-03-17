---
title: MySQL
date: 2021-03-16 20:21:31
categories: Database
---

# Tips

### 参数
innodb_thread_concurrency 控制并发线程上限

### 常用查询
```mysql
// 查询状态, LATESTDETECTED DEADLOCK 具有死锁记录
show engine innodb status
```