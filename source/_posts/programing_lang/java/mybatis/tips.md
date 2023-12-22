---
title: mybatis tips
date: 2021-02-19 14:08:31
categories: [ Java ]
---

[mybatis 官网](https://mybatis.org/mybatis-3/zh/index.html)

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

### 2.枚举类处理器

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
要主要的是在 SQL 语句中不要用双引号包围

```xml

<mapper>
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
</mapper>
```

最后还需要在 mybatis-config.xml 中配置 `<typeHandlers/>`

1. 一个个配置

```xml

<typeHandlers>
    <typeHandler handler="org.mybatis.example.ExampleTypeHandler"/>
</typeHandlers>
```

2. 整包扫描

```xml

<typeHandlers>
    <package name="org.mybatis.example"/>
</typeHandlers>
```

### 3.@Mapper 注解

添加后可以省去扫描包的过程

### 4.Mapper 的 XML 文件路径

一定要放在对应资源目录下，不然扫描不到

### 5.通过标签简化 xml 配置

5.1 `<sql/>` `<include/>`标签  
通过配置此标签，通配多个 select 查询场景。

```xml

<mapper>
    <sql id="base_select">
        id, name, info1, info2, info3
    </sql>

    <select>
        select
        <include refid="base_select"/>
        from table_name
    </select>
</mapper>
```

5.2 `<set/>` 与 `<if/>` 两种标签配合可以处理 update 相关的内容
注意：与 `<where/>`配合时不需要加, `<set/>`加上

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

5.3 `<foreach/>` 处理列表循环  
要注意的是定义了 item, 下方的字段前面也需要加上 item

```xml

<foreach item="item" collection="list" separator=",">
    (#{item.username}, #{item.password}, #{item.email}, #{item.bio})
</foreach>
```

### 6.[批量插入处理](https://www.jianshu.com/p/97e484b55d04)

### 7.XML处理大于小于号

| **原符号** | `>`    | `<`    | `>=`    | `<=`    | `&`     | `'`      | `"`      |
|:--------|:-------|:-------|:--------|:--------|:--------|:---------|:---------|
| **转义后** | `&gt;` | `&lt;` | `&gt;=` | `&lt;=` | `&amp;` | `&apos;` | `&quot;` |

### 8.Common Dependence mapper(.xml) file scan

add `*` after classpath is ok

```yaml
mybatis:
  mapper-locations: classpath:mapper/*.xml
```

```yaml
mybatis:
  mapper-locations: classpath*:mapper/*.xml
```