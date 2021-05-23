import React, { useState } from 'react';
import { useHistory, Redirect } from 'react-router-dom';
import frontend from '../actions/givers';

import NavBar from '../navbar';
import Item from '../components/itemCard';

function Profile() {

  const [topusers, setTopUsers] = useState([
    {
      image: 'test image',
      title: 'Bus',
      description: 'Bus Lift 150km',
      disabled: false,
      cost: '150Points',
    },   
    {
      image: 'test image',
      title: 'Bus',
      description: 'Bus Lift 150km',
      disabled: false,
      cost: '150Points',
    },   

  ]);

  if(!frontend.Authenticated()){
    return <Redirect to="/"/>
  }
  

  return (
    <> 
      {topusers.map(function(i){
            return (
              <Item image={i.image} title={i.title} description={i.description} disabled={i.disabled} cost={i.cost}/>
            )
      })} 
    </>
  );
}

export default Profile;