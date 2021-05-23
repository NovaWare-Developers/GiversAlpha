import React from 'react';
import { Redirect } from 'react-router-dom';
import { createStyles, Theme, withStyles, WithStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import MuiDialogTitle from '@material-ui/core/DialogTitle';
import MuiDialogContent from '@material-ui/core/DialogContent';
import MuiDialogActions from '@material-ui/core/DialogActions';
import IconButton from '@material-ui/core/IconButton';
import CloseIcon from '@material-ui/icons/Close';
import Typography from '@material-ui/core/Typography';
import SignIn from './loginForm';
import Regist from './registForm';

import frontend from './actions/givers.js';
import { Paper } from '@material-ui/core';

const styles = (theme: Theme) =>
  createStyles({
    root: {
      margin: 0,
      padding: theme.spacing(2),
    },
    closeButton: {
      position: 'absolute',
      right: theme.spacing(1),
      top: theme.spacing(1),
      color: theme.palette.grey[500],
    },
  });

export interface DialogTitleProps extends WithStyles<typeof styles> {
  id: string;
  children: React.ReactNode;
  onClose: () => void;
}


const DialogTitle = withStyles(styles)((props: DialogTitleProps) => {
  const { children, classes, onClose, ...other } = props;
  return (
    <MuiDialogTitle disableTypography className={classes.root} {...other}>
      <Typography variant="h6">{children}</Typography>
      {onClose ? (
        <IconButton aria-label="close" className={classes.closeButton} onClick={onClose}>
          <CloseIcon />
        </IconButton>
      ) : null}
    </MuiDialogTitle>
  );
});

const DialogContent = withStyles((theme: Theme) => ({
  root: {
    padding: theme.spacing(2),
  },
}))(MuiDialogContent);

/*const DialogActions = withStyles((theme: Theme) => ({
  root: {
    margin: 0,
    padding: theme.spacing(1),
  },
}))(MuiDialogActions);*/

export default function CustomizedDialogs() {
  const [openLogin, setOpenLogin] = React.useState(false);
  const [openRegist, setOpenRegist] = React.useState(false);

  const handleClickOpen = () => {
    setOpenLogin(true);
  };
  const handleClose = () => {
    setOpenLogin(false);
  };

  const handleClickOpenRegist = () => {
    setOpenRegist(true);
  };
  const handleCloseRegist = () => {
    setOpenRegist(false);
  };

  if(frontend.Authenticated()){
    return <Redirect to="/home"/>
  }

  return (
    <Paper>
      <Button variant="outlined" color="primary" onClick={handleClickOpen}>
        Login
      </Button>
      <Button variant="outlined" color="primary" onClick={handleClickOpenRegist}>
        Registar
      </Button>
      
      <Dialog onClose={handleClose} aria-labelledby="customized-dialog-title" open={openLogin}>
        <DialogTitle id="customized-dialog-title" onClose={handleClose}>
          Login
        </DialogTitle>
        <DialogContent dividers>
          <SignIn/>
        </DialogContent>
        
      </Dialog>
      <Dialog onClose={handleCloseRegist} aria-labelledby="customized-dialog-title" open={openRegist}>
        <DialogTitle id="customized-dialog-title" onClose={handleCloseRegist}>
          Registar 
        </DialogTitle>
        <DialogContent dividers>
          <Regist/>
        </DialogContent>
        
      </Dialog>
    </Paper>
  );
}