import React, { ChangeEvent, useState } from 'react';
import NavBar from '../navbar';
import { Button } from '@material-ui/core';
import frontend from '../actions/givers';

function Profile() {
  const [selectedFile, setSelectedFile] = useState<Number[]>([]);
  
  const fileHandler = (event) => {
    event.target.files[0].arrayBuffer().then(function (result){
      setSelectedFile(Array.from(new Int8Array(result)));
    });
    
  };
  
  const editProfile = () => {
    frontend.editProfile("adress",123213,"description","gender",[],"nationality",123123123213,selectedFile);
  }
  
  return (
    <>  
      <br/>
      <br/>
      <br/>

      <input
      accept="image/*"
      style={{ display: 'none' }}
      id="raised-button-file"
      multiple
      type="file"
      onChange={fileHandler}/>

      <label htmlFor="raised-button-file">
        <Button variant="contained" component="span">
          Upload
        </Button>
      </label> 

<Button onClick={editProfile}>Teste</Button>
    </>
  );
}

export default Profile;