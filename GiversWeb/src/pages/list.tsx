import React from 'react';
import { Redirect } from 'react-router-dom';
import frontend from '../actions/givers';
import NavBar from '../navbar';

import ScrollList from '../components/scrollList'

function Profile() {

  if(!frontend.Authenticated()){
    return <Redirect to="/"/>
  }

  return (
    <>  
      <ScrollList/>
    </>
  );
}

export default Profile;