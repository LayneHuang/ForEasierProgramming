import Button from 'antd/es/button';
import * as React from 'react';
import { ThemeContext } from './context';

class ThemedButton extends React.Component {

    static contextType = ThemeContext;

    render() {
        console.log(this.context);
        return <Button shape={this.context}>这是一个Button</Button>;
    }
}

export default ThemedButton;
