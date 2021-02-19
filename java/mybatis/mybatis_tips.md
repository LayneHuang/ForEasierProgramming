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
```mysql
INSERT INTO TABLE_NAME(KEY, COLUMN1, COLUMN2)
VALUES (#{key}, #{column1}, #{column2})
ON DUPLICATE KEY UPDATE COLUMN1 = #{column1},
                        COLUMN2 = #{column2};
```
这个是 MySQL 特有的语法吗？

### 3.枚举类处理器
```java
public class EnumHandler extends BaseTypeHandler<MyEnum> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int columnIndex, MyEnum myEnum, JdbcType jdbcType)
        throws SQLException {
    }

    @Override
    public MyEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
    }

    @Override
    public MyEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    }

    @Override
    public MyEnum getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    }
}
```
在 XML 的用法，不管是查询在 resultMap 中，还是插入时都要加上
```xml
<resultMap id="MyObject" type="MyObject">
    <result column="MY_ENUM" property="myEnum"
            javaType="EnumHandler"
            typeHandler="EnumHandlerClassPath"/>
</resultMap>

<insert id="saveMyObject" parameterType="MyObject">
    INSERT INTO TABLE_NAME(ID, MY_ENUM)
    VALUES (#{id},
            #{myEnum,typeHandler=EnumHandlerClassPath})
</insert>

<select id="getMyObject" parameterType="int" resultMap="MyObject">
    SELECT ID, MY_ENUM FROM TABLE_NAME WHERE ID = #{id}
</select>
```

### 4.@Mapper 注解
添加后可以省去扫描包的过程

### 5.Mapper 的 XML 文件路径
一定要放在对应资源目录下，不然扫描不到

### 6.通过标签简化 xml 配置

6.1 `<sql/>` `<include/>`标签  
通过配置此标签，通配多个 select 查询场景。
```xml
<mapper>
    <sql id="base_select">
        id, name, info1, info2, info3
    </sql>

    <select>
        select <include refid="base_select" /> from table_name
    </select>
</mapper>
```

6.2 `<set/>`与`<if/>`两种标签配合可以处理 update 相关的内容
```xml
<update>
    UPDATE TABLE_NAME
    <set>
        <if test="name != null">
            NAME = #{name},
        </if>
    </set>
    <where>
        ID = #{id}
    </where>
</update>
```

### 7.批量插入处理
https://www.jianshu.com/p/97e484b55d04
