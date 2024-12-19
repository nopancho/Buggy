import {getRouterInstance}  from "../../jsModules/src/baseRouter";

export default {
    logout(){
        document.cookie = 'token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
        window.location = 'http://localhost:3000/#/login';
    },
}