import React, { useState, useEffect } from "react";
import InfiniteScroll from "react-infinite-scroll-component";
import Grid from '@material-ui/core/Grid';
import Event from '../components/eventCard';
import frontend from "../actions/givers";
import Alert from './alert';
import { CircularProgress } from "@material-ui/core";

const style = {
  width: 1000,
  overflow: "hidden",
};
const All = {
  display: "flex",
  justifyContent: "center",
  marginTop:0,
};


function Scroll(){
  const [cursor, setCursor] =useState("");
  const [alert, setAlert] = useState({
    description: "test",
    severity:"warning",
  });
  const [open, setOpen] = useState(false);

  const [items, setItem] = useState([]);

  const [hasMore, setHasMore] = useState(true);

  useEffect(() => {
    // Initialization code comes here
      frontend.listAllEvents(0,"","").then(function(json) {
        setCursor(json.cursor);
        setItem(items.concat(json.list));
        console.log("cursor"+json.cursor);
        if(json.cursor=="end") setHasMore(false);
      }); 
  }, []);

  const fetchMoreData = () => {
//    setTimeout(() => {
      frontend.listAllEvents(0,"",cursor).then(function(json) {
        setCursor(json.cursor);
        setItem(items.concat(json.list));
        console.log("cursor"+json.cursor);
        if(json.cursor=="end") setHasMore(false);
      });  
//    }, 5000);
    // a fake async api call like which sends
    // 20 more records in .5 secs
  /*  setTimeout(() => {
      setItem(items.concat(Array.from({ length: 21 })));
    }, 1000);*/
  };

    return (
      <div style={All}>
        <InfiniteScroll style={style}
          dataLength={items.length}
          next={fetchMoreData}
          hasMore={hasMore}
          loader={<div style={{ textAlign: "center", margin: `3% 0`}}>
          <CircularProgress  variant="indeterminate" color={"primary"}/>
        </div>}
          endMessage={
              <p style={{ textAlign: "center" }}>
              <b>Yay! You have seen it all</b>
            </p>
          }
          >
          <Grid container spacing={3}>
        
          {items.map((e, index) => (
              <>
                <Grid key={index} item sm={12}>
                    <Event style={style} event={e} image={'https://static.toiimg.com/photo/72975551.cms'} setAlert={setAlert} setOpen={setOpen} />
                </Grid>    
              </>
          ))}
          </Grid>
        </InfiniteScroll>
        <Alert description={alert.description} severity={alert.severity} open={open} setOpen={setOpen}/>
        </div>
    );
}

export default Scroll;
 /*<div style={style} key={index}>
              div - #{index}
            </div>*/