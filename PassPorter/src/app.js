
window.PP_API_URL = process.env.PP_API_URL;
import {getRouterInstance, loadContent} from "../../jsModules/src/baseRouter";
import Alpine from "alpinejs";
import icons from "../../jsModules/src/icons";
const router = getRouterInstance();
window.Router = router;
window.Icons = icons;
document.addEventListener('alpine:init', () => {
    const Alpine = window.Alpine;

// Define your routes
    router.on({
        '/': async () => {
            // register the component
            loadContent('content', './welcome');
        },
        '/login': async (data) => {
            let loginSignup = await import('./login-signup/login-signup');
            Alpine.data('loginSignup', (initialState = 'login') => ({
                ...loginSignup.default,
                state: initialState, // Pass the initial state dynamically
            }));
            loadContent('content', './login-signup/login-signup');
        },
        '/signup': async (data) => {
            let loginSignup = await import('./login-signup/login-signup');
            Alpine.data('loginSignup', (initialState = 'signup') => ({
                ...loginSignup.default,
                state: initialState, // Pass the initial state dynamically
            }));
            loadContent('content', './login-signup/login-signup');
        },
        '/confirm-signup': async (data) => {
            let confirmSignup = await import('./confirm-signup/confirm-signup');
            Alpine.data('confirmSignup', () => confirmSignup.default);
            loadContent('content', './confirm-signup/confirm-signup');
        }
    });
    router.resolve();
});

window.Alpine = Alpine;
Alpine.start();

