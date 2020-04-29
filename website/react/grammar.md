# 语法小细节

### 1.object 大括号内...的用法
```javascript
let a = { id:888, gg:'haha'};
let b = {...a, id: 1000};
let c = {id: 1000, ...a};
```
由于顺序的不同，{} 内前面的参数会被后面的覆盖。  
所以最后 b = { id: 1000 , gg : 'haha'}  
c 跟 a 是一样的。
