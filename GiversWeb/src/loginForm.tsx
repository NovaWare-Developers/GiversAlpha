import React, { ChangeEvent, useState } from 'react';
import { useHistory } from 'react-router-dom'
import Button from '@material-ui/core/Button';
import CssBaseline from '@material-ui/core/CssBaseline';
import TextField from '@material-ui/core/TextField';
import Link from '@material-ui/core/Link';
import Grid from '@material-ui/core/Grid';
import Box from '@material-ui/core/Box';
import { makeStyles } from '@material-ui/core/styles';
import Container from '@material-ui/core/Container';
import Copyright from './copyright'
import Alert from './components/alert';

import frontend from './actions/givers.js';
import { CircularProgress } from '@material-ui/core';

const useStyles = makeStyles((theme) => ({
  paper: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
  },
  avatar: {
    margin: theme.spacing(1),
    backgroundColor: theme.palette.secondary.main,
  },
  form: {
    width: '100%', // Fix IE 11 issue.
    marginTop: theme.spacing(1),
  },
  submit: {
    margin: theme.spacing(3, 0, 2),
  },
}));

export default function SignIn() {
  const classes = useStyles();
  let history = useHistory();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);


  const [alert, setAlert] = useState({
    description: "",
    severity:"",
  });
  const [open, setOpen] = useState(false);
  
  function emailHandler(e:ChangeEvent<HTMLInputElement>){
    setEmail(e.target.value);
  }
  function passwordHandler(e:ChangeEvent<HTMLInputElement>){
    setPassword(e.target.value);
  }
    
  function handleKeyPressSubmit(e){
    if(e.charCode === 13) {
      handleLogin();
    }
  }
    
  function LogButton(){
    if(!loading){
      return <>Entrar</>;
    }else
    return <CircularProgress size={24} variant="indeterminate" color={"inherit"}/>;
  }

  function handleLogin(){
    if (email == "" || password == "") {
      setAlert({
        description: "Preencha todos os campos",
        severity:"warning",
      });
      setOpen(true);
    } else {
      setLoading(true);
        frontend.login(email,password).then(function(success){
            if(success[0]) history.push('/app');
            else {
              setAlert({
                description: success[1].toString(),
                severity:"error",
              });
              setOpen(true);
              setLoading(false);
            }
        });
    }
  }

  return (
    <Container component="main" maxWidth="xs">
      <CssBaseline />
      <div className={classes.paper}>
        <form>
          <TextField
            variant="outlined"
            margin="normal"
            fullWidth
            id="username"
            label="Username"
            name="username"
            autoComplete="username"
            autoFocus
            value={email}
            onChange={emailHandler}
            onKeyPress={handleKeyPressSubmit}
          />
          <TextField
            variant="outlined"
            margin="normal"
            fullWidth
            name="password"
            label="Password"
            type="password"
            id="password"
            autoComplete="current-password"
            value={password}
            onChange={passwordHandler}
            onKeyPress={handleKeyPressSubmit}
          />
         
          <Button
            fullWidth
            variant="contained"
            color="primary"
            className={classes.submit}
            onClick={handleLogin}
            disabled={loading}
          >
            <LogButton/>
          </Button>
          <Grid container>
            <Grid item xs>
              <Link href="#" variant="body2">
                Esqueci-me da password
              </Link>
            </Grid>
            <Grid item>
              <Link href="/regist" variant="body2">
                {"Não tenho uma conta"}
              </Link>
            </Grid>
          </Grid>
        </form>
      </div>
      <Box mt={8}>
        <Copyright />
      </Box>
      <Alert description={alert.description} severity={alert.severity} open={open} setOpen={setOpen}/>
      <br/><br/>
    </Container>
  );
}

/*
 <FormControlLabel
    control={<Checkbox value="remember" color="primary" />}
    label="Remember me"
  />
*/