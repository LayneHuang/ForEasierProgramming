import { Button } from 'antd';
import React from 'react';
import Test1 from './test1';
import Test2 from './test2';

class Demo5 extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            list: [<Test1 key='test1'/>, <Test2 key='test2'/>]
        };
        this.handleClick = this.handleClick.bind(this);
    }

    handleClick(e) {
        const list = this.state.list;
        const newList = [list[0]];
        this.setState({ list: newList });
    }

    render() {
        return (
            <>
                {this.state.list}
                <Button onClick={this.handleClick}> remove </Button>
            </>
        );
    }
}

export default Demo5;