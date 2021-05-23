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

import frontend from './actions/givers.js';

const useStyles = makeStyles((theme) => ({
  paper: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
  },
  form: {
    width: '100%', // Fix IE 11 issue.
    marginTop: theme.spacing(3),
  },
  submit: {
    margin: theme.spacing(5, 0, 2),
  },
}));

export default function SignUp() {
  const classes = useStyles();
  let history = useHistory();

  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [nameU, setNameU] = useState("");
  const [password, setPassword] = useState("");
  const [passwordConf, setPasswordConf] = useState("");

  function usernameHandler(e:ChangeEvent<HTMLInputElement>){
    setUsername(e.target.value);
  }
  function emailHandler(e:ChangeEvent<HTMLInputElement>){
    setEmail(e.target.value);
  }
  function nameHandler(e:ChangeEvent<HTMLInputElement>){
    setNameU(e.target.value);
  }
  function passwordHandler(e:ChangeEvent<HTMLInputElement>){
      setPassword(e.target.value);
  }
  function passwordConfirmHandler(e:ChangeEvent<HTMLInputElement>){
    setPasswordConf(e.target.value);
  }

  function handleRegist(){
    if (username == "" || email == "" || password == "" || passwordConf == "") {
        alert("Values can't be empty");
    } else {
        frontend.regist(username,email,nameU,password,passwordConf).then(function(success){
            if(success) alert("Registo feito com sucesso");
        });
    }
  }

  return (
    <Container component="main" maxWidth="xs">
      <CssBaseline />
      <div className={classes.paper}>
        <form className={classes.form} noValidate>
          <Grid container spacing={2}>
            <Grid item xs={12}>
              <TextField
                variant="outlined"
                fullWidth
                id="username"
                label="Username"
                name="username"
                autoComplete="username"
                value={username}
                onChange={usernameHandler}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                variant="outlined"
                fullWidth
                id="name"
                label="Nome"
                name="name"
                autoComplete="name"
                value={nameU}
                onChange={nameHandler}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                variant="outlined"
                fullWidth
                id="email"
                label="Email"
                name="email"
                autoComplete="email"
                value={email}
                onChange={emailHandler}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                variant="outlined"
                fullWidth
                name="password"
                label="Password"
                type="password"
                id="password"
                autoComplete="current-password"
                value={password}
                onChange={passwordHandler}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                variant="outlined"
                fullWidth
                name="passwordConfirm"
                label="Confirmar Password"
                type="password"
                id="passwordConfirm"
                autoComplete="current-password"
                value={passwordConf}
                onChange={passwordConfirmHandler}
              />
            </Grid>
          </Grid>
          <Button
            fullWidth
            variant="contained"
            color="primary"
            className={classes.submit}
            onClick={handleRegist}
          >
            Registar
          </Button>
          <Grid container justify="flex-end">
            <Grid item>
              <Link href="" variant="body2">
                Já tens uma conta? Log in
              </Link>
            </Grid>
          </Grid>
        </form>
      </div>
      <Box mt={5}>
        <Copyright />
      </Box>
    </Container>
  );
}