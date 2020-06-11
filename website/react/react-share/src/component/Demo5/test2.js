import { Button } from 'antd';
import * as React from 'react';
import { withSubscription } from './subscription';

class Test2 extends React.Component {

    componentDidMount() {
        this.init();
    }

    init = function () {
        let a = 0;
        for (let i = 1; i < 1000; ++i) {
            a += i;
        }
        console.log('test2 cal :', a);
    };

    render() {
        return (
            <Button>Button2</Button>
        );
    }
}

export default withSubscription(Test2, 'Test2');
