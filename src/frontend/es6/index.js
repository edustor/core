require("./styl/index");

import ReactDom from 'react-dom';
import React from 'react';
import injectTapEventPlugin from 'react-tap-event-plugin';
import {Router, Route, browserHistory} from 'react-router'

import App from "./components/app"
import Login from "./components/login"

injectTapEventPlugin();

ReactDom.render(
    <Router history={browserHistory}>
        <Route path="/" component={App}/>
        <Route path="/login" component={Login}/>
    </Router>,
    document.getElementById("container"));