import React from 'react';
import NavBar from '../navbar';
import TextField from '@material-ui/core/TextField';
import { Paper } from '@material-ui/core';

import { makeStyles, Theme, createStyles } from '@material-ui/core/styles';

function Profile() {
    const useStyles = makeStyles((theme: Theme) =>
    createStyles({
      root: {
        
      },
      paper: {
        width: `100%`,
        height: `100%`,
      },
      fab: {
        position: 'absolute',
        bottom: theme.spacing(2),
        right: theme.spacing(2),
      },
    }),
  );

  const classes = useStyles();

  return (
    <Paper className={classes.paper}>  
      {"Email"}
      <TextField
            variant="outlined"
            margin="normal"
            fullWidth
            name="password"
            value="teste@gmail.com"
            type="email"
            id="password"
            autoComplete="current-password"
          />
    </Paper>
  );
}

export default Profile;