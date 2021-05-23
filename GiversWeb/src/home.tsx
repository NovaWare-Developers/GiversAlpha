import React from 'react';
import { Redirect } from 'react-router';
import frontend from './actions/givers';
import NavBar from './navbar';

function Home() {
  if(!frontend.Authenticated()){
    return <Redirect to="/"/>
  }

  return (
    <>  
       Dashboard
    </>
  );
}

export default Home;
