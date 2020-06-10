import React from 'react';
import ToolBar from './tool-bar';
import { ThemeContext } from './context';

class Demo3 extends React.Component {

    render() {
        return (
            <ThemeContext.Provider value="round">
                <ToolBar/>
            </ThemeContext.Provider>
        );
    }
}

export default Demo3;