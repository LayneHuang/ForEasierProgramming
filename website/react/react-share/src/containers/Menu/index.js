import Tabs from 'antd/es/tabs';
import React from 'react';
import Calculator from '../Calculator';
import Demo1 from '../Demo1';
import Demo3 from '../Demo3';
import NameForm from '../NameForm';

const { TabPane } = Tabs;

class Menu extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            tabs: []
        };
    }

    componentDidMount() {
        this.initTabs();
    }

    initTabs = function () {
        // const componentList = [];
        // const componentList = null;
        const componentList = [
            {
                component: <Demo1/>, title: '条件渲染'
            }, {
                component: '以 Tabs 页面为例子', title: '列表 & Key'
            }, {
                component: <NameForm/>, title: '表单'
            }, {
                component: <Calculator/>, title: '状态提升'
            }, {
                component: <Demo3/>, title: 'Context'
            }
        ];
        let tabs = [];
        componentList.forEach((v, k) => {
            // tabs.push(<TabPane tab={`Share Demo(${v.title})`}>{v.component}</TabPane>);
            tabs.push(<TabPane key={`menuTab${k}`} tab={`Share Demo(${v.title})`}>{v.component}</TabPane>);
        });
        this.setState({ tabs: tabs });
    };

    render() {
        return (
            <div style={{ marginLeft: 10, marginRight: 10 }}>
                <Tabs>
                    {this.state.tabs}
                </Tabs>
            </div>
        );
    }
}

export default Menu;