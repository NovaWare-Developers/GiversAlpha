import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Card from '@material-ui/core/Card';
import CardActionArea from '@material-ui/core/CardActionArea';
import CardActions from '@material-ui/core/CardActions';
import CardContent from '@material-ui/core/CardContent';
import CardMedia from '@material-ui/core/CardMedia';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
const useStyles = makeStyles((theme)=>({
    root: {
      maxWidth: 345,
      margin: theme.spacing(3, 1, 2),
    },
  }));

function Top(props){
    const classes = useStyles();

    return(
        <>
        <Card className={classes.root}>
            <CardActionArea>
                <CardMedia
                component="img"
                alt="Item image"
                height="140"
                image={props.image}
                title="Item image"
                />
                <CardContent>
                <Typography gutterBottom variant="h5" component="h2">
                    {props.title}
                </Typography>
                <Typography variant="body2" color="textSecondary" component="p">
                    {props.description}
                </Typography>
                </CardContent>
            </CardActionArea>
            <CardActions>
                <Typography variant="body2" color="textSecondary" component="p">
                    {props.cost}
                </Typography>
                <Button size="small" color="primary" disabled={props.disabled}>
                Redeem
                </Button>
            </CardActions>
        </Card>
        </>
    );
}

export default Top;