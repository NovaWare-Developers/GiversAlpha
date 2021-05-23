import React from "react";
import { useLocation, Route, Switch, Redirect } from "react-router-dom";
// core components
import AdminNavbar from "../navbar";
import Sidebar from "../components/drawer";

import routes from "../routes";

const Admin = (props) => {
  const mainContent = React.useRef(null);
  const location = useLocation();

  React.useEffect(() => {
    document.documentElement.scrollTop = 0;
    document.scrollingElement!.scrollTop = 0;
  }, [location]);

  const getRoutes = (routes) => {
    return routes.map((prop, key) => {
      if (prop.layout === "/app") {
        return (
          <Route
            path={prop.layout + prop.path}
            component={prop.component}
            key={key}
          />
        );
      } else {
        return null;
      }
    });
  };

  const getBrandText = (path) => {
    for (let i = 0; i < routes.length; i++) {
      if (
        props.location.pathname.indexOf(routes[i].layout + routes[i].path) !==
        -1
      ) {
        return routes[i].name;
      }
    }
    return "Brand";
  };

  console.log(getRoutes(routes));
  return (
    <>
      <Sidebar
        {...props}
        routes={routes}
        logo={{
          innerLink: "/ ",
          imgSrc: require("../assets/white.png").default,
          imgAlt: "...",
        }}
      page={<div className="main-content" ref={mainContent}>
        <Switch>
          {getRoutes(routes)}
          <Redirect from="*" to="/app" />
        </Switch>
      </div>}
      />
    </>
  );
};

export default Admin;
