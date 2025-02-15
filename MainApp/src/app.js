console.log("Initializing Main app ....");
import {getRouterInstance, loadContent} from "../../jsModules/src/baseRouter";
import fetchWithAuth from "../../jsModules/src/api";
// get apline instance
import Alpine from "alpinejs";
import icons from "../../jsModules/src/icons";
// register the icons
window.Icons = icons;

const router = getRouterInstance();
window.Router = router;
window.PP_API_URL = process.env.PP_API_URL;
window.PP_URL = process.env.PP_URL;
// define a router hook before every routing process it must be checked whether the user is authenticated or not.
// add a listener for alpine to be ready
document.addEventListener('alpine:init', () => {
    // when alpine is ready add a user object to the store
    // Alpine.store('user', {});
    router.hooks({
        after: (params) => {
            // Store route params and data in the global routeData object
            Alpine.store('routeData').params = params.params; // Parameters from the route, if any
            Alpine.store('routeData').data = params.data; // Other data, if any
            console.log('Route data saved in Alpine store:', Alpine.store('routeData'));
        },
        before: async (done, params) => {
            console.log('Before Hook');
            try {
                // Check if a token exists in cookies
                const hasToken = document.cookie.includes('token');
                const user = Alpine.store('user');
                console.log('User:', user);
                console.log('Has Token:', hasToken);
                if (hasToken && user === undefined) {
                    // Extract the token from cookies
                    const token = document.cookie.split('; ')
                        .find(row => row.startsWith('token='))
                        ?.split('=')[1];

                    // Attempt to authenticate with the token
                    let url = `${window.PP_API_URL}account/login`;
                    console.log('Token:', token);
                    console.log('URL:', url);
                    const response = await fetchWithAuth(url, {
                        method: 'GET',
                        headers: {
                            'Authorization': `Bearer ${token}`,
                        },
                    });

                    if (response.ok) {
                        const data = await response.json();
                        Alpine.store('user', data); // Save authenticated user to store
                        done(); // Proceed with routing
                    } else {
                        // Redirect to login page if token validation fails
                        console.warn('Token validation failed, redirecting to login.');
                        window.location = process.env.PP_URL;
                    }
                } else if (user === undefined) {
                    // Redirect to login if no user is authenticated
                    console.warn('No user authenticated, redirecting to login.');
                    window.location = process.env.PP_URL;
                } else {
                    console.log("all fine");
                    done(); // Proceed with routing if user is already authenticated
                }
            } catch (error) {
                console.error('Error in before hook:', error);
                window.location = 'http://localhost:3000/#/login'; // Redirect to login on error
            }
        },
    });
    router.on({
        '/': async () => {
            let menu = await import('./menu');
            Alpine.data('menu', () => menu.default);
            loadContent('content', './welcome');
        },
    });
    router.resolve();
});

window.Alpine = Alpine;
Alpine.start();

// set a null user object


// get the base router instance


const cookies = document.cookie.split(';');
if (cookies.length === 1 && cookies[0].trim() === '') {
    console.log('No cookies available');
}
cookies.forEach(cookie => {
    const [name, value] = cookie.split('=');
    console.log(`Cookie Name: "${name.trim()}", Value: "${decodeURIComponent(value)}"`);
});