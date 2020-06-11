import { Button } from 'antd';
import * as React from 'react';
import { withSubscription } from './subscription';

class Test1 extends React.Component {

    render() {
        return (
            <Button>Button1</Button>
        );
    }
}

export default withSubscription(Test1, 'Test1');
