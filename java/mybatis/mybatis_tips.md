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