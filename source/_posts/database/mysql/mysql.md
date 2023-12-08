---
title: MySQL
date: 2021-03-16 20:21:31
categories: [ Database ]
---

# 1.Query Optimization

### Efficiency between = and != in inner joins SQL queries

In some business data queries, we always need to consider inner joins [Company or Organization] table.
And we always set org status to an integer value[to distinguish enable 0 , freeze 1, or more ...].
but most status are in enable status.

Common case:

```sql
select * form table_a a inner join org_info org on a.org_id = b.org_id and b.org_status = 0 
```

Better case (Chat Gpt Explain):
(Because, the dataset of not 0 status is much smaller than that of 0)

```sql
select * form table_a a inner join org_info org on a.org_id = b.org_id and b.org_status != 1 and b.org_status != 2 
```

# 2.Tips

### Deployment with Docker

{% link '参考' https://www.cnblogs.com/smallmin/p/11582954.html [title] %}

```shell script
docker pull mysql:8.0.16
```

```shell
docker rm -f mysql8
docker run -p 3306:3306 --name=mysql8 \
-e MYSQL_ROOT_PASSWORD=123456 \
--mount type=bind,src=/home/mysql/conf/my.cnf,dst=/etc/mysql/my.cnf \
--mount type=bind,src=/home/mysql/datadir,dst=/var/lib/mysql \
--restart=on-failure:3 \
-d mysql:8.0.16 --lower-case-table-names=2
```

### Parameters

innodb_thread_concurrency 控制并发线程上限

### Common Queries

```mysql
# 查询状态, LATESTDETECTED DEADLOCK 具有死锁记录
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

### binlog backup days

don't forget to config binlog backup days when you are private deployment. otherwise, your disk will full soon.
(/etc/my.cnf)

```conf
expire_logs_days=30
max_binlog_size=1024M
```

