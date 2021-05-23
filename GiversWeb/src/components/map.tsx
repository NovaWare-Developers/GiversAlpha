import React, { useEffect, useRef, useState } from 'react';
import { GoogleMap, LoadScript, Marker } from '@react-google-maps/api';

const containerStyle = {
  width: `100%`,
  height: '860px'
};


function Map(props) {
  const refMap = useRef(null);
  
  const [lat,setLat] = useState(0);
  const [lng,setLong] = useState(0);
  const [center, setCenter] = useState({lat: lat,lng: lng,});
  const [markers,setMarkers] = useState([
  {
    name:"Teste",
    lat: 38.7974272,
    long: -9.1783168
  },
]);
  function onClick(event) {
  const latLng  = event.latLng;
  props.setSelectedMarker(latLng);
  }
useEffect(() => {
  navigator.geolocation.getCurrentPosition(function(position) {
    setLat(position.coords.latitude);
    setLong(position.coords.longitude);
    setCenter({lat: position.coords.latitude,lng: position.coords.longitude,});
    console.log("Latitude is :", position.coords.latitude);
    console.log("Longitude is :", position.coords.longitude);
  });
}, []);
  
  
 /* const handleBoundsChanged = () => {
    const mapCenter = refMap.current.getCenter(); //get map center
    setCenter(mapCenter);
  };
*/
  const defaultcenter = {
    lat: lat,
    lng: lng,
  };

  return (
    <LoadScript
      googleMapsApiKey=""
    >
      <GoogleMap
        mapContainerStyle={containerStyle}
        center={center}
        zoom={15}
        ref={refMap}
        onClick={onClick}
      >
        { /* Child components, such as markers, info windows, etc. */ }
        <>
        <Marker position={props.selectedMarker}/>
        </>
      </GoogleMap>
    </LoadScript>
  )
}


export default Map;