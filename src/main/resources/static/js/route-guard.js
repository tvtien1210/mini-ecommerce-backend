//import cac function tu ./auth.js

import {isLoggedIn, isAdmin} from "./auth.js";
// Check loggedIn
export function requireLogin(){
    //Neu chua login, link toi /login.html
    if(!isLoggedIn()){
    window.location.href="/login";
    return;
    }

    if(!isAdmin()){
    window.location.href="/403";
    }
}