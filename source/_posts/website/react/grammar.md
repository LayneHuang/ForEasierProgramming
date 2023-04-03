---
title: 语法
date: 2021-02-23 11:26:31
categories: [ js ]
---

### 1.object 大括号内...的用法

```javascript
let a = {id: 888, gg: 'haha'};
let b = {...a, id: 1000};
let c = {id: 1000, ...a};
```

由于顺序的不同，{} 内前面的参数会被后面的覆盖。  
所以最后 b = { id: 1000 , gg : 'haha'}  
c 跟 a 是一样的。

### 2.idea ESLint 配置

https://www.shangmayuan.com/a/dc565108debc46b89050f3af.html