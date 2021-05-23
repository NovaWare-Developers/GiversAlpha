import React from 'react';
import { Button } from '@material-ui/core';
import { Redirect, useHistory } from 'react-router';
import frontend from './actions/givers';

function NavBar() {
    let history = useHistory();
  if(!frontend.Authenticated()){
    return <Redirect to="/"/>
  }
   
  function logout(){
    frontend.logOut().then(function(){
        history.push('/');
    });
  }

  return (
    <>  
        <Button onClick={() => {history.push('/tops')}}>Tops</Button>
        <Button onClick={() => {history.push('/maps')}}>Maps</Button>
        <Button onClick={() => {history.push('/list')}}>List</Button>
        <Button onClick={() => {history.push('/marketplace')}}>MarketPlace</Button>
        <Button onClick={() => {history.push('/profile')}}>Profile</Button>
        <Button onClick={logout}>LogOut</Button>
    </>
  );
}

export default NavBar;