import React from 'react'
import RaisedButton from 'material-ui/lib/raised-button';
import { Link } from 'react-router'



class App extends React.Component {
    constructor() {
        super();
    }

    static defaultProps = {
        name: "world"
    };

    render = () => {
        return <div>
            <RaisedButton label="Hello" />
            <Link to="/login">Login</Link>
        </div>
    };
}
export default App;