import { Button } from 'antd';
import React from 'react';
import FancyButton from '../FancyButton';

class Demo4 extends React.Component {
    constructor(props) {
        super(props);
        this.showRef = this.showRef.bind(this);
        this.state = {
            myRef: React.createRef()
        };
    }

    showRef() {
        console.log(this.state.myRef);
    }

    render() {
        const ref = this.state.myRef;
        return (
            <>
                <FancyButton ref={ref}>Upper Button</FancyButton>
                <Button onClick={this.showRef}>show ref</Button>
            </>
        );
    }
}

export default Demo4;