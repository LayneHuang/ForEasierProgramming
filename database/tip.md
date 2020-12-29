# 数据库小知识
数据库语句基本喜欢用大写, 下划线分割的形式

### 1. CASE WHEN
```sql
SELECT CASE WHEN TABLE.ID = 1 THEN 2 WHEN TABLE.ID = 2 THEN 5 ELSE TABLE.ID + 1 END TABLE_ID FROM TABLE
```
1.使用 END 结束 CASE WHEN 语句, 后面加上字段别名
