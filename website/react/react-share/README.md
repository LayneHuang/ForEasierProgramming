# React

[React中文网](https://react.docschina.org/docs/hello-world.html)

界面渲染：条件渲染、 列表 & Key、 表单     
数据流相关：状态提升、 Context、 （熟悉即可，Redux处理方式不同）  
设计思路： 组合 VS 继承、 高阶组件

### 1.条件渲染
使用 JavaScript 运算符 if 或者 条件运算符 去创建元素来表现当前的状态，  
然后让 React 根据它们来更新 UI。

### 2.列表 & Key

### 2.1 渲染多个组件
你可以通过使用 {} 在 JSX 内构建一个元素集合。

### 2.2 Key
key 帮助 React 识别哪些元素改变了，比如被添加或删除。   
因此你应当给数组中的每一个元素赋予一个确定的标识。

(当元素没有确定 id 的时候，万不得已你可以使用元素索引 index 作为 key)

[必须要有 key 的原因](https://react.docschina.org/docs/reconciliation.html)

### 3.表单
在 React 里，HTML 表单元素的工作方式和其他的 DOM 元素有些不同，  
这是因为表单元素通常会**保持一些内部的 state**。  

### 4.状态提升
通常，多个组件需要反映相同的变化数据，这时我们建议将共享状态提升到最近的共同父组件中去。

### 5.Context
Context 提供了一个无需为每层组件手动添加 props，就能在组件树间进行数据传递的方法。  

#### 5.1 组件通信
1.父组件向子组件通信： props  
2.子组件向父组件通信： onChange 回调  
3.跨级组件通信(孙子组件? 相隔2层或以上) ： context  
前面两个都比较熟悉了，context倒是没用过，而之前是直接props（自上而下）一层层往里面传的，处理起来相对复杂。

 