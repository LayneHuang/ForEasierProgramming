---
title: Ace Editor 前端编辑器
date: 2021-02-23 11:26:31
categories: Website
---

在项目中涉及了 json 的实时编辑。  
而 ace 就是一个很不错的编辑器（提供了不仅仅只有 json , 还有 java, html, python 等等的语法）
因为我接入的项目是 react, 所以用的是 [react-ace](https://github.com/securingsincity/react-ace) 这个组件。  

## Syntax validation 语法检测
接入 react-ace 之后我发现了**语法检测有问题**，不能给出提示。  
而 ace 上是有这个功能的。  
解决方法：  
在 index.html 中引入 cdn 提供的.js
```html
<body>
    <script src="https://cdn.bootcss.com/ace/1.4.6/ace.js"></script>
    <script src="https://cdn.bootcss.com/ace/1.4.6/ext-beautify.js"></script>
    <script src="https://cdn.bootcss.com/ace/1.4.6/ext-language_tools.js"></script>
    <script src="https://cdn.bootcss.com/ace/1.4.6/mode-javascript.js"></script>
    <script src="https://cdn.bootcss.com/ace/1.4.6/theme-xcode.js"></script>
    <div id="root"></div>
</body>
```