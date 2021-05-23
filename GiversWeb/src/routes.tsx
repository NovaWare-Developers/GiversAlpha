import Home from "./home";
import Tops from "./pages/tops"
import Maps from "./pages/maps";
import List from "./pages/list";
import Marketplace from "./pages/marketplace";
import Profile from "./pages/profile";

import EditProfileTeste from './pages/editprofiletest';

import ProfileTeste from './pages/profileTest/profiletest';
import ShareDialogTeste from './pages/test';

import DashboardIcon from '@material-ui/icons/Dashboard';
import BarChartIcon from '@material-ui/icons/BarChart';
import MapIcon from '@material-ui/icons/Map';
import EventsIcon from '@material-ui/icons/AccessibilityNew';
import StoreIcon from '@material-ui/icons/LocalGroceryStore';
import PersonIcon from '@material-ui/icons/Person';
import TestIcon from '@material-ui/icons/BugReport';

/*import Profile from "views/examples/Profile.js";
import Maps from "views/examples/Maps.js";
import Register from "views/examples/Register.js";
import Login from "views/examples/Login.js";
import Tables from "views/examples/Tables.js";
import Icons from "views/examples/Icons.js";*/


var routes = [
  {
    path: "/dashboard",
    name: "Dashboard",
    icon: DashboardIcon,
    component: Home,
    layout: "/app",
  },
  {
    path: "/tops",
    name: "Tops",
    icon: BarChartIcon,
    component: Tops,
    layout: "/app",
  },
  {
    path: "/maps",
    name: "Mapa",
    icon: MapIcon,
    component: Maps,
    layout: "/app",
  },
  {
    path: "/list",
    name: "Eventos",
    icon: EventsIcon,
    component: List,
    layout: "/app",
  },
  {
    path: "/loja",
    name: "Loja",
    icon: StoreIcon,
    component: Marketplace,
    layout: "/app",
  },
  {
    path: "/profile",
    name: "Perfil",
    icon: PersonIcon,
    component: ProfileTeste,
    layout: "/app",
  },
  {
    path: "/editprofile",
    name: "(Teste) Editar Perfil",
    icon: TestIcon,
    component: EditProfileTeste,
    layout: "/app",
  },
  {
    path: "/share",
    name: "(Teste) Share Dialog",
    icon: TestIcon,
    component: ShareDialogTeste,
    layout: "/app",
  },
  {
    path: "/landing",
    name: "(Teste) Landing Page",
    icon: TestIcon,
    component: ShareDialogTeste,
    layout: "/teste",
  },
];
export default routes;