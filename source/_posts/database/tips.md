---
title: 数据库小Tips
date: 2020-12-29 11:03:31
categories: Database
---

数据库语句基本喜欢用大写, 下划线分割的形式

### CASE WHEN
```sql
SELECT 
    CASE WHEN TABLE.ID = 1 THEN 2 
    WHEN TABLE.ID = 2 THEN 5 
    ELSE TABLE.ID + 1 END
FROM TABLE
```
1.使用 END 结束 CASE WHEN 语句, 后面加上字段别名

### 利用 CASE WHEN 进行分情况排序
```sql
SELECT ID, NAME FROM TANLE_NAME 
ORDER BY CASE WHEN ID <= 100 THEN ID END ASC,
         CASE WHEN ID > 100 THEN NAME END DESC;
```

### ON DUPLICATE KEY UPDATE
```sql
INSERT INTO TABLE_NAME(KEY, COLUMN1, COLUMN2)
VALUES (#{key}, #{column1}, #{column2})
ON DUPLICATE KEY UPDATE COLUMN1 = #{column1},
                        COLUMN2 = #{column2};
```