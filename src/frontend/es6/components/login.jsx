import React from 'react'
import TextField from 'material-ui/lib/text-field';
import RaisedButton from 'material-ui/lib/raised-button';
import {Link} from 'react-router'


class Login extends React.Component {
    login = () => {
        alert("Logging in...")
    };

    render = () => {
        return <div>
            <TextField
                hintText="Login"
            />
            <TextField
                hintText="Password"
                type="password"
            />
            <RaisedButton
                label="Login"
                primary={true}
                onClick={this.login}/>
            <Link to="/">
                <RaisedButton
                    label="Back"/>
            </Link>
        </div>
    };
}
export default Login