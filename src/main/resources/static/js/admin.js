import {requireLogin} from "./route-guard.js";

//Yeu cau da loggedIn bang method requireLogin();
document.addEventListener("DOMContentLoaded", function (){
        requireLogin();
})

