import React, {ChangeEvent} from 'react';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import Paper, { PaperProps } from '@material-ui/core/Paper';
import Draggable from 'react-draggable';
import { Grid, TextField } from '@material-ui/core';
import frontend from '../actions/givers';

import { GoogleMap, LoadScript, Marker } from '@react-google-maps/api';
function PaperComponent(props: PaperProps) {
  return (
    <Draggable handle="#draggable-dialog-title" cancel={'[class*="MuiDialogContent-root"]'}>
      <Paper {...props} />
    </Draggable>
  );
}

export default function DraggableDialog(props) {
    const [address, setAddress] = React.useState("");
    const [capacity, setCapacity] = React.useState(0);
    const [dateStart, setDateStart] = React.useState(0);
    const [interests, setInterests] = React.useState("");
    const [duration, setDuration] = React.useState(0);
    const [institutionName, setInstitutionName] = React.useState("");
    const [description, setDescription] = React.useState("");
    const [name, setName] = React.useState("");
    const [lat, setLat] = React.useState(0);
    const [long, setLong] = React.useState(0);

    function addressHandler(e:ChangeEvent<HTMLInputElement>){
      setAddress(e.target.value);
    }

    function capacityHandler(e:ChangeEvent<HTMLInputElement>){
      setCapacity(isNaN(parseInt(e.target.value))?0:parseInt(e.target.value));
    }

    function dateStartHandler(e:ChangeEvent<HTMLInputElement>){
      setDateStart(isNaN(parseInt(e.target.value))?0:parseInt(e.target.value));
    }

    function interestsHandler(e:ChangeEvent<HTMLInputElement>){
      setInterests(e.target.value);
    }

    function durationHandler(e:ChangeEvent<HTMLInputElement>){
      setDuration(isNaN(parseInt(e.target.value))?0:parseInt(e.target.value));
    }

    function institutionNameHandler(e:ChangeEvent<HTMLInputElement>){
      setInstitutionName(e.target.value);
    }

    function descriptionHandler(e:ChangeEvent<HTMLInputElement>){
      setDescription(e.target.value);
    }

    function nameHandler(e:ChangeEvent<HTMLInputElement>){
      setName(e.target.value);
    }
    
    function latHandler(e:ChangeEvent<HTMLInputElement>){
      setLat(isNaN(parseInt(e.target.value))?0:parseInt(e.target.value));
    }

    function longHandler(e:ChangeEvent<HTMLInputElement>){
      setLong(isNaN(parseInt(e.target.value))?0:parseInt(e.target.value));
    }

  const handleClose = () => {
    props.setOpen(false);
  };
  
  const handleSubmit = () => {
    frontend.registEvent(address,capacity,description,duration,interests,institutionName,dateStart,name,lat,long);
    handleClose();
  };

  const handleCancel = () => {
    handleClose();
  };
  return (
    <div>
    
      <Dialog
        open={props.open}
        onClose={handleClose}
        PaperComponent={PaperComponent}
        aria-labelledby="draggable-dialog-title"
      >
        <DialogTitle style={{ cursor: 'move' }} id="draggable-dialog-title">
          Criar Evento
        </DialogTitle>
        <DialogContent>
        <TextField
            variant="outlined"
            margin="normal"
            fullWidth
            id="address"
            label="Morada"
            name="adress"
            autoComplete="adress"
            autoFocus
            value={address}
            onChange={addressHandler}
          />
        <TextField
            variant="outlined"
            margin="normal"
            fullWidth
            id="capacity"
            label="Capacidade"
            name="capacity"
            autoComplete="capcity"
            autoFocus
            value={capacity}
            onChange={capacityHandler}
          />
        <TextField
            variant="outlined"
            margin="normal"
            fullWidth
            id="dateStart"
            label="Data de início"
            name="dateStart"
            autoComplete="dateStart"
            autoFocus
            value={dateStart}
            onChange={dateStartHandler}
          />
        <TextField
            variant="outlined"
            margin="normal"
            fullWidth
            id="interests"
            label="Interesses"
            name="interests"
            autoComplete="interests"
            autoFocus
            value={interests}
            onChange={interestsHandler}
          />
        <TextField
            variant="outlined"
            margin="normal"
            fullWidth
            id="duration"
            label="Duração"
            name="duration"
            autoComplete="duration"
            autoFocus
            value={duration}
            onChange={durationHandler}
          />
        <TextField
            variant="outlined"
            margin="normal"
            fullWidth
            id="institutionName"
            label="Nome da Instituição"
            name="institutionName"
            autoFocus
            value={institutionName}
            onChange={institutionNameHandler}
          />
        <TextField
            variant="outlined"
            margin="normal"
            fullWidth
            id="description"
            label="Descrição do evento"
            name="description"
            autoFocus
            value={description}
            onChange={descriptionHandler}
          />
        <TextField
            variant="outlined"
            margin="normal"
            fullWidth
            id="name"
            label="Nome do Evento"
            name="name"
            autoFocus
            value={name}
            onChange={nameHandler}
          />
          <Grid container justify='space-around'>
          <Grid item xs={6} sm={4}>
        <TextField
            disabled
            variant="outlined"
            margin="normal"
            fullWidth
            id="lat"
            label="Latitude"
            name="lat"
            autoFocus
            value={props.selectedMarker.lat()}
          />
          </Grid>
          <Grid item xs={6} sm={4}>
        <TextField
            disabled
            variant="outlined"
            margin="normal"
            fullWidth
            id="long"
            label="Longitude"
            name="long"
            autoFocus
            value={props.selectedMarker.lng()}
          /></Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button autoFocus onClick={handleCancel} color="primary">
            Cancelar
          </Button>
          <Button onClick={handleSubmit} color="primary">
            Criar
          </Button>
        </DialogActions>
      </Dialog>
    </div>
  );
}