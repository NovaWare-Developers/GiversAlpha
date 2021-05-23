import { Paper } from '@material-ui/core';
import React from 'react';

function Top(props){
    return(
        <>
        <Paper elevation={3}>
            <p>Posição: {props.position}</p>
            <p>Username: {props.username}</p>
            <p>Points:  {props.points}</p>
        </Paper>
        </>
    );
}

export default Top;