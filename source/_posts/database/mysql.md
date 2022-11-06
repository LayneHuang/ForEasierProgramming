---
title: MySQL
date: 2021-03-16 20:21:31
categories: Database
---

# Tips

### docker 部署
```shell script
docker pull mysql:8.0.18
```

### 参数
innodb_thread_concurrency 控制并发线程上限

### 常用查询
```mysql
// 查询状态, LATESTDETECTED DEADLOCK 具有死锁记录
show engine innodb status
```

### 分组最值查询
业务上遇到一个分组(A字段)，并且需要二级排序(B,ID字段)取最值的情况。  
最后需要通过 distinct + max 函数 group by 后，配合子查询完成
```roomsql
SELECT T1.A, T1.B, T1.ID, T1.D FROM TABLE_NAME T1, (
    SELECT DISTINCT(T2.A), MAX(T2.ID) AS ID FROM TABLE_NAME T2, (
        SELECT DISTINCT(A), MAX(B) AS B FROM TABLE_NAME WHERE T1.D = 'd'
        GROUP BY A
    ) T3 WHERE T2.A = T3.A AND T2.B = T3.B
) T4
WHERE T1.ID = T4.ID
```

### 统一数据库时区
后端用 timestamp 插入后, 数据库数据时间显示依然不一致
{% link '统一时区' https://blog.csdn.net/weixin_43824829/article/details/124174247 %}
