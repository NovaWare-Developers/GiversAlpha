import React, { useState } from 'react';
import { makeStyles, Theme, createStyles } from '@material-ui/core/styles';
import clsx from 'clsx';
import Card from '@material-ui/core/Card';
import CardHeader from '@material-ui/core/CardHeader';
import CardMedia from '@material-ui/core/CardMedia';
import CardContent from '@material-ui/core/CardContent';
import CardActions from '@material-ui/core/CardActions';
import Collapse from '@material-ui/core/Collapse';
import Avatar from '@material-ui/core/Avatar';
import IconButton from '@material-ui/core/IconButton';
import Typography from '@material-ui/core/Typography';
import { red } from '@material-ui/core/colors';
import ShareIcon from '@material-ui/icons/Share';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import BookmarkBorderIcon from '@material-ui/icons/BookmarkBorder';
import BookmarkIcon from '@material-ui/icons/Bookmark';
import AccessTimeIcon from '@material-ui/icons/AccessTime';
import { Button } from '@material-ui/core';
import EmojiPeopleIcon from '@material-ui/icons/EmojiPeople';

const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    root: {
      
    },
    media: {
      height: 0,
      paddingTop: '18.75%', // 16:9
    },
    expand: {
      transform: 'rotate(0deg)',
      marginLeft: 'auto',
      transition: theme.transitions.create('transform', {
        duration: theme.transitions.duration.shortest,
      }),
    },
    expandOpen: {
      transform: 'rotate(180deg)',
    },
    avatar: {
      backgroundColor: red[500],
    },
  }),
);


export default function RecipeReviewCard(props) {
    const classes = useStyles();
    const [expanded, setExpanded] = useState(false);
    const [kept,setKept] = useState(false);
    const [capacity,setCapacity] = useState(0);
    const [maxCapacity,setMaxCapacity] = useState(0);
    
    function KeepButton(){
        if(kept) return  <BookmarkIcon/>;
        return <BookmarkBorderIcon/>;
    }
    
  const handleExpandClick = () => {
    setExpanded(!expanded);
  };

  const keep = () => {
    setKept(!kept);
    props.setAlert({
      description: "Evento Guardado!",
      severity:"info",
    });
    
    props.setOpen(true);
  };
  
    const join = () => {
      setCapacity(capacity+1);
    };

  function Dur(){
    const duration = Math.floor((props.event.date_end.value - props.event.date_start.value)/1000);
    return <>{duration+" Minutos"}</>;
  }

  function Cap(){
    setMaxCapacity(props.event.capacity.value);
    if(props.event.participants.value.lenght!=undefined) setCapacity(props.event.participants.value.lenght);
    return <>{capacity+" / "+maxCapacity}</>;
  }

  return (
    <Card className={classes.root}>
      <CardHeader
        avatar={
          <Avatar aria-label="recipe" className={classes.avatar}>
            N
          </Avatar>
        }
        action={
          <IconButton disabled={true} aria-label="settings">
            <div><Dur/>&nbsp;</div>
          <AccessTimeIcon/>
          </IconButton>
        }
        title={props.event.name.value}
        subheader={new Date(props.event.date_start.value).toLocaleString([], { timeZone: 'UTC',year: 'numeric', month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' })+""}
      />
      
      <CardMedia
        className={classes.media}
        image={props.image}
        title={props.event.name.value}
      />
      
      <CardActions disableSpacing>
        <IconButton aria-label="keep" disabled={kept} onClick={keep}>
            <KeepButton/>
        </IconButton>
        <IconButton aria-label="share">
          <ShareIcon />
        </IconButton>
        <IconButton
          className={clsx(classes.expand, {
            [classes.expandOpen]: expanded,
          })}
          onClick={handleExpandClick}
          aria-expanded={expanded}
          aria-label="show more"
        >
          <ExpandMoreIcon />
        </IconButton>
      </CardActions>
      <Collapse in={expanded} timeout="auto" unmountOnExit>
        <CardContent>
          <Typography variant="h6" color="textSecondary" component="p">
            {"Morada: "}
          </Typography>
          <Typography variant="body2" paragraph >{props.event.address.value}</Typography>
          <Typography variant="h6" color="textSecondary" component="p">
            Descrição
          </Typography>
          <Typography variant="body2" paragraph >{props.event.description.value}</Typography>
          <CardActions>
          <Button size="small" color="primary" variant="contained" onClick={join} disabled={capacity==maxCapacity}>Join</Button>
          <IconButton disabled={true} aria-label="settings">
          <EmojiPeopleIcon/>
            <div>&nbsp;<Cap/></div>
          </IconButton>
          </CardActions>
          
        </CardContent>
      </Collapse>
    </Card>
  );
}