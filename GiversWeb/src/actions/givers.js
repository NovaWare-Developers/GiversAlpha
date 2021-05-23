const BACKURL = 'https://givers-volunteering.appspot.com/rest';

const RESTLOGIN = '/login';
const RESTREGISTER = '/register';
const RESTLOGOUT = '/logout';
const RESTUPDATEUSER = '/edit/profile';
const LISTALLEVENTS = '/query/eventsFiltered';
const EDITPROFILE = '/edit/profile';
const CREATEEVENT = '/register/event';



class Front{
    constructor(){
    console.log("frontendlink created");
    }   

    Authenticated(){
        var token = localStorage.getItem('at');
        const now = Date.now();
        if(token){
            token = JSON.parse(token);
            if(token.expirationDate>now){
                return true;
            } else {
                localStorage.removeItem('at');
            }
        }
        return false;
    }

    login(user,pass){
        console.log("Login");
        return fetch(BACKURL+RESTLOGIN, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify( {
                username : user,
                password : pass,
            })
        
        })
        .then(function(response) {
            return response.text();
        })
        .then(function(text) {
            if(!text.includes('{')){
                return [false,text];
            }
            else {
                localStorage.setItem('at', text);
                return [true];
            }
        })
    }

    logOut(){
        return fetch(BACKURL+RESTLOGOUT, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify( {
                at: JSON.parse(localStorage.getItem('at'))
            })
        }).then(() => {
            
            localStorage.removeItem('at');
        });
    }

    regist(user,email,nameU,pass,passConf){        
        return fetch(BACKURL+RESTREGISTER, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify( {
                username : user,
                name: nameU,
                password : pass,
                passConfirm : passConf,
                email : email,
            })
        })
        .then(function(response) {
            return response.text();
        })
        .then(function(text) {
            alert(text);
            if(text.includes('successfully')){
             return true;
            }
            return false;
        });
    }

    editProfile(adress,dateOfBirth,desc,gender,interests, nameU,nationality,phoneNr){        
        return fetch(BACKURL+RESTUPDATEUSER, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                address: adress,
                at: JSON.parse(localStorage.getItem('at')),
                dateOfBirth: dateOfBirth,
                description: desc,
                gender: gender,
                interests: interests,
                name: nameU,
                nationality: nationality,
                phoneNr: phoneNr
              })
        })
        .then(function(response) {
            return response.text();
        })
        .then(function(text) {
            alert(text);
            if(text.includes('successfully')){
             //   location.reload();
            }
        });
    }

    listAllEvents(selector,interest,cursor){
        console.log("listAllEvents");
        return fetch(BACKURL+LISTALLEVENTS, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify( {
                at: JSON.parse(localStorage.getItem('at')),
                queryTime: selector,
                interests: interest,
                startCursorString: cursor,
            })
        
        })
        .then(function(response) {
            if(response.ok){
                return response.text();
            }
        })
        .then(function(text) {
            return JSON.parse(text);
        })
    }

    editProfile(adress,dateOfBirth,description,gender,interests,nationality,phoneNr,photo){
        console.log("editProfile");
        return fetch(BACKURL+EDITPROFILE, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify( {
                at: JSON.parse(localStorage.getItem('at')),
                address: adress,
                dateOfBirth: dateOfBirth,
                description: description,
                gender: gender,
                interests: interests,
                nationality: nationality,
                phoneNr: phoneNr,
                photo: photo,
            })
        
        })
        .then(function(response) {
            if(response.ok){
                return response.text();
            }
        })
        .then(function(text) {
            console.log(text);
            return text;
        })
    }

    registEvent(adress,capacity,description,duration,interests,institutionName,dateStart,nameE,lat,long){
        console.log("editProfile");
        return fetch(BACKURL+CREATEEVENT, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify( {
                at: JSON.parse(localStorage.getItem('at')),
                address: adress,
                capacity: capacity,
                dateStart: dateStart,
                description: description,
                interests: interests,
                duration: duration,
                institutionName: institutionName,
                name: nameE,
                markers:[lat,long],
            })
        
        })
        .then(function(response) {
            if(response.ok){
                return response.text();
            } else{
                return response.text();
            }
        })
        .then(function(text) {
            console.log(text);
            return text;
        })
    }
}

const frontend = new Front();

export default frontend;
