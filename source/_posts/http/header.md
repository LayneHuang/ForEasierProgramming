---
title: Http Header
date: 2024-04-07 00:00:00
categories: [ Http ]
---

### 1.Show file in browser

when we need to just show some file in browser(not download it as file)

Example: html file

```
Content-Type: text/html
Content-Disposition: inline
```

the label `Content-Type` can not be `application/octet-stream`,
that make `Content-Disposition` invalid, and download the file.

