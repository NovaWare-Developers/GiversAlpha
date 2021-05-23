import { Fab, Paper } from '@material-ui/core';
import { makeStyles, Theme, createStyles } from '@material-ui/core/styles';
import React from 'react';
import { useHistory, Link, Redirect } from 'react-router-dom';
import frontend from '../actions/givers';
import NavBar from '../navbar';
import Map from '../components/map';
import AddIcon from '@material-ui/icons/Add';
import FormDialog from '../components/formDialog';

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

function Profile() {
  const classes = useStyles();
  const [open, setOpen] = React.useState(false);
  const [selectedMarker,setSelectedMarker] = React.useState();
  const handleClickOpen = () => {
    if(selectedMarker!=undefined)setOpen(true);
  };


 

  if(!frontend.Authenticated()){
    return <Redirect to="/"/>
  }

  return (
    <>  
      <Paper>
        <Map selectedMarker={selectedMarker} setSelectedMarker={setSelectedMarker}/>
        <Fab color="primary" aria-label="add" className={classes.fab} onClick={handleClickOpen}>
          <AddIcon />
        </Fab>
        {open&&selectedMarker!=undefined?<FormDialog open={open} setOpen={setOpen} selectedMarker={selectedMarker}/>:""}
        
      </Paper>
    </>
  );
}

export default Profile;