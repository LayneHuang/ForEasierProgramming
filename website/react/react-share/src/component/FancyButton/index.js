import React from 'react';

const FancyButton = React.forwardRef((props, ref) => (
    <button ref={ref} className="UnderButton">
        {props.children}
    </button>
));

export default FancyButton;