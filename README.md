# ForEasyCode
搬砖时知识点的一些汇总

近期读书计划中有《深入React技术栈》，同时在之前的工作中也做过大概几个月的React开发，当时看了官方文档后就参与开发，有些比较好的特性又暂且没有用到。因此在阅读《深入React技术栈》还有根据看过的一些React、Redux相关的资料之后，根据自己的理解对一些常用的知识做一下汇总和分析。持续更新...

##一.基础部分
###1.数据流:
1.state<br/>
2.props(组件间交互,类似参数)，propType:Js是非强类型语言,定义propTypes可以在传入非指定参数时让浏览器给出一个错误提示。<br/>
```
static propTypes = {
	className: React.PropTypes.string,
	activeIndex: React.PropTypes.number,
	onChange: React.PropTypes.fuc,
	children: React.PropTypes.onOfType([
			React.PropTypes.arrayOf(React.PropTypes.node),
			React.PropTypes.node,
	]),
}
```

###2.refs
refs类似对象引用，它指向某个实例。某些组件接收ref，并将其向下传递给子组件。<br/>
React中文官网的一个demo:
```
// FancyButton 使用 React.forwardRef 来获取传递给它的ref，然后转发到它渲染的DOM button中
const FancyButton = React.forwardRef((props, ref) => (
  <button ref={ref} className="FancyButton">
    {props.children}
  </button>
));

// 你可以直接获取 DOM button 的 ref：
const ref = React.createRef();
<FancyButton ref={ref}>Click me!</FancyButton>;
```

##二.样式相关
###1.简化样式设置
React为了提高开发效率，自动对px做添加，不过在多数情况下使用Css Module的方案
###2.使用composes来组合样式
在之前的开发中，我对公用的一些样式定义了一个叫./Global.css的文件，然后个别的样式再单独定义,在使用时类似如下:<br/>
```
/* Global.css */
.common-style {
	width: 10px;
	height: 10px;
}

/* MyStyle.css */
.normal-style {
	width: 10px;
	height: 10px;
}

import "./Global.css";
import "./MyStyle.css";
const btnA = <Button className="common-style"/>
const btnB = <Button className="normal-style"/>
```
可以发现，缺点就是明明normal中有部分与common是重复的,却得重写。<br/>
compose就可以处理:
```
/* Global.css */
.common-style {
	/* 全局样式 */
	width: 10px;
	height: 10px;
}

/* MyStyle.css */
.base {
	/* 通用样式 */
}

.normal-style {
	composes: base;
	composes: $common-style from './Global.css';
	/* 其他样式 */
}
```

##三.组件间通信
###1.父组件向子组件通信: props
###2.子组件向父组件通信: onChange 回调<br/>
###3.跨级组件通信(孙子组件?): context<br/>
前面两个都比较熟悉了,context倒是没用过,而之前是直接props一层层往里面传的,处理起来相对复杂。<br/>
没用用到context:
```
class App extends React.Component {
  render() {
    return <Toolbar theme="dark" />;
  }
}

function Toolbar(props) {
  // Toolbar 组件接受一个额外的“theme”属性，然后传递给 ThemedButton 组件。
  // 如果应用中每一个单独的按钮都需要知道 theme 的值，这会是件很麻烦的事，
  // 因为必须将这个值层层传递所有组件。
  return (
    <div>
      <ThemedButton theme={props.theme} />
    </div>
  );
}

class ThemedButton extends React.Component {
  render() {
    return <Button theme={this.props.theme} />;
  }
}
```
用到context:
```
// Context 可以让我们无须明确地传遍每一个组件，就能将值深入传递进组件树。
// 为当前的 theme 创建一个 context（“light”为默认值）。
const ThemeContext = React.createContext('light');

class App extends React.Component {
  render() {
    // 使用一个 Provider 来将当前的 theme 传递给以下的组件树。
    // 无论多深，任何组件都能读取这个值。
    // 在这个例子中，我们将 “dark” 作为当前的值传递下去。
    return (
      <ThemeContext.Provider value="dark">
        <Toolbar />
      </ThemeContext.Provider>
    );
  }
}

// 中间的组件再也不必指明往下传递 theme 了。
function Toolbar(props) {
  return (
    <div>
      <ThemedButton />
    </div>
  );
}

class ThemedButton extends React.Component {
  // 指定 contextType 读取当前的 theme context。
  // React 会往上找到最近的 theme Provider，然后使用它的值。
  // 在这个例子中，当前的 theme 值为 “dark”。
  static contextType = ThemeContext;
  render() {
    return <Button theme={this.context} />;
  }
}
```
context类似全局变量，大部分情况下貌似不建议使用(给组件带来了外部依赖)，在一些不变的全局信息可以用(用户信息等)。<br/>

####4.没有嵌套关系的组件通信(即是无任何关系的组件?): EventEmitter
借用node.js的Events模块的浏览器版实现
```
// 首先创建一个EventEmitter的单例:
import {EventEmitter} from 'events';
export defulalt new EventEmitter();

// 生产者
emitter.emitEvent('ItemChange',msg);

// 消费者
componentDidMount() {
	this.itemChange = emitter.addListener('ItemChange',(msg) => {
		// do something...
	});
}

componentWillUnmount() {
	 emitter.removeListener(this.itemChange);
}
```

##四.组件间抽象<br/>
###1.高阶组件
高阶组件的用法类似一种代理的效果，对组件的功能进行增强，例如:<br/> 
####1.CommentList 需要订阅 DataSource，用于评论渲染
####2.Blog 需要订阅 DataSource，用于订阅单个blog的帖子
他们就存在共同的行为逻辑(监听，取消监听，对监听事件响应)，设为withSubscription:
```$xslt
// 此函数接收一个组件...
function withSubscription(WrappedComponent, selectData) {
  // ...并返回另一个组件...
  return class extends React.Component {
    constructor(props) {
      super(props);
      this.handleChange = this.handleChange.bind(this);
      this.state = {
        data: selectData(DataSource, props)
      };
    }

    componentDidMount() {
      // ...负责订阅相关的操作...
      DataSource.addChangeListener(this.handleChange);
    }

    componentWillUnmount() {
      DataSource.removeChangeListener(this.handleChange);
    }

    handleChange() {
      this.setState({
        data: selectData(DataSource, this.props)
      });
    }

    render() {
      // ... 并使用新数据渲染被包装的组件!
      // 请注意，我们可能还会传递其他属性
      return <WrappedComponent data={this.state.data} {...this.props} />;
    }
  };
}
```
共同的订阅和取消订阅行为就交由 withSubscription 来处理了
```$xslt
const CommentListWithSubscription = withSubscription(
  CommentList,
  (DataSource) => DataSource.getComments()
);

const BlogPostWithSubscription = withSubscription(
  BlogPost,
  (DataSource, props) => DataSource.getBlogPost(props.id)
);
```
## 五.Redux应用框架
###1.Redux三大原则
####1.单一数据源
一个对象管理，称为store，它实质上是最外层组件的state对象，然后向下传递到各组件的props中
####2.状态是只读的
只有getter，没有setter的意思咯，数据修改就交给dispatch发起
####3.状态修改均由纯函数完成
对于给定的相同输入，输出都是相同的。（Math.rand()这些就不是纯函数了）
###2.Redux基础组成

####1.Action
action实质上就是一个js对象，约定用type字段来表示要执行的操作,如: 
```$xslt
{
    type: DO_SOME_THING, 
    person: someone, 
}
```
####2.Reducer
reducer指定了应用状态的变化，如何响应action并发送到store的。记住action只描述了有事情发生这一事实，并未描述如何更新state。<br/>
在我看来，redux的action更像是一个 object & do_flag ，reducer才是真正执行action的地方。<br/>
```$xslt
// reducer实际上执行的过程
(preState, action) => newState
```
**1.官网文档还提示到有几个要注意到的点:**<br/>
1.1 不要修改传入的参数preState(??)<br/>
1.2 不执行有副作用的函数，如API请求或者路由转跳（那API请求的操作怎么处理呢？）<br/>
1.3 不要调用非纯函数（符合原则）<br/>
1.4 在default的情况下要返回旧的state（符合上式，应该是避免特殊情况，导致store被覆盖）<br/>
```$xslt
// 更加详细的reducer
export default (preState, action) => {
    switch (action.type) {
        case ADD: {
            return {
                ...preState,
                value: preState.value + 1,
            };
        }
        case REDUCE: {
            return {
                ...preState,
                value: preState.value - 1,
            };
        }
        default:
            return preState;
    }
}
```
**2.拆分reducer**<br/>
Redux提供了一个combineReducers()工具类去组合多个reducers,对于不同独立功能reducer就可以放在不同的文件中。
