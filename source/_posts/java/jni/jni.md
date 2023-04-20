---
title: JNI
date: 2023-04-20 21:00:00
categories: [ Java ]
---

### Load Native Interface in Linux

```java

public static class Loader {
    static {
        System.setProperty("java.awt.headless", "false");
        String osName = System.getProperties().getProperty("os.name");
        try {
            if (!osName.toLowerCase(Locale.ROOT).contains("win")) {
                // in
                Resource resource = new ClassPathResource("lib/xxx.so");
                // out
                String path = System.getProperty("java.io.tmpdir") + "lib" + File.separator;
                File folder = new File(path);
                folder.mkdirs();
                File libFile = new File(folder, "xxx.so");
                try (InputStream in = resource.getInputStream();
                     FileOutputStream fOut = new FileOutputStream(libFile)) {
                    FileCopyUtils.copy(in, fOut);
                }
                System.load(libFile.getAbsolutePath());
            }
        } catch (Throwable e) {
            log.error("native interface load failure: {}", e.getMessage());
        }
    }
}
```