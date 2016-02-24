require("./styl/index");

import ReactDom from 'react-dom';
import React from 'react';
import injectTapEventPlugin from 'react-tap-event-plugin';
import RaisedButton from 'material-ui/lib/raised-button';

injectTapEventPlugin();

ReactDom.render(
    <div id="react_root">
        <RaisedButton label="Hello" />
    </div>,
document.getElementById("container"));