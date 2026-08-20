
import {requireLogin} from "./route-guard.js";

document.addEventListener("DOMContentLoaded", function (){
    requireLogin();
})