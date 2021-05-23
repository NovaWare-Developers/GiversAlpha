import React, { useState } from 'react';
import { useHistory, Redirect } from 'react-router-dom';
import frontend from '../actions/givers';

import NavBar from '../navbar';
import Top from '../components/topCard';

function Profile() {

  const [topusers, setTopUsers] = useState([
    {
      username: 'mikas',
      points: '145',
    },
    {
      username: 'mikas',
      points: '123',
    },
    {
      username: 'mikas',
      points: '100',
    },  {
      username: 'mikas',
      points: '100',
    },  {
      username: 'mikas',
      points: '100',
    },
    

  ]);

  if(!frontend.Authenticated()){
    return <Redirect to="/"/>
  }
   
  return (
    <>  
      {topusers.map(function(u,index){
            return (
              <Top position={index+1} username={u.username} points={u.points} key={index}/>
            )
 })} 
    </>
  );
}

export default Profile;