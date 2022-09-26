---
title: 文件管理
date: 2022-9-23 21:00:00
categories: Business
---
### 当前构建方案
1.将自动生成及文件管理分成了2个文件夹(操作数据在同一个文件夹不知道会不会写并发很高?)

### 文件上传大小限制
Spring Boot 微服务及 nginx 都需要配置上传文件大小限制参数

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 1000MB
```

```config
http {
    client_max_body_size 500m;
    proxy_max_temp_file_size 1024m;
}
```

### 文件分类
在文件上传后，涉及对文件类型进行分类

```java
public enum FileType {
    // 文件类型
    UNKNOWN(0,  ".*", ".*", "未知"),
    IMAGE(1, "image/(png|jpg|jpeg|svg)", ".*\\.(png|jpg|jpeg|svg)", "图片"),
    VIDEO(2, ".*", ".*\\.(mp4|mp5)", "视频"),
    CONFIG(3, ".*", ".*\\.(xml|yaml)", "配置"),
    COMPRESSED(4, "application/(zip|rar|tar)", ".*\\.(zip|rar|tar)", "普通压缩包"),
    TXT(5, "(application/.*sheet|text/(plain|markdown))", ".*\\.(txt|md|xlsx)", "普通文本"),
    EXE(6, "application/.*program", ".*\\.(exe)", "普通安装包"),
    ;

    @Getter
    private final int code;

    @Getter
    private final String pattern;

    @Getter
    private final String suffix;

    @Getter
    private final String msg;

    FileType(int code, String pattern, String suffix, String msg) {
        this.code = code;
        this.pattern = pattern;
        this.suffix = suffix;
        this.msg = msg;
    }

    public static FileType getBySuffix(String filename) {
        if (!StringUtils.hasText(filename)) {
            return UNKNOWN;
        }
        // 从后向前匹配
        List<FileType> types = new ArrayList<>(Arrays.asList(values()));
        Collections.reverse(types);
        for (FileType type : types) {
            if (Pattern.matches(type.suffix, filename.toLowerCase(Locale.ROOT))) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public static FileType getByCode(Integer code) {
        if (code == null) return UNKNOWN;
        for (FileType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public boolean checkPattern(String contentType) {
        return Pattern.matches(pattern, contentType);
    }
}
```

###