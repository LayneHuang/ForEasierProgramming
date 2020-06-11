import * as React from 'react';

export function withSubscription(WrappedComponent, name) {
    // ...并返回另一个组件...
    return class extends React.Component {

        constructor(props) {
            super(props);
            this.handleChange = this.handleChange.bind(this);
            // this.state = {
            //     data: selectData(DataSource, props)
            // };
        }

        componentDidMount() {
            // ...负责订阅相关的操作...
            // DataSource.addChangeListener(this.handleChange);
            console.log(`${name} begin time`, new Date().getTime());
        }

        componentWillUnmount() {
            // DataSource.removeChangeListener(this.handleChange);
            console.log(`${name} end time`, new Date().getTime());
        }

        handleChange() {
            // this.setState({
            //     data: selectData(DataSource, this.props)
            // });
        }

        render() {
            // ... 并使用新数据渲染被包装的组件!
            // 请注意，我们可能还会传递其他属性
            // return <WrappedComponent data={this.state.data} {...this.props} />;
            return <WrappedComponent  {...this.props} />;
        }
    };
}
