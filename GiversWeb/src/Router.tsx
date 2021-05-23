import React from 'react';
import {BrowserRouter, Route, Switch, Redirect} from 'react-router-dom';
import frontend from './actions/givers.js';
import Login from './login';
import Regist from './regist';
import Home from './home';

import Tops from './pages/tops';
import Maps from './pages/maps';
import List from './pages/list';
import Marketplace from './pages/marketplace';
import Profile from './pages/profile';

import LandPage from './landingPage';

import DrawerTeste from './components/drawer';
import EditProfileTeste from './pages/editprofiletest';
import ProfileTeste from './pages/profileTest/profiletest';
import App from './Layouts/app';

import './App.css';
import { createMuiTheme, MuiThemeProvider } from '@material-ui/core';

function Router() {
  const [darkMode,setDrarkMode] = React.useState(true);

  const darkTheme = createMuiTheme({
    palette:{
      primary: {
        main: '#0060ff',
      },
  
      secondary:{
        main: '#00d8ff',
      },
      type:'dark',
    }
  });
  const lightTheme = createMuiTheme({
    palette:{
      primary: {
        main: '#0060ff',
      },
  
      secondary:{
        main: '#00d8ff',
      },
    }
  });
  console.log(darkTheme);
  return (
    <>
    <MuiThemeProvider theme={darkMode? darkTheme:lightTheme}>
      <BrowserRouter>
        <Switch>
          <Route exact path='/home' component={Home}/>
          <Route path='/regist' component={Regist}/>
          <Route path='/login' component={Login}/>
          <Route path='/tops' component={Tops}/>
          <Route path='/maps' component={Maps}/>
          <Route path='/list' component={List}/>
          <Route path='/marketplace' component={Marketplace}/>
          <Route path='/profile' component={Profile}/>
          <Route exact path='/' component={LandPage}/>
          <Route exact path='/teste/drawer' component={DrawerTeste}/>
          <Route exact path='/teste/editprofile' component={EditProfileTeste}/>
          <Route path='/teste/profile' component={ProfileTeste}/>
          <Route path='/app' component={App}/>
          <Redirect from="*" to={'/app'} />
        </Switch>
      </BrowserRouter>
    </MuiThemeProvider>
    </>
  );
}

export default Router;
