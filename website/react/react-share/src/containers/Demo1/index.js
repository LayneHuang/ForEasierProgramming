import { Button } from 'antd';
import React from 'react';

class Demo1 extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            show: true
        };
        this.onClick = this.onClick.bind(this);
    }

    onClick(e) {
        console.log(e);
        this.setState({ show: !this.state.show });
    };

    render() {
        const valueNull = null;
        const valueUndefine = undefined;
        const valueTrue = true;
        const valueFalse = false;
        const obj = {};

        return (
            <>
                <Button onClick={this.onClick}>按钮</Button>
                {this.state.show ? <div>状态...A</div> : <div>状态...B</div>}
                {valueNull && <div>当值为 null 时...</div>}
                {valueUndefine && <div>当值为 undefine 时...</div>}
                {valueTrue && <div>当值为 true 时...</div>}
                {valueFalse && <div>当值为 false 时...</div>}
                {obj && <div>当值为 空对象 时...</div>}
            </>
        );
    }
}

export default Demo1;