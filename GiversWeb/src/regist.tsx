import React, { ChangeEvent, useState } from 'react';
import { Redirect } from 'react-router';
import frontend from './actions/givers.js';

function Regist() {
    const BACKURL = 'http://givers-volunteering.appspot.com/rest';
    const RESTREGISTER = '/register/user';

    const [user, setUser] = useState("");
    const [email, setEmail] = useState("");
    const [pass, setPassword] = useState("");
    const [passConf, setPasswordConf] = useState("");
    
    function userHandler(e:ChangeEvent<HTMLInputElement>){
      setUser(e.target.value);
    }
    function emailHandler(e:ChangeEvent<HTMLInputElement>){
      setEmail(e.target.value);
    }
    function passwordHandler(e:ChangeEvent<HTMLInputElement>){
      setPassword(e.target.value);
    }
    function passwordConfHandler(e:ChangeEvent<HTMLInputElement>){
      setPasswordConf(e.target.value);
    }
    
  
    function handleLogin(){
        fetch(BACKURL+RESTREGISTER, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify( {
                username : user,
                password : pass,
                passwordConfirm : passConf,
                email : email
            })
        })
        .then(function(response) {
            return response.text();
        })
        .then(function(text) {
            alert(text);
            if(text.includes('successfully')){
             //   location.reload();
            }
        });
    }

    if(frontend.Authenticated()){
        return <Redirect to="/"/>
    }

  return (
    <>
    <input type={"text"} placeholder="Username" value={user} onChange={userHandler}/>
    <input type={"email"} placeholder="Email" value={email} onChange={emailHandler}/>
    <input type={"password"} placeholder="Password" value={pass} onChange={passwordHandler}/>
    <input type={"password"} placeholder="Confirm Password" value={passConf} onChange={passwordConfHandler}/>
    <button onClick={handleLogin}>Entrar</button>
    </>
  );
}

export default Regist;
