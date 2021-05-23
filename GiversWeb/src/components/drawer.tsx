import React from "react";
import clsx from "clsx";
import {
  createStyles,
  makeStyles,
  useTheme,
  Theme
} from "@material-ui/core/styles";
import Drawer from "@material-ui/core/Drawer";
import AppBar from "@material-ui/core/AppBar";
import Toolbar from "@material-ui/core/Toolbar";
import List from "@material-ui/core/List";
import CssBaseline from "@material-ui/core/CssBaseline";
import Typography from "@material-ui/core/Typography";
import Divider from "@material-ui/core/Divider";
import IconButton from "@material-ui/core/IconButton";
import MenuIcon from "@material-ui/icons/Menu";
import ListItem from "@material-ui/core/ListItem";
import ListItemIcon from "@material-ui/core/ListItemIcon";
import ListItemText from "@material-ui/core/ListItemText";
import InboxIcon from "@material-ui/icons/MoveToInbox";
import MailIcon from "@material-ui/icons/Mail";
import Routes from "../routes"
import { NavLink as NavLinkRRD, Link, useHistory } from "react-router-dom";
import { Avatar, Button } from "@material-ui/core";
import Brightness4Icon from '@material-ui/icons/Brightness4';
import frontend from "../actions/givers";
import MeetingRoomIcon from '@material-ui/icons/MeetingRoom';

const drawerWidth = 240;

const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    root: {
      display: "flex"
    },
    media: {
        height: 0,
        paddingTop: '18.75%', // 16:9
    },
    appBar: {
      zIndex: theme.zIndex.drawer + 1,
      transition: theme.transitions.create(["width", "margin"], {
        easing: theme.transitions.easing.sharp,
        duration: theme.transitions.duration.leavingScreen
      }),
      background: 'secondary',
    },
    appBarShift: {
      marginLeft: drawerWidth,
      width: `calc(100% - 0)`,
      transition: theme.transitions.create(["width", "margin"], {
        easing: theme.transitions.easing.sharp,
        duration: theme.transitions.duration.enteringScreen
      })
    },
    menuButton: {
      marginRight: 36,
    },
    hide: {
      display: "none"
    },
    drawer: {
      width: drawerWidth,
      flexShrink: 0,
      whiteSpace: "nowrap",
      
    },
    drawerOpen: {
      width: drawerWidth,
      transition: theme.transitions.create("width", {
        easing: theme.transitions.easing.sharp,
        duration: theme.transitions.duration.enteringScreen
      })
    },
    drawerClose: {
      transition: theme.transitions.create("width", {
        easing: theme.transitions.easing.sharp,
        duration: theme.transitions.duration.leavingScreen
      }),
      overflowX: "hidden",
      width: theme.spacing(7) + 1,
      [theme.breakpoints.up("sm")]: {
        width: theme.spacing(7) + 1
      }
    },
    toolbar: {
      display: "flex",
      alignItems: "center",
      justifyContent: "flex-end",
      padding: theme.spacing(0, 1),
      // necessary for content to be below app bar
      ...theme.mixins.toolbar
    },
    content: {
      flexGrow: 1,
      padding: theme.spacing(3),
    }
  })
);

export default function MiniDrawer(props) {
  const classes = useStyles();
  const theme = useTheme();
  const [open, setOpen] = React.useState(false);
  let history = useHistory();

  const handleDrawer = (props) => {
    setOpen(!open);
  };

  const [collapseOpen, setCollapseOpen] = React.useState(false);
  // verifies if routeName is the one active (in browser input)
  const activeRoute = (routeName) => {
    return props.location.pathname.indexOf(routeName) > -1 ? "active" : "";
  };
  // toggles collapse between opened and closed (true/false)
  const toggleCollapse = () => {
    setCollapseOpen((data) => !data);
  };
  // closes the collapse
  const closeCollapse = () => {
    setCollapseOpen(false);
  };

  function logout(){
    frontend.logOut().then(function(){
        history.push('/');
    });
  }

  const createLinks = (routes) => {
    return routes.map((prop, key) => {
      return (
        <Link to={prop.layout + prop.path} key={key} style={{ textDecoration: 'none', color: 'inherit' }}>
          <ListItem button onClick={() => {}}>
            <ListItemIcon>
              <prop.icon/>
            </ListItemIcon>
            <ListItemText primary={prop.name} />
            
          </ListItem>
        </Link>
      );
    });
  };

  return (
    <div className={classes.root}>
      <CssBaseline />
      <AppBar
        position="fixed"
        className={clsx(classes.appBar, {
          [classes.appBarShift]: open
        })}
      >
        <Toolbar>
          <IconButton
            color="inherit"
            aria-label="open drawer"
            onClick={handleDrawer}
            edge="start"
            className={clsx(classes.menuButton)}
          >
            <MenuIcon/>
          </IconButton>
          <Typography variant="h6" noWrap>
            Givers
          </Typography>
          <Avatar src="/broken-image.jpg" />
        </Toolbar>
      </AppBar>
      <Drawer
        variant="permanent"
        className={clsx(classes.drawer, {
          [classes.drawerOpen]: open,
          [classes.drawerClose]: !open
        })}
        classes={{
          paper: clsx({
            [classes.drawerOpen]: open,
            [classes.drawerClose]: !open
          })
        }}
      >
        <div className={classes.toolbar}/>
        <Divider />
        <List>
          {createLinks(Routes)}
        </List>
        <Divider />
        <List>
          <ListItem button>
            <ListItemIcon>
              <Brightness4Icon />
            </ListItemIcon>
            <ListItemText primary={"DarkMode"} />
          </ListItem>
          <ListItem button onClick={logout}>
            <ListItemIcon>
              <MeetingRoomIcon/>
            </ListItemIcon>
            <ListItemText primary={"Logout"} />
          </ListItem>
        </List>
      </Drawer>
      <main className={classes.content}>
        <div className={classes.toolbar} />
          {props.page}
      </main>
    </div>
  );
}