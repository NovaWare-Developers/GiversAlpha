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


import './App.css';

function App() {

  return (
    <>
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
        <Route exact path='/' component={Home}/>
        <Redirect from="*" to='/app' />
      </Switch>
    </BrowserRouter>
    </>
  );
}

export default App;
