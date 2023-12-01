---
title: fastjson
date: 2023-09-19 23:00:00
categories: [ Java, fastjson ]
---

### JSON 对于数组的转换

```
List<String> list = JSON.parseObject(v, new TypeReference<List<String>>(){})
List<String> list = JSON.parseArray(str, String.class);
```

在进行 DTO -> Bean 之间的转换时, 如果对象包含子对象或者数组的情况下,
BeanUtils 仅仅是一个浅拷贝, 这个时候可以用 fastjson 做一下对象拷贝

### 对象中空值转换

fastjson 通常把空值直接忽略

比如

```java
public class User {
    private String name;

    // @JSONField(serializeFeatures = JSONWriter.Feature.WriteMapNullValue)
    private Object obj;
}
```

如果 name 设置成 "abc"
如果 obj 设置成 null

转换后即变成

```json
{
  "name": "abc"
}
```

使用注解 `@JSONField(serializeFeatures = JSONWriter.Feature.WriteMapNullValue)` 可以将其转换为:

```json
{
  "name": "abc",
  "obj": null
}
```

如果需要转换为一下格式，则需要自己实现 ObjectSerializer

```json
{
  "name": "abc",
  "obj": {}
}
```

chatGPT tips:

```java
public class EmptyObjectSerializer implements ObjectSerializer {
    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerializeWriter out = serializer.out;
        if (object == null) {
            out.write("{}");
            return;
        }
        serializer.write(object);
    }
}
```

### 泛型如 ? extend TreeNode 这种字段在反序列化时怎么处理才不丢失字段

