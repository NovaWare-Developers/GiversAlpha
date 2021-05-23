import React from 'react';
import { Redirect} from 'react-router-dom'
import './Login.css';
import frontend from './actions/givers.js';
import SignIn from './loginForm';
import { Paper } from '@material-ui/core';

function Login() {

    if(frontend.Authenticated()){
        return <Redirect to="/"/>
    }

  return (
    <>
    <Paper style={{width:'500px',margin:'auto',borderRadius:'10px'}}>
        <SignIn/>
    </Paper>
    </>
  );
}

export default Login;