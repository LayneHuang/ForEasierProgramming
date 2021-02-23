---
title: dotenv开发环境控制
date: 2021-02-23 11:26:31
categories: Website
---

[dotenv github address](https://github.com/mrsteele/dotenv-webpack) <br/>
说明: 在打包过程中进行变量的预替代，能够以命令的形式来对不同环境做打包

### 1.webpack & dotenv的依赖
```shell script
// webpack 插件以来自行查找
npm install dotenv-webpack --save-dev
```
result:
```html
"devDependencies": {
    "dotenv-webpack": "^1.7.0",
    "webpack": "^3.7.1",
    "webpack-dev-server": "^2.9.1",
    "... and wepack plugins dependencies(file loader and more)"
}
```

### 2.webpack.config.js的设置
```html
const Dotenv = require('dotenv-webpack');

module.exports = {
    // some module
    //.....
    plugins: [
        //... other plugins
        //...
        new Dotenv({
            path: './env/.env', // load this now instead of the ones in '.env'
            safe: true, // load '.env.example' to verify the '.env' variables are all set. Can also be a string to a different file.
           systemvars: true, // load all the predefined 'process.env' variables which will trump anything local per dotenv specs.
           silent: true, // hide any errors
           defaults: false // load '.env.defaults' as the default values if empty.
       })
    ],
    //.....
}
```

### 3.添加环境配置文件 .env
```properties
VERSION=1.0.0
API_URL=XXXXXXX
```

### 4.在代码中使用(以process.env.XXX的方式)
```html
static getApiUrl() {
    switch (process.env.NODE_ENV) {
        case 'prod':
            return process.env.API_URL_PROD;
        case 'adhoc':
            return process.env.API_URL_ADHOC;
        default:
            return process.env.API_URL_DEV;
    }
}
```

### 5. cross-env的使用(能够在package.json修改process.env.XXX)
```shell script
npm i --save-dev cross-env
```
    
    
    
### 6.在webpack.config.js引用process.env.XXX变量
```html
const env = process.env.NODE_ENV;
module.exports = {
   entry: [
        // ...
    ],
    // Distinguish the output file name by env
    output: {
        path: path.join(__dirname, `./dist_${env}/`),
    }
}
```

### 7. 修改package.json文件
```json
{
    "scripts": {
      "build:dev": "cross-env NODE_ENV=dev webpack --config ./webpack.config.js --progress --profile --colors",
      "build:adhoc": "cross-env NODE_ENV=adhoc webpack --config ./webpack.config.js --progress --profile --colors",
      "build:prod": "cross-env NODE_ENV=prod webpack --config ./webpack.config.js --progress --profile --colors"
    }
}
```
    
    
