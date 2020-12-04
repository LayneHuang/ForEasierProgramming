# MyBatis 小知识

### 1.数据增量更新
https://www.cnblogs.com/wx60079/p/13201456.html  
通过 if 条件去增量更新  
```xml
<update id="functionName" parameterType="JavaBean">
    UPDATE DATABASE_TABLE_NAME SET
    <if test="name != null">ROW_NAME = #{name},</if>
    ROW_ID = #{id} 
    WHERE ROW_ID = #{id}
</update>
```

### 2.ON DUPLICATE KEY UPDATE
这个是 MySQL 特有的语法吗？

### 3.枚举类处理器
```java
public class EnumHandler extends BaseTypeHandler<RunResult> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, RunResult runResult, JdbcType jdbcType)
        throws SQLException {
    }

    @Override
    public RunResult getNullableResult(ResultSet rs, String columnName) throws SQLException {
    }

    @Override
    public RunResult getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    }

    @Override
    public RunResult getNullableResult(CallableStatement cs, int i) throws SQLException {
    }
}
```
